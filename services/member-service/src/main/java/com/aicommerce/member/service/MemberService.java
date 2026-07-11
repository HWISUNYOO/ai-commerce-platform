package com.aicommerce.member.service;

import com.aicommerce.member.domain.Member;
import com.aicommerce.member.exception.DuplicateEmailException;
import com.aicommerce.member.exception.InvalidCredentialsException;
import com.aicommerce.member.exception.NotFoundException;
import com.aicommerce.member.repository.MemberRepository;
import com.aicommerce.member.web.dto.MemberCreateRequest;
import com.aicommerce.member.web.dto.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Transactional
	public MemberResponse create(MemberCreateRequest request) {
		if (memberRepository.existsByEmail(request.email())) {
			throw new DuplicateEmailException(request.email());
		}
		Member member = Member.builder()
				.email(request.email())
				.passwordHash(passwordEncoder.encode(request.password()))
				.name(request.name())
				.build();
		return MemberResponse.from(memberRepository.save(member));
	}

	/** 이메일/비밀번호를 검증하고 회원 정보를 반환한다(불일치 시 401). */
	@Transactional(readOnly = true)
	public MemberResponse authenticate(String email, String rawPassword) {
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(InvalidCredentialsException::new);
		if (!passwordEncoder.matches(rawPassword, member.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}
		return MemberResponse.from(member);
	}

	@Transactional(readOnly = true)
	public MemberResponse get(Long id) {
		return memberRepository.findById(id)
				.map(MemberResponse::from)
				.orElseThrow(() -> new NotFoundException("Member", id));
	}

	@Transactional(readOnly = true)
	public List<MemberResponse> list() {
		return memberRepository.findAll().stream()
				.map(MemberResponse::from)
				.toList();
	}
}
