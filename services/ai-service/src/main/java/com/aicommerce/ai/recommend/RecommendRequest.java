package com.aicommerce.ai.recommend;

import jakarta.validation.constraints.NotBlank;

/** 자연어 추천 요청. */
public record RecommendRequest(@NotBlank String query) {
}
