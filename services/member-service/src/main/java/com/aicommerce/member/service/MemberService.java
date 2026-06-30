package com.aicommerce.member.service;

import com.aicommerce.member.domain.Member;
import com.aicommerce.member.exception.DuplicateEmailException;
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
