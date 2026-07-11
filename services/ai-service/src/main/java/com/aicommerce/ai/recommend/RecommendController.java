package com.aicommerce.ai.recommend;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class RecommendController {

	private final RecommendService recommendService;

	public RecommendController(RecommendService recommendService) {
		this.recommendService = recommendService;
	}

	@PostMapping("/recommend")
	public RecommendResponse recommend(@RequestBody @Valid RecommendRequest request) {
		return recommendService.recommend(request.query());
	}
}
