const { chromium } = require('playwright');
const { authenticator } = require('otplib');

const TARGET_URL = 'http://localhost:8080';

(async () => {
    console.log('🚀 Starting Playwright E2E Test: Login and 2FA Flow');
    const browser = await chromium.launch({ headless: true });
    
    // We need to grant permissions to avoid popup blockers or handle multiple pages
    const context = await browser.newContext();
    const page = await context.newPage();
    
    let pendingPromptResponse = null;

    page.on('dialog', async dialog => {
        const type = dialog.type();
        const msg = dialog.message();
        console.log(`[Dialog Detected]: Type=${type}, Message=${msg}`);
        
        if (type === 'prompt' && pendingPromptResponse) {
            console.log(`💬 Answering prompt with: ${pendingPromptResponse}`);
            await dialog.accept(pendingPromptResponse);
            pendingPromptResponse = null;
        } else {
            await dialog.accept();
        }
    });

    page.on('response', async res => {
        if (res.url().includes('/api/auth/login') && res.request().method() === 'POST') {
            try {
                const body = await res.json();
                console.log(`[Network Response] /api/auth/login : ${JSON.stringify(body)}`);
            } catch (e) {
                // Not JSON
            }
        }
    });

    try {
        await page.goto(TARGET_URL);

        // 1. Signup
        const email = `test2fa_${Date.now()}@example.com`;
        const password = `Test1234!`;
        
        console.log(`📝 Signing up: ${email}`);
        await page.click('button:has-text("시작하기")');
        await page.waitForSelector('#signupEmail', { state: 'visible' });
        await page.fill('#signupEmail', email);
        await page.fill('#signupPassword', password);
        await page.click('form[onsubmit="handleSignup(event)"] button[type="submit"]');
        
        // Wait for renderLogin to be called after alert
        await page.waitForSelector('#loginEmail', { state: 'visible' });
        
        // 2. Initial Login
        console.log('🔑 Logging in initially');
        await page.fill('#loginEmail', email);
        await page.fill('#loginPassword', password);
        await page.click('form[onsubmit="handleLogin(event)"] button[type="submit"]');

        // Wait for dashboard redirect
        await page.waitForURL('**/dashboard.html');
        console.log('✅ Logged in successfully. Redirected to dashboard.');

        // 3. Enable GOOGLE_OTP
        console.log('⚙️ Enabling GOOGLE_OTP');
        await page.waitForSelector('#btnChange2fa', { state: 'visible' });
        
        pendingPromptResponse = '3'; // GOOGLE_OTP is option 3
        
        // When clicking btnChange2fa, it opens a prompt, then a new popup window with the QR code.
        // We catch the new page to close it.
        const [popup] = await Promise.all([
            context.waitForEvent('page'),
            page.click('#btnChange2fa')
        ]);
        
        console.log('✅ Google OTP setup popup appeared. Closing it.');
        await popup.close();
        
        // 4. Logout
        console.log('🚪 Logging out');
        await page.click('button[onclick="logout()"]');
        await page.waitForURL('**/index.html');
        
        // 5. Login again to trigger 2FA
        console.log('🔑 Logging in again to test 2FA');
        await page.click('nav button:has-text("로그인")');
        await page.waitForSelector('#loginEmail', { state: 'visible' });
        await page.fill('#loginEmail', email);
        await page.fill('#loginPassword', password);
        await page.click('form[onsubmit="handleLogin(event)"] button[type="submit"]');
        
                // Wait for 2FA screen
                await page.waitForSelector('#otpCode', { state: 'visible' });
                console.log('🔒 2FA Screen triggered successfully');
        
                // 6. Get secret from network response (more reliable for test)
                let secret = null;
                const loginResponse = await page.waitForResponse(res => res.url().includes('/api/auth/login') && res.request().method() === 'POST');
                const resBody = await loginResponse.json();
                secret = resBody.twoFactorSecret;
                
                if (!secret) {
                    console.log("Secret not found in login response. Trying UI...");
                    const secretElement = page.locator('.font-mono.text-center');
                    const secretText = await secretElement.innerText({ timeout: 5000 });
                    secret = secretText.trim();
                }
                
                console.log(`🔑 Using Secret: ${secret}`);
                const token = authenticator.generate(secret);
                console.log(`🔢 Generated TOTP Token: ${token}`);        
        // 7. Submit 2FA
        await page.fill('#otpCode', token);
        await page.click('form[onsubmit="handle2faVerify(event)"] button[type="submit"]');
        
        // Wait for dashboard redirect
        await page.waitForURL('**/dashboard.html');
        console.log('✅ 2FA Login successful! Reached dashboard.');
        
        // Take screenshot of final dashboard
        await page.screenshot({ path: 'e2e-login-2fa-result.png', fullPage: true });
        console.log('📸 Screenshot saved to e2e-login-2fa-result.png');

    } catch (e) {
        console.error('❌ E2E Test Failed:', e);
        process.exit(1);
    } finally {
        await browser.close();
    }
})();