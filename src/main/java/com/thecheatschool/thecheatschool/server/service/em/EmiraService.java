package com.thecheatschool.thecheatschool.server.service.em;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecheatschool.thecheatschool.server.model.em.EmiraAnalysisRequest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class EmiraService {

    @Value("${emira.gemini.primary-key}")
    private String primaryKey;

    @Value("${emira.gemini.backup-key}")
    private String backupKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final CircuitBreaker circuitBreaker;

    private static final String GEMINI_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:streamGenerateContent?key=%s";

    public EmiraService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("emiraGemini");
    }

    @PostConstruct
    public void resetCircuitBreaker() {
        circuitBreaker.reset();
        log.info("Emira circuit breaker reset on startup");
    }

    public void analyse(EmiraAnalysisRequest request, SseEmitter emitter) {
        if (!circuitBreaker.tryAcquirePermission()) {
            sendError(emitter, "Emira is temporarily unavailable. Please try again.");
            return;
        }

        String prompt = buildPrompt(request);
        AtomicBoolean hasSentFallback = new AtomicBoolean(false);
        
        // Attempt with primary key first
        callGeminiWithFallback(prompt, primaryKey, backupKey, emitter, hasSentFallback);
    }

    private void callGeminiWithFallback(String prompt, String currentKey, String fallbackKey, SseEmitter emitter, AtomicBoolean hasSentFallback) {
        long startTime = System.currentTimeMillis();
        String url = String.format(GEMINI_URL_TEMPLATE, currentKey);

        Map<String, Object> body = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        webClient.post()
            .uri(url)
            .bodyValue(body)
            .retrieve()
            .bodyToFlux(String.class)
            .subscribe(
                chunk -> {
                    try {
                        String token = extractTokenFromChunk(chunk);
                        if (token != null) {
                            emitter.send(SseEmitter.event().data(token));
                        }
                    } catch (Exception e) {
                        log.error("Error processing Gemini chunk", e);
                    }
                },
                error -> {
                    log.error("Gemini call failed for key: {}", currentKey.substring(0, 5) + "...", error);
                    
                    if (fallbackKey != null && !hasSentFallback.get()) {
                        hasSentFallback.set(true);
                        callGeminiWithFallback(prompt, fallbackKey, null, emitter, hasSentFallback);
                    } else {
                        // Record failure in circuit breaker only if BOTH keys fail
                        circuitBreaker.onError(System.currentTimeMillis() - startTime, java.util.concurrent.TimeUnit.MILLISECONDS, error);
                        sendError(emitter, "Emira is temporarily unavailable. Please try again.");
                    }
                },
                () -> {
                    // Record success in circuit breaker
                    circuitBreaker.onSuccess(System.currentTimeMillis() - startTime, java.util.concurrent.TimeUnit.MILLISECONDS);
                    try {
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("Error completing emitter", e);
                    }
                }
            );
    }

    private String extractTokenFromChunk(String chunk) {
        try {
            // Remove leading/trailing brackets or commas if it's a stream array
            String cleanChunk = chunk.trim();
            if (cleanChunk.startsWith("[")) cleanChunk = cleanChunk.substring(1);
            if (cleanChunk.endsWith("]")) cleanChunk = cleanChunk.substring(0, cleanChunk.length() - 1);
            if (cleanChunk.startsWith(",")) cleanChunk = cleanChunk.substring(1);
            
            if (cleanChunk.isEmpty()) return null;

            JsonNode node = objectMapper.readTree(cleanChunk);
            if (node.has("candidates") && node.get("candidates").isArray()) {
                JsonNode candidate = node.get("candidates").get(0);
                if (candidate.has("content") && candidate.get("content").has("parts")) {
                    JsonNode parts = candidate.get("content").get("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        return parts.get(0).get("text").asText();
                    }
                }
            }
        } catch (Exception e) {
            log.trace("Failed to parse potential chunk: {}", chunk);
        }
        return null;
    }

    private void sendError(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("error").data(message));
            emitter.complete();
        } catch (Exception e) {
            log.error("Error sending error event", e);
        }
    }

    private String buildPrompt(EmiraAnalysisRequest request) {
        String area = request.getArea();
        String marketContext = request.getMarketContext();
        String additionalContext = request.getAdditionalContext() != null ? request.getAdditionalContext() : "";

        return switch (request.getAnalysisType()) {
            case PRICE_FORECAST -> String.format("""
                You are Emira, an expert Dubai real estate analyst 
                for Emiratiyo Investments. Based on the market data 
                provided and your knowledge of Dubai real estate, 
                provide a price forecast for %s.
                
                Market Context: %s
                Additional User Context: %s
                
                Structure your response exactly like this: 
                CURRENT PRICE: [from market data] 
                1 YEAR FORECAST: [price range AED/sqft + %% change] 
                3 YEAR FORECAST: [price range AED/sqft + %% change] 
                5 YEAR FORECAST: [price range AED/sqft + %% change] 
                KEY DRIVERS: [3-4 bullet points] 
                CONFIDENCE: [Low/Medium/High + one line reason] 
                DISCLAIMER: This is an AI-assisted projection 
                based on available data, not a financial guarantee.
                """, area, marketContext, additionalContext);
            case RENTAL_YIELD -> String.format("""
                You are Emira, an expert Dubai real estate analyst 
                for Emiratiyo Investments. Based on the market data 
                provided, provide a rental yield analysis for %s.
                
                Market Context: %s
                Additional User Context: %s
                
                Structure your response exactly like this: 
                CURRENT AVG RENT: [AED/year for apartment] 
                GROSS YIELD: [%% range] 
                NET YIELD: [%% range after typical costs] 
                1 YEAR RENTAL INCOME: [on AED 2M property] 
                3 YEAR RENTAL INCOME: [cumulative estimate] 
                5 YEAR RENTAL INCOME: [cumulative estimate] 
                RENTAL DEMAND OUTLOOK: [2-3 sentences] 
                BEST PROPERTY TYPE: [apartment/villa + reason] 
                DISCLAIMER: Estimates based on current market 
                data and historical Dubai rental trends.
                """, area, marketContext, additionalContext);
            case GROWTH_DRIVERS -> String.format("""
                You are Emira, an expert Dubai real estate analyst 
                for Emiratiyo Investments. Analyse the growth 
                drivers for %s in Dubai.
                
                Market Context: %s
                Additional User Context: %s
                
                Structure your response exactly like this: 
                OVERALL OUTLOOK: [Bullish/Neutral/Bearish + reason] 
                GOVERNMENT INITIATIVES: [relevant UAE/Dubai projects] 
                INFRASTRUCTURE: [transport, developments nearby] 
                DEMAND FACTORS: [who is buying and why] 
                SUPPLY PIPELINE: [new units coming, impact] 
                MARKET MOMENTUM: [from transaction data provided] 
                VERDICT: [2-3 sentence summary]
                """, area, marketContext, additionalContext);
            case RISK_ASSESSMENT -> String.format("""
                You are Emira, an expert Dubai real estate analyst 
                for Emiratiyo Investments. Provide a risk assessment 
                for investing in %s right now.
                
                Market Context: %s
                Additional User Context: %s
                
                Structure your response exactly like this: 
                RISK LEVEL: [Low/Medium/High] 
                OVERSUPPLY RISK: [assessment + evidence] 
                MACRO RISKS: [global factors that could impact] 
                LIQUIDITY RISK: [how easy to sell if needed] 
                REGULATORY RISK: [any UAE policy considerations] 
                MARKET TIMING: [good time to buy or wait?] 
                RISK SUMMARY: [2-3 sentences]
                """, area, marketContext, additionalContext);
            case MARKET_PULSE -> String.format("""
                You are Emira, an expert Dubai real estate analyst 
                for Emiratiyo Investments. Based on the transaction 
                data provided, give a market pulse reading for %s.
                
                Market Context: %s
                Additional User Context: %s
                
                Structure your response exactly like this: 
                TRANSACTION ACTIVITY: [volume vs Dubai average] 
                PRICE TREND: [direction and momentum] 
                OFF-PLAN VS READY: [split and what it signals] 
                BUYER DEMAND: [strong/moderate/weak + reason] 
                COMPARED TO DUBAI: [how area ranks overall] 
                HOT OR NOT: [single word verdict + one line reason]
                """, area, marketContext, additionalContext);
        };
    }
}
