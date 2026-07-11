package com.aicommerce.member.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 로그인 성공 시 JWT를 발급하고, 요청의 토큰을 검증한다.
 * 서명 방식은 HMAC-SHA256(대칭키). 비밀키가 노출되면 위조 가능하므로 운영에선 시크릿을 안전히 주입한다.
 */
@Component
public class JwtProvider {

	private final SecretKey key;
	private final long expirationMinutes;

	public JwtProvider(
			@Value("${jwt.secret:dev-only-secret-change-me-please-32bytes-minimum!!}") String secret,
			@Value("${jwt.expiration-minutes:120}") long expirationMinutes) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMinutes = expirationMinutes;
	}

	/** 회원 정보를 담은 토큰을 발급한다(subject=회원 id). */
	public String createToken(Long memberId, String email, String name) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(String.valueOf(memberId))
				.claim("email", email)
				.claim("name", name)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
				.signWith(key)
				.compact();
	}

	/** 토큰 서명·만료를 검증하고 클레임을 반환한다(위조/만료 시 JwtException 발생). */
	public Claims parse(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	/** 토큰에서 회원 id를 꺼낸다. */
	public Long memberId(String token) {
		return Long.valueOf(parse(token).getSubject());
	}
}
