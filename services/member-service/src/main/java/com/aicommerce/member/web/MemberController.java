package com.aicommerce.member.web;

import com.aicommerce.member.security.JwtProvider;
import com.aicommerce.member.service.MemberService;
import com.aicommerce.member.web.dto.LoginRequest;
import com.aicommerce.member.web.dto.LoginResponse;
import com.aicommerce.member.web.dto.MemberCreateRequest;
import com.aicommerce.member.web.dto.MemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;
	private final JwtProvider jwtProvider;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MemberResponse create(@RequestBody @Valid MemberCreateRequest request) {
		return memberService.create(request);
	}

	/** 로그인: 자격 검증 후 JWT와 회원 정보를 반환한다. */
	@PostMapping("/login")
	public LoginResponse login(@RequestBody @Valid LoginRequest request) {
		MemberResponse member = memberService.authenticate(request.email(), request.password());
		String token = jwtProvider.createToken(member.id(), member.email(), member.name());
		return new LoginResponse(token, member);
	}

	/** 현재 토큰의 회원 정보를 반환한다(프론트 세션 복원용). 토큰이 유효하지 않으면 401. */
	@GetMapping("/me")
	public MemberResponse me(@RequestHeader("Authorization") String authorization) {
		String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
		return memberService.get(jwtProvider.memberId(token));
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
