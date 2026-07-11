package com.aicommerce.member.web.dto;

/** 로그인 성공 응답: JWT 토큰과 회원 정보. */
public record LoginResponse(String token, MemberResponse member) {
}
