const { spawnSync } = require("child_process");
const path = require("path");

const rootDir = path.resolve(__dirname, "..", "..");
const frontendDir = path.join(rootDir, "frontend");
const configPath = "playwright-audit.config.js";

function run(command, args) {
  const result = spawnSync(command, args, {
    cwd: frontendDir,
    stdio: "inherit",
    env: process.env
  });
  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

// Standard, reproducible runner from repo root:
// 1) ensure browser binaries exist
// 2) execute the profile audit suite
run("npx", ["playwright", "install", "chromium"]);
run("npx", ["playwright", "test", "--config", configPath]);
