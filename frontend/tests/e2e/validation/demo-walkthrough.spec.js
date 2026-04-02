const { test, expect } = require('@playwright/test');
const fs = require('fs');
const path = require('path');

test.describe('PhoenixTask Demo Walkthrough', () => {
  const PASSWORD = 'PhoenixTask2026!';
  const artifactsDir = path.join(__dirname, '..', '..', '..', '..', 'artifacts', 'demo');

  test.beforeAll(() => {
    if (!fs.existsSync(artifactsDir)) {
      fs.mkdirSync(artifactsDir, { recursive: true });
    }
  });
  
  async function login(page, email) {
    await page.goto('http://localhost:3000/login');
    await page.fill('input[name="email"]', email);
    await page.fill('input[name="password"]', PASSWORD);
    await page.click('button[type="submit"]');
    // Wait for the redirect to /home or any workspace page
    await page.waitForURL('**/home', { timeout: 15000 });
  }

  test('Sofia (Owner) - Full Walkthrough', async ({ page }) => {
    await login(page, 'sofia.ramos@phoenixtask.demo');
    
    // 1. Home
    await page.goto('http://localhost:3000/home');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'sofia_1_home.png'), fullPage: true });
    
    // 2. Issues
    await page.goto('http://localhost:3000/issues');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'sofia_2_issues.png'), fullPage: true });
    
    // 3. Issue Detail (PTW-3)
    await page.goto('http://localhost:3000/issues/4'); // Sofia's PTW-3 detail
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'sofia_3_issue_detail.png'), fullPage: true });
    
    // 4. Scrum
    await page.goto('http://localhost:3000/scrum');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'sofia_4_scrum.png'), fullPage: true });
    
    // 5. Kanban
    await page.goto('http://localhost:3000/kanban');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'sofia_5_kanban.png'), fullPage: true });
    
    // 6. OKR
    await page.goto('http://localhost:3000/okr');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'sofia_6_okr.png'), fullPage: true });
    
    // 7. Gantt
    await page.goto('http://localhost:3000/gantt');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'sofia_7_gantt.png'), fullPage: true });
    
    // 8. Insights
    await page.goto('http://localhost:3000/analytics');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'sofia_8_insights.png'), fullPage: true });
  });

  test('Elena (QA) - Read-Only & Access Control', async ({ page }) => {
    await login(page, 'elena.torres@phoenixtask.demo');
    
    // 1. Issues (Can view)
    await page.goto('http://localhost:3000/issues');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'elena_1_issues_view.png'), fullPage: true });
    
    // 2. Analytics (Forbidden UI)
    await page.goto('http://localhost:3000/analytics');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'elena_2_analytics_forbidden.png'), fullPage: true });
  });

  test('Pablo (Restricted) - Restricted Access', async ({ page }) => {
    await login(page, 'pablo.ruiz@phoenixtask.demo');
    
    // 1. OKR (Forbidden)
    await page.goto('http://localhost:3000/okr');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'pablo_1_okr_forbidden.png'), fullPage: true });
    
    // 2. Gantt (Forbidden)
    await page.goto('http://localhost:3000/gantt');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(artifactsDir, 'pablo_2_gantt_forbidden.png'), fullPage: true });
  });
});
