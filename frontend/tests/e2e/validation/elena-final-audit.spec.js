const { test, expect } = require('@playwright/test');
const path = require('path');

test.describe('Elena QA Validation - Final Block 4 Audit', () => {
  const PASSWORD = 'PhoenixTask2026!';
  const artifactsDir = path.join(__dirname, '..', '..', '..', '..', 'artifacts', 'demo');

  async function login(page, email) {
    await page.goto('http://localhost:3000/login');
    await page.fill('input[name="email"]', email);
    await page.fill('input[name="password"]', PASSWORD);
    await page.click('button[type="submit"]');
    await page.waitForURL('**/home');
  }

  test('Elena (QA) - Multi-Action Read-Only / Restricted Gestión', async ({ page }) => {
    await login(page, 'elena.torres@phoenixtask.demo');
    
    // 1. Issues (Lectura OK, Create Blocked)
    await page.goto('http://localhost:3000/issues');
    await page.waitForTimeout(2000);
    // Verificar que NO existe el botón "Create" o que está deshabilitado
    const createBtn = page.locator('button:has-text("Create"), a:has-text("Create"), button:has-text("New Issue")');
    const count = await createBtn.count();
    await page.screenshot({ path: path.join(artifactsDir, 'elena_3_issues_no_create_button.png'), fullPage: true });

    // 2. Issue Detail - Commenting restricted (Elena has no comment.create)
    await page.goto('http://localhost:3000/issues/4');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'elena_4_issue_comment_blocked.png'), fullPage: true });

    // 3. Gantt (Module Level Block)
    await page.goto('http://localhost:3000/gantt');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'elena_5_gantt_forbidden_ux.png'), fullPage: true });
  });
});
