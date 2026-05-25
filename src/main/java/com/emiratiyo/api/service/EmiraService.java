package com.emiratiyo.api.service;

import com.emiratiyo.api.dto.EmiraAnalysisRequest;
import com.emiratiyo.api.entity.EmiraAnalysisEntity;
import com.emiratiyo.api.exception.AiAnalysisException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmiraService {

    @Value("${emira.gemini.primary-key}")
    private String primaryKey;

    @Value("${emira.gemini.backup-key}")
    private String backupKey;

    private final EmiraHistoryService emiraHistoryService;
    private final WebClient webClient;

    private static final String GEMINI_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=%s";

   
    @CircuitBreaker(name = "emiraGemini", fallbackMethod = "fallbackAnalyse")
    public Mono<String> analyse(EmiraAnalysisRequest request) {
        log.info("=== EMIRA ANALYSE CALLED === type: {}, area: {}",
                request.analysisType(), request.area());

        String prompt = buildPrompt(request);

        return callGeminiWithRetry(prompt, request);
    }

    private Mono<String> callGeminiWithRetry(String prompt, EmiraAnalysisRequest request) {
        return callGemini(prompt, primaryKey)
            .onErrorResume(e -> {
                log.warn("Primary key failed, trying backup key: {}", e.getMessage());
                return callGemini(prompt, backupKey);
            })
            .doOnSuccess(text -> {
                log.info("Gemini response received, length: {}", text.length());
                // Save to history asynchronously
                emiraHistoryService.save(
                        EmiraAnalysisEntity.builder()
                                .area(request.area())
                                .analysisType(request.analysisType().toString())
                                .responseText(text)
                                .build());
            })
            .doOnError(e -> log.error("Both Gemini keys failed or unexpected error: {}", e.getMessage()));
    }

    private Mono<String> callGemini(String prompt, String key) {
        String url = String.format(GEMINI_URL_TEMPLATE, key);
        
        Map<String, Object> body = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(root -> {
                    String text = root
                            .path("candidates").get(0)
                            .path("content")
                            .path("parts").get(0)
                            .path("text")
                            .asText();
                    if (text == null || text.isEmpty()) {
                        throw new AiAnalysisException("Gemini returned empty text");
                    }
                    return text;
                });
    }

    /**
     * Fallback method for Circuit Breaker
     */
    private Mono<String> fallbackAnalyse(EmiraAnalysisRequest request, Throwable t) {
        log.error("Circuit breaker opened for Emira Gemini: {}", t.getMessage());
        return Mono.error(new AiAnalysisException("Emira is temporarily unavailable. Please try again later."));
    }

    private String buildPrompt(EmiraAnalysisRequest request) {
        String area = request.area();
        String marketContext = request.marketContext();
        String additionalContext = request.additionalContext() != null ? request.additionalContext() : "";

        return switch (request.analysisType()) {
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