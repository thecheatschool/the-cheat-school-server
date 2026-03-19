package com.thecheatschool.thecheatschool.server.service.em;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecheatschool.thecheatschool.server.model.em.EmiraAnalysisRequest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmiraService {

    @Value("${emira.gemini.primary-key}")
    private String primaryKey;

    @Value("${emira.gemini.backup-key}")
    private String backupKey;

    private final ObjectMapper objectMapper;
    private final CircuitBreaker circuitBreaker;

    private static final String GEMINI_URL_TEMPLATE =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:streamGenerateContent?key=%s&alt=sse";

    public EmiraService(ObjectMapper objectMapper, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.objectMapper = objectMapper;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("emiraGemini");
    }

    @PostConstruct
    public void resetCircuitBreaker() {
        circuitBreaker.reset();
        log.info("Emira circuit breaker reset on startup");
    }

    @Async("taskExecutor")
    public void analyse(EmiraAnalysisRequest request, SseEmitter emitter) {
        log.info("=== EMIRA ANALYSE CALLED === type: {}, area: {}",
            request.getAnalysisType(), request.getArea());

        if (!circuitBreaker.tryAcquirePermission()) {
            sendError(emitter, "Emira is temporarily unavailable. Please try again.");
            return;
        }

        String prompt = buildPrompt(request);
        long startTime = System.currentTimeMillis();

        boolean success = callGemini(prompt, primaryKey, emitter);
        if (!success) {
            log.warn("Primary key failed, trying backup key");
            success = callGemini(prompt, backupKey, emitter);
        }

        if (!success) {
            circuitBreaker.onError(
                System.currentTimeMillis() - startTime,
                TimeUnit.MILLISECONDS,
                new RuntimeException("Both Gemini keys failed")
            );
            sendError(emitter, "Emira is temporarily unavailable. Please try again.");
        } else {
            circuitBreaker.onSuccess(
                System.currentTimeMillis() - startTime,
                TimeUnit.MILLISECONDS
            );
        }
    }

    private boolean callGemini(String prompt, String key, SseEmitter emitter) {
        try {
            String urlStr = String.format(GEMINI_URL_TEMPLATE, key);
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(180_000);

            String body = String.format(
                "{\"contents\":[{\"parts\":[{\"text\":%s}]}]}",
                objectMapper.writeValueAsString(prompt)
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            log.info("Gemini response status: {}", status);

            if (status != 200) {
                log.error("Gemini returned non-200: {}", status);
                return false;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if (data.isEmpty()) continue;
                        try {
                            JsonNode node = objectMapper.readTree(data);
                            JsonNode candidates = node.path("candidates");
                            if (candidates.isArray() && candidates.size() > 0) {
                                JsonNode parts = candidates.get(0)
                                    .path("content")
                                    .path("parts");
                                if (parts != null && parts.isArray() && parts.size() > 0) {
                                    String token = parts.get(0).path("text").asText();
                                    if (!token.isEmpty()) {
                                        emitter.send(SseEmitter.event().data(token));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.trace("Could not parse chunk: {}", data);
                        }
                    }
                }
            }

            emitter.complete();
            return true;

        } catch (Exception e) {
            log.error("Gemini call failed with key starting {}: {}",
                key.substring(0, 8), e.getMessage());
            return false;
        }
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
