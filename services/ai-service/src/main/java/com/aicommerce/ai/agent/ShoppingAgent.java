package com.aicommerce.ai.agent;

import com.aicommerce.ai.agent.tool.AgentTool;
import com.aicommerce.ai.llm.LlmGateway;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 도구를 스스로 호출하는 쇼핑 어시스턴트(ReAct 루프).
 *
 * <p>매 턴 모델에게 "다음 행동을 JSON으로" 요청한다. 모델이 도구 호출을 고르면 우리가 실행해 관찰을
 * 작업 메모에 붙이고, 그 메모를 포함해 다시 모델에게 묻는다. 모델이 {@code final}을 내면 종료한다.
 * CLI 백엔드는 상태가 없으므로(single-shot) 매 턴 작업 메모를 프롬프트에 재구성해 넣는다.
 */
@Service
public class ShoppingAgent {

	private static final Logger log = LoggerFactory.getLogger(ShoppingAgent.class);
	private static final int MAX_STEPS = 5;

	private final Map<String, AgentTool> tools;
	private final String toolCatalog;
	private final LlmGateway llm;
	private final ObjectMapper mapper;

	public ShoppingAgent(List<AgentTool> toolList, LlmGateway llm, ObjectMapper mapper) {
		this.tools = toolList.stream()
				.collect(Collectors.toMap(AgentTool::name, Function.identity(), (a, b) -> a, LinkedHashMap::new));
		this.toolCatalog = toolList.stream()
				.map(t -> "- " + t.description())
				.collect(Collectors.joining("\n"));
		this.llm = llm;
		this.mapper = mapper;
	}

	public AssistantResponse run(String query) {
		String system = systemPrompt();
		StringBuilder memory = new StringBuilder();
		List<AgentStep> steps = new ArrayList<>();

		for (int i = 0; i < MAX_STEPS; i++) {
			String raw = llm.complete(userPrompt(query, memory), system);
			log.debug("Agent raw decision: {}", raw);

			JsonNode decision = parseDecision(raw);
			if (decision == null) {
				// LLM이 유효한 JSON을 안 냄 → 최종답으로 오해하지 말고 교정 후 재시도(자가 복구).
				memory.append("\n[시스템] 직전 출력이 유효한 JSON이 아니었다. 설명 없이 JSON 객체 하나만 출력하라.");
				continue;
			}

			String action = decision.path("action").asText("");
			if ("final".equals(action)) {
				log.info("Agent done in {} step(s), backend={}", steps.size(), llm.backendName());
				return new AssistantResponse(decision.path("answer").asText(""), steps, llm.backendName());
			}

			AgentTool tool = tools.get(action);
			JsonNode args = decision.path("args");
			if (tool == null) {
				memory.append("\n[시스템] 알 수 없는 action '").append(action)
						.append("'. 사용 가능: ").append(tools.keySet()).append(". 다시 결정하라.");
				steps.add(new AgentStep(action, args.toString(), "(알 수 없는 도구)"));
				continue;
			}

			String observation = tool.execute(args);
			steps.add(new AgentStep(action, args.toString(), observation));
			memory.append("\n[행동] ").append(action).append(" ").append(args.toString());
			memory.append("\n[관찰] ").append(observation);
			log.info("Agent step {}: action={}, args={}", i + 1, action, args);
		}

		// 스텝 소진 → 도구 없이 지금까지 정보로 최종 답만 요청
		String forced = userPrompt(query, memory)
				+ "\n\n이제 도구를 더 쓰지 말고, 지금까지 관찰한 상품만 근거로 최종 답변을 한국어로 작성하라.";
		String answer = llm.complete(forced, system);
		return new AssistantResponse(answer.strip(), steps, llm.backendName());
	}

	/** 모델 출력에서 JSON 객체를 추출/파싱한다. 유효한 JSON이 없으면 null(호출부에서 교정 재시도). */
	private JsonNode parseDecision(String raw) {
		int start = raw.indexOf('{');
		int end = raw.lastIndexOf('}');
		if (start < 0 || end <= start) {
			return null;
		}
		try {
			return mapper.readTree(raw.substring(start, end + 1));
		}
		catch (Exception e) {
			log.warn("Agent JSON parse failed: {}", e.getMessage());
			return null;
		}
	}

	private String systemPrompt() {
		return """
				너는 커머스 쇼핑 어시스턴트 에이전트다. 사용자 요청을 처리하려면 아래 도구를 호출할 수 있다.

				[도구]
				%s

				[규칙]
				- 매 턴 반드시 JSON 객체 하나만 출력한다. 인사·설명·코드펜스(```) 등 다른 텍스트를 절대 붙이지 마라.
				- 도구가 필요하면: {"action":"search_products","args":{"keyword":"키보드","max_price":50000}}
				- 정보가 충분하면 최종 답: {"action":"final","answer":"...추천과 이유..."}
				- 반드시 도구가 반환한 상품만 근거로 삼고, 목록에 없는 상품을 지어내지 마라.
				- 조건에 맞는 상품이 없으면 솔직히 없다고 답하라.
				- answer는 한국어로 간결하게, 추천 상품명·가격과 추천 이유를 담아라.
				""".formatted(toolCatalog);
	}

	private String userPrompt(String query, StringBuilder memory) {
		String mem = memory.length() == 0 ? "(아직 없음)" : memory.toString();
		return "사용자 요청: " + query + "\n\n[작업 메모]\n" + mem + "\n\n다음 행동을 JSON으로 출력하라.";
	}
}
