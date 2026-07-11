package com.aicommerce.member.exception;

/** 이메일/비밀번호가 일치하지 않을 때 발생. */
public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("이메일 또는 비밀번호가 올바르지 않습니다.");
	}
}
