### 개발 워크플로우

| 스킬 | 역할 | 사용법 |
| :--- | :--- | :--- |
| plan | 요구사항 분석 후 단계별 구현 계획 작성 | /plan |
| tdd | TDD 워크플로우 강제 (인터페이스→테스트→구현) | /tdd |
| build-fix | 빌드 오류 자동 감지 및 수정 | /build-fix |
| code-review | 코드 리뷰 실행 | /code-review |
| refactor-clean | 데드코드 정리 및 리팩토링 | /refactor-clean |
| security-review | 보안 취약점 분석 | /security-review |
| test-coverage | 테스트 커버리지 측정 | /test-coverage |
| e2e | Playwright E2E 테스트 생성 및 실행 | /e2e |

### Git / 협업

| 스킬 | 역할 | 사용법 |
| :--- | :--- | :--- |
| commit-push-pr | 커밋 → 푸시 → PR 생성 자동화 | /commit-push-pr |
| quick-commit | 간단한 수정용 빠른 커밋 | /quick-commit |
| pull | git pull origin main 실행 | /pull |
| sync | Git pull + 문서 동기화 | /sync |
| worktree-start | 격리된 Git worktree 생성 | /worktree-start |
| worktree-cleanup | PR 완료 후 worktree 정리 | /worktree-cleanup |
| finishing-a-development-branch | 브랜치 완료 후 merge/PR/cleanup 옵션 제시 | /finishing-a-development-branch |

### 탐색 / 분석

| 스킬 | 역할 | 사용법 |
| :--- | :--- | :--- |
| explore | 반복 정제 코드베이스 탐색 | /explore |
| init-project | 프로젝트 초기화 설정 | /init-project |
| update-codemaps | 코드맵 업데이트 | /update-codemaps |
| update-docs | 문서 자동 업데이트 | /update-docs |
| show-setup | Claude Forge 설정 요약 표시 | /show-setup |

### 문서 / 동기화

| 스킬 | 역할 | 사용법 |
| :--- | :--- | :--- |
| sync-docs | prompt_plan.md, spec.md, CLAUDE.md 동기화 | /sync-docs |
| summarize | URL/파일/팟캐스트 요약 추출 | /summarize |
| humanizer | AI 작성 텍스트를 자연스럽게 변환 | /humanizer |

### 세션 관리

| 스킬 | 역할 | 사용법 |
| :--- | :--- | :--- |
| session-wrap | 세션 종료 전 정리 (문서 업데이트, 학습 포인트, 후속 작업) | /session-wrap |
| checkpoint | 작업 상태 저장/복원 | /checkpoint |
| handoff-verify | 핸드오프 + 자동 검증 통합 | /handoff-verify |
| next-task | 다음 작업 추천 | /next-task |
| strategic-compact | 컨텍스트 압축 타이밍 제안 | /strategic-compact |

### Agent / 오케스트레이션

| 스킬 | 역할 | 사용법 |
| :--- | :--- | :--- |
| orchestrate | Agent Teams 기반 병렬 오케스트레이션 | /orchestrate |
| verify-loop | 검증 루프 실행 | /verify-loop |
| eval | 평가 실행 | /eval |
| learn | 교훈 기록 + 자동화 제안 | /learn |
| suggest-automation | 반복 패턴 분석 → 자동화 기회 제안 | /suggest-automation |

### 보안 / 품질

| 스킬 | 역할 | 사용법 |
| :--- | :--- | :--- |
| security-pipeline | CWE Top 25 + STRIDE 자동 검증 | /security-pipeline |
| stride-analysis-patterns | STRIDE 위협 모델링 | /stride-analysis-patterns |
| security-compliance | SOC2/ISO27001/GDPR/HIPAA 컴플라이언스 가이드 | /security-compliance |
| web-checklist | 웹 보안 체크리스트 | /web-checklist |

### 기타

| 스킬 | 역할 | 사용법 |
| :--- | :--- | :--- |
| find-skills | 필요한 스킬 탐색/설치 | /find-skills |
| brainstorming | 구현 전 아이디어/요구사항 탐색 | /brainstorming |
| keybindings-help | 키보드 단축키 커스터마이징 | /keybindings-help |