package com.aicommerce.member.web.dto;

import com.aicommerce.member.domain.Member;

import java.time.Instant;

public record MemberResponse(
		Long id,
		String email,
		String name,
		String status,
		Instant createdAt) {

	public static MemberResponse from(Member member) {
		return new MemberResponse(
				member.getId(),
				member.getEmail(),
				member.getName(),
				member.getStatus().name(),
				member.getCreatedAt());
	}
}
