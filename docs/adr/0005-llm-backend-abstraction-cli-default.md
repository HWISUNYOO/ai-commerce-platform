# 5. LLM 백엔드를 추상화하고 기본값을 구독 기반 Claude Code CLI로 한다

- 상태: 채택(Accepted) — ADR-0002의 LLM 호출 부분을 구체화/일부 대체
- 날짜: 2026-06-29

## 배경

ADR-0002는 오케스트레이터가 Claude API를 직접 호출하도록 했다. 그러나 Claude
API(개발자 플랫폼)는 **토큰당 과금**이며 `ANTHROPIC_API_KEY`가 필요하고, 이는
보유 중인 Claude Code Pro Max 구독과 **별개로 청구**된다. 프로젝트 오너는 추가
비용을 원치 않는다.

확인된 사실(2026-06-29):

- `claude -p --output-format json` (headless 모드)는 Claude Code에 로그인된 인증을
  그대로 사용한다. Pro Max 구독으로 로그인돼 있으면 **API 토큰 과금 없이 구독으로
  실행**된다(구독 사용량 한도 내).
- 이 PC에는 독립 `claude`가 PATH에 없지만, VS Code 확장에 네이티브 바이너리가
  번들돼 있다: `~/.vscode/extensions/anthropic.claude-code-<ver>/resources/native-binary/claude.exe`
- headless 호출 결과는 JSON(`{"result": "...", "is_error": false, ...}`).

## 결정

LLM 호출을 **`LlmGateway` 인터페이스로 추상화**하고 구현체를 둘 둔다.

| 구현체 | 동작 | 비용 | 활성 조건 |
|---|---|---|---|
| `ClaudeCliGateway` (**기본**) | `claude.exe`를 서브프로세스로 호출, 프롬프트는 stdin으로 전달, `--output-format json` 파싱 | **구독 사용($0 추가)** | `llm.backend=cli` (기본/미설정 시) |
| `ClaudeApiGateway` | Spring `RestClient`로 Anthropic Messages API 호출 | 토큰당 과금, API 키 필요 | `llm.backend=api` |

- 백엔드 선택은 `@ConditionalOnProperty(name="llm.backend")`로, 미설정 시 CLI.
- `ClaudeCliGateway`는 `llm.cli.executable` 미지정 시 VS Code 확장의 번들 바이너리를
  자동 탐색한다(확장 버전 업데이트에 견디도록).
- 실행 파일 경로/모델/타임아웃은 `llm.cli.*`로, API 설정은 `llm.api.*`로 분리.

## 근거

1. **추가 비용 0** — 보유 구독만으로 전체 Agent를 끝까지 개발/운영 가능.
2. **설계 품질** — Strategy 패턴으로 LLM 백엔드를 교체 가능하게 분리하고,
   비용을 의식한 기본값(구독 기반 CLI)을 둔다.
3. ADR-0002의 하이브리드(개발/테스트는 Claude Code 위임)와 자연스럽게 통합된다 —
   이제 기획/분석/설계 단계도 동일한 CLI 게이트웨이를 공유한다.

## 결과

- 오케스트레이터는 기본적으로 로컬 Claude Code 구독에 의존한다(서버 무인 배포 시에는
  `llm.backend=api`로 전환하고 키를 주입). 구독 사용량 한도가 운영 제약으로 추가된다.
- 서브프로세스 호출이므로 stdin/stdout, 타임아웃, 종료코드 처리 등 프로세스 관리
  코드가 필요하다.
