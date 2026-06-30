package com.aicommerce.member.web;

import com.aicommerce.member.service.MemberService;
import com.aicommerce.member.web.dto.MemberCreateRequest;
import com.aicommerce.member.web.dto.MemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MemberResponse create(@RequestBody @Valid MemberCreateRequest request) {
		return memberService.create(request);
	}

	@GetMapping("/{id}")
	public MemberResponse get(@PathVariable Long id) {
		return memberService.get(id);
	}

	@GetMapping
	public List<MemberResponse> list() {
		return memberService.list();
	}
}
