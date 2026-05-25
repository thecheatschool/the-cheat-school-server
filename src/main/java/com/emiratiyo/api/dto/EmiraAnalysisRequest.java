package com.emiratiyo.api.dto;

import lombok.Builder;

@Builder
public record EmiraAnalysisRequest(
    String area,
    AnalysisType analysisType,
    String marketContext,
    String additionalContext
) {
    public enum AnalysisType {
        PRICE_FORECAST,
        RENTAL_YIELD,
        GROWTH_DRIVERS,
        RISK_ASSESSMENT,
        MARKET_PULSE
    }
}
