package com.emiratiyo.api.dto;

import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Lightweight DTO for Emira analysis history list — excludes the full responseText
 * to keep list responses lean and Redis-efficient.
 */
@Builder
public record EmiraHistoryResponse(
    Long id,
    String area,
    String analysisType,
    LocalDateTime createdAt
) {}
