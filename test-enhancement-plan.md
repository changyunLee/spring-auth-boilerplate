# Test Enhancement Plan

## Goal
강력한 백엔드 통합 테스트(Edge Cases) 추가 및 Playwright를 활용한 프론트엔드 웹 화면 동작(E2E) 테스트를 구현하여 전체 시스템의 안정성을 검증한다.

## Tasks
- [x] Task 1: 백엔드 `AuthController` 엣지 케이스 테스트 추가 (잘못된 로그인, 정지된 계정 로그인 시도 등) → Verify: `./gradlew test --tests "*AuthControllerTest*"` 통과 확인
- [x] Task 2: 백엔드 `UserController` 엣지 케이스 테스트 추가 (내 정보 조회, 비밀번호 변경 실패 케이스 등) → Verify: `./gradlew test --tests "*UserControllerTest*"` 통과 확인
- [x] Task 3: E2E 테스트를 위한 백엔드 애플리케이션(Dev Server) 백그라운드 실행 → Verify: `curl http://localhost:8080/health` (또는 브라우저 접속) 200 OK 확인
- [ ] Task 4: Playwright 스크립트 작성 - 회원가입 및 이메일 중복 엣지 케이스 UI 테스트 → Verify: Playwright 스크립트 실행 및 스크린샷 결과 확인
- [ ] Task 5: Playwright 스크립트 작성 - 일반 로그인 및 2FA 로그인 UI 테스트 → Verify: Playwright 스크립트 실행 및 대시보드 리다이렉트 확인

## Done When
- [ ] 백엔드의 주요 컨트롤러(`Admin`, `Auth`, `User`)에 대한 엣지 케이스 테스트가 모두 추가되고 통과한다.
- [ ] Playwright를 통해 실제 브라우저 환경에서 회원가입, 일반 로그인, 2FA 로그인 시나리오가 성공적으로 자동화 테스트된다.
- [ ] 테스트 수행 과정에서 발생한 스크린샷이나 로그를 통해 UI의 정상 동작을 육안/코드로 검증할 수 있다.