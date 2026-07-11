package com.aicommerce.ai.agent;

import com.aicommerce.ai.recommend.RecommendRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 에이전트 방식 쇼핑 어시스턴트. 모델이 도구를 스스로 호출(ReAct)해 답한다.
 * 한 번에 전체 상품을 프롬프트에 넣는 {@code /api/ai/recommend}(RAG-lite)와 대비되는 구현.
 */
@RestController
@RequestMapping("/api/ai")
public class AssistantController {

	private final ShoppingAgent agent;

	public AssistantController(ShoppingAgent agent) {
		this.agent = agent;
	}

	@PostMapping("/assistant")
	public AssistantResponse assistant(@RequestBody @Valid RecommendRequest request) {
		return agent.run(request.query());
	}
}
