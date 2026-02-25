const { chromium } = require('playwright');
const TARGET_URL = 'http://localhost:8080';

(async () => {
    console.log('🚀 Starting Playwright E2E Test: Signup and Duplication Check');
    const browser = await chromium.launch({ headless: true });
    const page = await browser.newPage();
    
    // Catch dialogs (alerts)
    let lastAlertMessage = '';
    page.on('dialog', async dialog => {
        lastAlertMessage = dialog.message();
        console.log(`[Alert Detected]: ${lastAlertMessage}`);
        await dialog.accept();
    });

    try {
        await page.goto(TARGET_URL);

        // Click "시작하기" button to open signup modal
        await page.click('button[onclick="openModal(\'signup\')"]');
        await page.waitForSelector('#signupEmail');

        // 1. Normal Signup
        const email = `testuser_${Date.now()}@example.com`;
        const password = `Test1234!`;
        
        console.log(`📝 Attempting to sign up with: ${email}`);
        await page.fill('#signupEmail', email);
        await page.fill('#signupPassword', password);
        
        await page.click('form[onsubmit="handleSignup(event)"] button[type="submit"]');
        
        // Wait for the alert to be processed
        await page.waitForTimeout(2000);
        
        if (!lastAlertMessage.includes('회원가입 성공')) {
            throw new Error(`Expected success alert but got: ${lastAlertMessage}`);
        }
        console.log('✅ Normal Signup Successful!');

        // 2. Duplicate Signup Check
        console.log(`📝 Attempting to sign up again with duplicated email: ${email}`);
        // Go back to signup form if we were redirected to login
        await page.click('button[onclick="renderSignup()"]');
        await page.waitForSelector('#signupEmail');
        
        await page.fill('#signupEmail', email);
        await page.fill('#signupPassword', password);
        
        await page.click('form[onsubmit="handleSignup(event)"] button[type="submit"]');
        
        // Wait for the alert to be processed
        await page.waitForTimeout(2000);
        
        if (!lastAlertMessage.includes('이미 가입되어 있는 이메일입니다.')) {
            throw new Error(`Expected duplication error alert but got: ${lastAlertMessage}`);
        }
        console.log('✅ Duplicate Email Error Captured Correctly!');

        // Take screenshot of the result state
        await page.screenshot({ path: 'e2e-signup-result.png', fullPage: true });
        console.log('📸 Screenshot saved to e2e-signup-result.png');
        
    } catch (e) {
        console.error('❌ E2E Test Failed:', e);
        process.exit(1);
    } finally {
        await browser.close();
    }
})();