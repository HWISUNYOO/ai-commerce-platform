package com.aicommerce.ai.recommend;

import com.aicommerce.ai.llm.LlmGateway;
import com.aicommerce.ai.product.ProductClient;
import com.aicommerce.ai.product.ProductView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 자연어 요청을 받아 우리 카탈로그에서 상품을 추천하는 RAG 서비스.
 *
 * <p>흐름: (1) product-service에서 상품을 조회(Retrieval) → (2) 상품 목록을 프롬프트에 주입(Augmented)
 * → (3) LLM이 그 근거로 추천을 생성(Generation). 실제 데이터를 근거로 주입해 환각을 억제한다.
 */
@Service
public class RecommendService {

	private static final Logger log = LoggerFactory.getLogger(RecommendService.class);

	private static final String SYSTEM_PROMPT = """
			너는 커머스 쇼핑 어시스턴트다. 아래 규칙을 반드시 지켜라.
			- 반드시 [상품 목록]에 있는 상품만 추천한다. 목록에 없는 상품을 지어내지 마라.
			- 사용자 요청(가격대, 용도 등)에 맞는 상품을 1~3개 고르고, 각각 왜 추천하는지 한 줄로 설명한다.
			- 조건에 맞는 상품이 없으면 솔직히 "조건에 맞는 상품이 없습니다"라고 답한다.
			- 답변은 한국어로, 간결하게 한다.
			""";

	private final ProductClient productClient;
	private final LlmGateway llm;

	public RecommendService(ProductClient productClient, LlmGateway llm) {
		this.productClient = productClient;
		this.llm = llm;
	}

	public RecommendResponse recommend(String query) {
		List<ProductView> products = productClient.findAll().stream()
				.filter(p -> "ACTIVE".equalsIgnoreCase(p.status()))
				.toList();

		String catalog = toCatalog(products);
		String userPrompt = "사용자 요청: " + query + "\n\n[상품 목록]\n" + catalog;

		log.info("Recommend request: query='{}', products={}, backend={}",
				query, products.size(), llm.backendName());

		String answer = llm.complete(userPrompt, SYSTEM_PROMPT);
		return new RecommendResponse(answer, products.size(), llm.backendName());
	}

	private String toCatalog(List<ProductView> products) {
		if (products.isEmpty()) {
			return "(등록된 상품 없음)";
		}
		StringBuilder sb = new StringBuilder();
		for (ProductView p : products) {
			sb.append("- [id=").append(p.id()).append("] ")
					.append(p.name())
					.append(" | 가격 ").append(p.price()).append("원")
					.append(" | 재고 ").append(p.stockQuantity())
					.append(" | ").append(p.description() == null ? "" : p.description())
					.append('\n');
		}
		return sb.toString();
	}
}
