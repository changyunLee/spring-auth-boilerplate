# Spring Boot Authentication Boilerplate

이 프로젝트는 Java 17, Spring Boot 3, MariaDB 기반으로 구축되었으며, 자체 로그인 및 OAuth2 소셜 로그인, 이메일/Google OTP 기반 2단계 인증(2FA), 그리고 JWT 기반의 권한 관리 기능을 종합적으로 제공하는 풀스택 호환 보일러플레이트 앱입니다. 프론트엔드는 Tailwind CSS가 적용된 Vanilla JS 템플릿으로 작성되어 있습니다.

## 🚀 주요 기능 (Features)

### 1. 인증 및 인가 시스템
* **JWT 기반 Stateless 인증**: Access Token은 Response/Header로, Refresh Token은 HttpOnly 쿠키(+DB 저장) 형태로 안전하게 관리
* **OAuth2.0 소셜 로그인**: Google 로그인 구현 및 신규 가입 시 소셜 계정 데이터 연동
* **다중 2단계 인증(2FA)**:
  * Google Authenticator (TOTP)
  * Email OTP 인증
* **이메일 인증**: 회원가입 후 이메일 인증 코드를 통한 최종 계정 활성화
* **비밀번호 정책 강제**: 영대소문자 + 숫자 + 특수기호 최소 8자리 검증 (`@ValidPassword`)

### 2. 보안 및 트래픽 제어
* **계정 잠금/해제 기능**: 
  * 비밀번호 5회 실패 시 30분간 계정 자동 잠금(Lock)
  * 관리자에 의한 강제 정지(Suspend) 및 정지 사유 메모 기록
* **트래픽 및 무차별 대입(Brute-Force) 방어**: 
  * 로그인 엔드포인트에 **Bucket4j & Caffeine Cache** 기반 Rate Limiting(속도 제한) 적용

### 3. 모니터링 및 로깅
* **Spring Boot Actuator**: 시스템 헬스 모니터링
* **감사 로그(Audit Logs)**: 사용자의 로그인 성공/실패, 로그아웃, 설정 변경 등의 행위에 대한 IP Address 트래킹 및 DB 기록

### 4. 프론트엔드 통합 UI (Light Theme)
* 관리자 콘솔 (`admin.html`): 유저 관리(권한 변경, 계정 정지, 비밀번호 초기화), 트래픽/가입 지표 조회(Chart.js 통합), 감사 로그 모니터링
* 사용자 대시보드 (`dashboard.html`): 내 정보 수정(아바타, 이름), 비밀번호 변경, 2FA 설정
* 모달 기반 로그인 & 회원가입 폼 (`index.html`)

---

## ⚙️ 환경 설정 및 실행 (`application.yml`)

`src/main/resources/application.yml` 파일에서 로컬 데이터베이스 및 SMTP, OAuth2 환경 변수를 본인의 개발 환경에 맞춰 세팅하세요.

```bash
# Mac / Linux 실행
./gradlew bootRun

# Windows 실행
gradlew.bat bootRun
```
