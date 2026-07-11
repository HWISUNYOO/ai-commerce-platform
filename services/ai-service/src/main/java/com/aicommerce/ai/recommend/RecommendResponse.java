package com.aicommerce.ai.recommend;

/**
 * 추천 응답.
 *
 * @param answer             LLM이 생성한 추천 답변
 * @param consideredProducts 추천 근거로 사용한(활성) 상품 수
 * @param backend            응답을 생성한 LLM 백엔드 이름
 */
public record RecommendResponse(String answer, int consideredProducts, String backend) {
}
