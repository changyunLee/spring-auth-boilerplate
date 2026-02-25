<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Spring Auth Boilerplate - Test Interface</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;800&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; }
        /* Tailwind로 구현 불가한 shimmer 효과 */
        .glass-panel::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 50%;
            height: 100%;
            background: linear-gradient(to right, transparent, rgba(255,255,255,0.05), transparent);
            transform: skewX(-20deg);
            transition: all 0.7s;
        }
        .glass-panel:hover::before { left: 200%; }
    </style>
</head>
<body
    class="min-h-screen bg-slate-50 flex items-center justify-center overflow-x-hidden"
    style="background-image: radial-gradient(circle at top right, #e0f2fe, transparent 40%), radial-gradient(circle at bottom left, #e2e8f0, transparent 40%); background-attachment: fixed;"
>

    <!-- 로고 -->
    <div class="absolute top-8 left-8 z-20">
        <h1 class="text-2xl font-extrabold text-slate-800">
            Auth<span class="text-blue-600">Boilerplate</span>
        </h1>
    </div>

    <!-- 메인 그리드 -->
    <div class="w-full max-w-4xl px-6 py-16 grid grid-cols-1 md:grid-cols-2 gap-8 z-10">

        <!-- Sign Up Panel -->
        <div class="glass-panel relative bg-white/70 backdrop-blur-xl border border-slate-200 rounded-2xl p-10 shadow-xl transition-all duration-300 hover:-translate-y-1 overflow-hidden">
            <h2 class="text-3xl font-extrabold mb-1 bg-gradient-to-br from-blue-600 to-indigo-600 bg-clip-text text-transparent">
                Sign Up
            </h2>
            <p class="text-slate-500 text-sm mb-8">새로운 계정을 생성하여 테스트해보세요.</p>

            <form id="signupForm" class="space-y-5">
                <div>
                    <label for="signupEmail" class="block mb-1.5 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                        Email
                    </label>
                    <input
                        type="email" id="signupEmail" placeholder="test@test.com" required
                        class="w-full px-4 py-3 bg-white border border-slate-200 rounded-lg text-slate-900 text-base transition-all duration-300 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 placeholder-slate-400 shadow-sm"
                    >
                </div>
                <div>
                    <label for="signupPassword" class="block mb-1.5 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                        Password
                    </label>
                    <input
                        type="password" id="signupPassword" placeholder="••••••••" required
                        class="w-full px-4 py-3 bg-white border border-slate-200 rounded-lg text-slate-900 text-base transition-all duration-300 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 placeholder-slate-400 shadow-sm"
                    >
                </div>
                <button
                    type="submit"
                    class="w-full py-3.5 bg-gradient-to-br from-blue-500 to-indigo-600 text-white rounded-lg text-base font-semibold cursor-pointer transition-all duration-300 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-indigo-500/40 active:translate-y-0 flex items-center justify-center gap-2"
                >
                    회원가입
                    <div id="signupLoader" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" style="display:none;"></div>
                </button>
            </form>

            <div id="signupResult" class="mt-6 p-4 bg-slate-100 rounded-lg font-mono text-sm text-emerald-700 min-h-[60px] break-all whitespace-pre-wrap border-l-4 border-emerald-500" style="display:none;"></div>
        </div>

        <!-- Login Panel -->
        <div class="glass-panel relative bg-white/70 backdrop-blur-xl border border-slate-200 rounded-2xl p-10 shadow-xl transition-all duration-300 hover:-translate-y-1 overflow-hidden">
            <h2 class="text-3xl font-extrabold mb-1 bg-gradient-to-br from-blue-600 to-indigo-600 bg-clip-text text-transparent">
                Login
            </h2>
            <p class="text-slate-500 text-sm mb-8">발급된 JWT 토큰을 확인하세요.</p>

            <form id="loginForm" class="space-y-4">
                <div id="login-fields">
                    <div>
                        <label for="loginEmail" class="block mb-1 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                            Email
                        </label>
                        <input
                            type="email" id="loginEmail" placeholder="test@test.com" required
                            class="w-full px-4 py-3 bg-white border border-slate-200 rounded-lg text-slate-900 text-base transition-all duration-300 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 placeholder-slate-400 mb-3 shadow-sm"
                        >
                    </div>
                    <div>
                        <label for="loginPassword" class="block mb-1 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                            Password
                        </label>
                        <input
                            type="password" id="loginPassword" placeholder="••••••••" required
                            class="w-full px-4 py-3 bg-white border border-slate-200 rounded-lg text-slate-900 text-base transition-all duration-300 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 placeholder-slate-400 shadow-sm"
                        >
                    </div>
                </div>

                <div id="2fa-fields" style="display: none;">
                    <div>
                        <label id="2fa-label" for="login2FA" class="block mb-1 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                            2차 인증 코드
                        </label>
                        <input
                            type="text" id="login2FA" placeholder="6자리 코드 입력"
                            class="w-full px-4 py-3 bg-white border border-slate-200 rounded-lg text-slate-900 text-base transition-all duration-300 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 placeholder-slate-400 shadow-sm"
                        >
                        <p id="2fa-guide" class="text-xs text-blue-600 mt-2"></p>
                    </div>
                </div>

                <button
                    type="submit"
                    class="w-full mt-2 py-3 bg-gradient-to-br from-blue-500 to-indigo-600 text-white rounded-lg text-base font-semibold cursor-pointer transition-all duration-300 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-indigo-500/40 active:translate-y-0 flex items-center justify-center gap-2"
                >
                    로그인
                    <div id="loginLoader" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" style="display:none;"></div>
                </button>
            </form>

            <div class="mt-4 flex flex-col items-center gap-3">
                <a href="/oauth2/authorization/google" class="w-full py-3 bg-white text-slate-700 rounded-lg text-base font-bold cursor-pointer transition-all duration-300 hover:-translate-y-0.5 hover:shadow-md flex items-center justify-center gap-2 border border-slate-300 hover:bg-slate-50">
                    <svg class="w-5 h-5" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/><path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/><path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/><path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/></svg>
                    Google 계정으로 로그인
                </a>
                
                <div class="flex gap-4 text-xs text-slate-500">
                    <button type="button" onclick="forgotPassword()" class="hover:text-slate-800 transition-colors">비밀번호 찾기</button>
                    <button type="button" onclick="resendVerification()" class="hover:text-slate-800 transition-colors">인증 메일 재발송</button>
                </div>
            </div>

            <div id="loginResult" class="mt-5 p-4 bg-slate-100 rounded-lg font-mono text-sm text-emerald-700 min-h-[60px] break-all whitespace-pre-wrap border-l-4 border-emerald-500" style="display:none;"></div>
        </div>
    </div>

    <!-- Action Modal -->
    <div id="actionModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 opacity-0 pointer-events-none transition-opacity duration-300" style="background-color: rgba(15, 23, 42, 0.4); backdrop-filter: blur(4px);">
        <div class="bg-white rounded-2xl p-8 w-full max-w-sm shadow-2xl transform scale-95 transition-transform duration-300 relative text-center" id="actionModalContent">
            <!-- Close Button -->
            <button type="button" onclick="closeModal()" class="absolute top-4 right-4 text-slate-400 hover:text-slate-700 transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>
            
            <h3 id="modalTitle" class="text-xl font-bold text-slate-800 mb-2"></h3>
            <p id="modalDesc" class="text-sm text-slate-500 mb-6"></p>

            <form id="modalForm" class="space-y-4 text-left">
                <div>
                    <label class="block mb-1 text-xs font-semibold text-slate-500 uppercase tracking-wider">Email</label>
                    <input type="email" id="modalEmail" placeholder="가입하신 이메일" required
                        class="w-full px-4 py-3 bg-white border border-slate-200 rounded-lg text-slate-900 text-base transition-all duration-300 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 placeholder-slate-400 shadow-sm">
                </div>
                <button type="submit" id="modalSubmitBtn" class="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-base font-semibold transition-colors flex justify-center items-center gap-2 shadow-sm">
                    요청하기
                    <div id="modalLoader" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" style="display:none;"></div>
                </button>
            </form>

            <div id="modalResult" class="mt-4 p-3 bg-slate-50 rounded-lg text-sm text-emerald-700 border border-emerald-200 hidden"></div>
        </div>
    </div>

    <script>
        const API_BASE_URL = '/api/auth';

        function showResult(elementId, message, isError = false) {
            const el = document.getElementById(elementId);
            el.style.display = 'block';
            el.className = isError
                ? 'mt-6 p-4 bg-red-50 rounded-lg font-mono text-sm text-red-700 min-h-[60px] break-all whitespace-pre-wrap border-l-4 border-red-500'
                : 'mt-6 p-4 bg-emerald-50 rounded-lg font-mono text-sm text-emerald-700 min-h-[60px] break-all whitespace-pre-wrap border-l-4 border-emerald-500';

            if (typeof message === 'object') {
                el.textContent = JSON.stringify(message, null, 2);
            } else {
                el.textContent = message;
            }
        }

        // Signup Handler
        document.getElementById('signupForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('signupEmail').value;
            const password = document.getElementById('signupPassword').value;
            const loader = document.getElementById('signupLoader');

            loader.style.display = 'inline-block';
            document.getElementById('signupResult').style.display = 'none';

            try {
                const response = await fetch(`${API_BASE_URL}/signup`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });

                const data = await response.text();

                if (response.ok) {
                    showResult('signupResult', data || '회원가입 성공!');
                } else {
                    let errData;
                    try { errData = JSON.parse(data); } catch(ex) { errData = data; }
                    showResult('signupResult', errData, true);
                }
            } catch (error) {
                showResult('signupResult', '네트워크 오류: ' + error.message, true);
            } finally {
                loader.style.display = 'none';
            }
        });

        let pending2FAEmail = null;

        // Login Handler
        document.getElementById('loginForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const loader = document.getElementById('loginLoader');
            loader.style.display = 'inline-block';
            document.getElementById('loginResult').style.display = 'none';

            try {
                if (pending2FAEmail) {
                    // 2FA 진행 단계
                    const code = document.getElementById('login2FA').value;
                    const response = await fetch(`${API_BASE_URL}/login/2fa`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ email: pending2FAEmail, code })
                    });

                    if (response.ok) {
                        const data = await response.json();
                        showResult('loginResult', `[Success] accessToken:\n\n${data.accessToken}`);
                        localStorage.setItem('token', data.accessToken);
                        window.location.href = '/dashboard.html';
                    } else {
                        const text = await response.text();
                        let errData; try { errData = JSON.parse(text); } catch(ex) { errData = text; }
                        showResult('loginResult', errData, true);
                    }
                } else {
                    // 1차 로그인 진행 단계
                    const email = document.getElementById('loginEmail').value;
                    const password = document.getElementById('loginPassword').value;

                    const response = await fetch(`${API_BASE_URL}/login`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ email, password })
                    });

                    if (response.ok) {
                        const data = await response.json();
                        if (data.status === 'REQUIRES_2FA') {
                            // 2FA 요구 응답
                            pending2FAEmail = data.email;
                            document.getElementById('login-fields').style.display = 'none';
                            document.getElementById('2fa-fields').style.display = 'block';
                            
                            if (data.twoFactorType === 'EMAIL') {
                                document.getElementById('2fa-guide').textContent = "이메일로 6자리 인증 코드가 발송되었습니다.";
                            } else {
                                const secret = data.twoFactorSecret;
                                if (secret) {
                                    const otpUrl = `otpauth://totp/AuthBoilerplate:${data.email}?secret=${secret}&issuer=AuthBoilerplate`;
                                    const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${encodeURIComponent(otpUrl)}`;
                                    document.getElementById('2fa-guide').innerHTML = `
                                        <div class="text-center">
                                            Google Authenticator 앱으로 아래 QR 코드를 스캔하세요.<br>
                                            <img src="${qrUrl}" alt="QR Code" class="mx-auto mt-3 mb-3 p-2 bg-white rounded-lg border border-slate-200">
                                            또는 키워드 직접 입력: <strong class="text-slate-800 bg-slate-100 px-2 py-1 rounded tracking-widest">${secret}</strong>
                                        </div>
                                    `;
                                } else {
                                    document.getElementById('2fa-guide').innerHTML = `
                                        <div class="text-center text-slate-500">
                                            Google Authenticator 앱을 열어<br>
                                            <strong class="text-slate-900">6자리 인증 코드</strong>를 입력하세요.
                                        </div>
                                    `;
                                }
                            }
                        } else {
                            // 정상 로그인 성공
                            showResult('loginResult', `[Success] accessToken:\n\n${data.accessToken}`);
                            localStorage.setItem('token', data.accessToken);
                            window.location.href = '/dashboard.html';
                        }
                    } else {
                        const text = await response.text();
                        let errData; try { errData = JSON.parse(text); } catch(ex) { errData = text; }
                        showResult('loginResult', errData, true);
                    }
                }
            } catch (error) {
                showResult('loginResult', 'Error: ' + error.message, true);
            } finally {
                loader.style.display = 'none';
            }
        });

        // ── 모달 UI 제어 ────────────────────────────────────────────────────────
        let currentModalAction = null; // 'resend' or 'forgot'

        function openModal(action) {
            currentModalAction = action;
            document.getElementById('modalEmail').value = '';
            document.getElementById('modalResult').style.display = 'none';
            document.getElementById('modalLoader').style.display = 'none';
            document.getElementById('modalSubmitBtn').disabled = false;
            
            const titleEl = document.getElementById('modalTitle');
            const descEl = document.getElementById('modalDesc');

            if (action === 'resend') {
                titleEl.textContent = '인증 메일 재발송';
                descEl.textContent = '가입하신 이메일 주소를 입력해주세요.';
            } else if (action === 'forgot') {
                titleEl.textContent = '비밀번호 찾기';
                descEl.textContent = '재설정 링크를 받을 이메일을 입력해주세요.';
            }

            const modal = document.getElementById('actionModal');
            const content = document.getElementById('actionModalContent');
            modal.classList.remove('opacity-0', 'pointer-events-none');
            content.classList.remove('scale-95');
            content.classList.add('scale-100');
        }

        function closeModal() {
            const modal = document.getElementById('actionModal');
            const content = document.getElementById('actionModalContent');
            modal.classList.add('opacity-0', 'pointer-events-none');
            content.classList.remove('scale-100');
            content.classList.add('scale-95');
        }

        // 모달 폼 전송 핸들러
        document.getElementById('modalForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('modalEmail').value;
            const loader = document.getElementById('modalLoader');
            const resultEl = document.getElementById('modalResult');
            const submitBtn = document.getElementById('modalSubmitBtn');

            loader.style.display = 'inline-block';
            submitBtn.disabled = true;
            resultEl.style.display = 'none';

            let endpoint = currentModalAction === 'resend' ? '/resend-verification' : '/forgot-password';

            try {
                const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email })
                });

                const msg = await response.text();
                resultEl.style.display = 'block';

                if (response.ok) {
                    resultEl.className = 'mt-4 p-3 bg-emerald-50 rounded-lg text-sm text-emerald-700 border border-emerald-200 break-all';
                    resultEl.textContent = msg;
                } else {
                    resultEl.className = 'mt-4 p-3 bg-red-50 rounded-lg text-sm text-red-700 border border-red-200 break-all';
                    let errData; try { errData = JSON.parse(msg).message || msg; } catch(ex) { errData = msg; }
                    resultEl.textContent = errData;
                }
            } catch (error) {
                resultEl.style.display = 'block';
                resultEl.className = 'mt-4 p-3 bg-red-50 rounded-lg text-sm text-red-700 border border-red-200 break-all';
                resultEl.textContent = "오류가 발생했습니다.";
            } finally {
                loader.style.display = 'none';
                submitBtn.disabled = false;
            }
        });

        // 헬퍼: 기존 함수는 openModal 연동으로 변경
        function resendVerification() {
            openModal('resend');
        }

        function forgotPassword() {
            openModal('forgot');
        }

        window.addEventListener('DOMContentLoaded', () => {
            // OAuth2 리다이렉트 처리: 해시(#)에서 토큰 추출
            if (window.location.hash) {
                const hashParams = new URLSearchParams(window.location.hash.substring(1));
                const authCode = hashParams.get('accessToken');
                const refreshToken = hashParams.get('refreshToken');
                if (authCode) {
                    localStorage.setItem('token', authCode);
                    localStorage.setItem('accessToken', authCode);
                    if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
                    
                    // 해시 제거 후 대시보드로 이동
                    window.history.replaceState(null, null, window.location.pathname);
                    window.location.replace('/dashboard.html');
                    return;
                }
            }

            if (localStorage.getItem('token')) {
                window.location.href = '/dashboard.html';
            }
        });
    </script>
</body>
</html>
