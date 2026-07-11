package com.aicommerce.ai.llm.cli;

import com.aicommerce.ai.llm.LlmGateway;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 구독 기반 Claude Code CLI 백엔드(기본값, ADR-0005). {@code llm.backend}가 없거나 {@code cli}일 때 활성화.
 *
 * <p>{@code claude -p --output-format json} 를 자식 프로세스로 실행하고, 사용자 프롬프트를 stdin으로
 * 전달한 뒤 stdout JSON의 {@code result} 필드를 응답으로 반환한다. API 키 없이 구독 인증을 재사용하므로
 * 로컬 개발에 비용이 들지 않는다(배포 환경에서는 {@code llm.backend=api}로 교체).
 */
@Component
@ConditionalOnProperty(name = "llm.backend", havingValue = "cli", matchIfMissing = true)
@EnableConfigurationProperties(ClaudeCliProperties.class)
public class ClaudeCliGateway implements LlmGateway {

	private static final Logger log = LoggerFactory.getLogger(ClaudeCliGateway.class);

	private final ClaudeCliProperties props;
	private final ObjectMapper objectMapper;
	private volatile String resolvedExecutable;

	public ClaudeCliGateway(ClaudeCliProperties props, ObjectMapper objectMapper) {
		this.props = props;
		this.objectMapper = objectMapper;
	}

	@Override
	public String complete(String userPrompt, String systemPrompt) {
		List<String> command = new ArrayList<>();
		command.add(executable());
		command.add("-p");
		command.add("--output-format");
		command.add("json");
		if (systemPrompt != null && !systemPrompt.isBlank()) {
			command.add("--append-system-prompt");
			command.add(systemPrompt);
		}
		if (props.model() != null && !props.model().isBlank()) {
			command.add("--model");
			command.add(props.model());
		}

		try {
			Process process = new ProcessBuilder(command).start();

			// 프롬프트는 stdin으로 전달(명령행 인자 길이 제한·이스케이프 문제 회피).
			try (OutputStream stdin = process.getOutputStream()) {
				stdin.write(userPrompt.getBytes(StandardCharsets.UTF_8));
			}

			String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

			if (!process.waitFor(props.timeoutSeconds(), TimeUnit.SECONDS)) {
				process.destroyForcibly();
				throw new LlmCliException("Claude CLI timed out after " + props.timeoutSeconds() + "s");
			}
			if (process.exitValue() != 0) {
				throw new LlmCliException("Claude CLI exited with " + process.exitValue() + ": " + stderr.strip());
			}

			return parseResult(stdout);
		}
		catch (IOException e) {
			throw new LlmCliException("Failed to run Claude CLI (" + executable() + ")", e);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new LlmCliException("Interrupted while waiting for Claude CLI", e);
		}
	}

	@Override
	public String backendName() {
		return "claude-cli";
	}

	private String parseResult(String stdout) {
		try {
			JsonNode root = objectMapper.readTree(stdout);
			JsonNode result = root.get("result");
			if (result == null || result.isNull()) {
				throw new LlmCliException("Claude CLI response has no 'result' field: " + stdout.strip());
			}
			return result.asText();
		}
		catch (IOException e) {
			throw new LlmCliException("Failed to parse Claude CLI JSON output", e);
		}
	}

	/** 실행 파일 경로를 해석(1회)한다: 설정값 → VSCode 확장 자동 탐색 → PATH의 "claude". */
	private String executable() {
		String cached = resolvedExecutable;
		if (cached != null) {
			return cached;
		}
		String resolved = resolve();
		resolvedExecutable = resolved;
		log.info("Claude CLI executable resolved: {}", resolved);
		return resolved;
	}

	private String resolve() {
		if (props.executable() != null && !props.executable().isBlank()) {
			return props.executable();
		}
		Path discovered = discoverInVsCodeExtensions();
		return discovered != null ? discovered.toString() : "claude";
	}

	/** VSCode 확장(anthropic.claude-code-*) 설치 폴더에서 claude 실행 파일 중 최신 버전을 찾는다. */
	private Path discoverInVsCodeExtensions() {
		Path extensions = Path.of(System.getProperty("user.home"), ".vscode", "extensions");
		if (!Files.isDirectory(extensions)) {
			return null;
		}
		String exeName = System.getProperty("os.name", "").toLowerCase().contains("win")
				? "claude.exe" : "claude";
		try (Stream<Path> dirs = Files.list(extensions)) {
			return dirs
					.filter(Files::isDirectory)
					.filter(p -> p.getFileName().toString().startsWith("anthropic.claude-code-"))
					.sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
					.map(dir -> findExecutable(dir, exeName))
					.filter(java.util.Objects::nonNull)
					.findFirst()
					.orElse(null);
		}
		catch (IOException e) {
			log.warn("Failed to scan VSCode extensions for Claude CLI: {}", e.getMessage());
			return null;
		}
	}

	private Path findExecutable(Path extensionDir, String exeName) {
		try (Stream<Path> walk = Files.walk(extensionDir, 4)) {
			return walk.filter(p -> p.getFileName().toString().equals(exeName))
					.filter(Files::isRegularFile)
					.findFirst()
					.orElse(null);
		}
		catch (IOException e) {
			return null;
		}
	}
}
