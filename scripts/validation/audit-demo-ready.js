const http = require("http");

async function request(path, method, body, cookie) {
  return new Promise((resolve) => {
    const options = {
      hostname: "localhost",
      port: 8080,
      path,
      method,
      headers: {
        "Content-Type": "application/json",
        "X-Tenant-Code": "demo"
      },
      timeout: 5000
    };
    if (cookie) options.headers["Cookie"] = cookie;
    
    const req = http.request(options, (res) => {
      let data = "";
      res.on("data", (chunk) => data += chunk);
      res.on("end", () => resolve({ status: res.statusCode, headers: res.headers, body: data }));
    });
    req.on("error", (e) => resolve({ status: 500, error: e.message }));
    if (body) req.write(JSON.stringify(body));
    req.end();
  });
}

async function login(email) {
  const res = await request("/api/auth/login", "POST", {
    tenantCode: "demo",
    email,
    password: "PhoenixTask2026!"
  });
  return res.headers["set-cookie"] ? res.headers["set-cookie"][0].split(";")[0] : null;
}

const MODULES = [
  { name: "Issues (GET)", path: "/api/workspace/issues", method: "GET" },
  { name: "Issues (POST - Block Test)", path: "/api/workspace/issues", method: "POST", body: { title: "Test", status: "OPEN" } },
  { name: "Scrum (GET)", path: "/api/workspace/scrum/projects/1/sprints", method: "GET" },
  { name: "Kanban (GET)", path: "/api/workspace/kanban/projects", method: "GET" },
  { name: "OKR (GET)", path: "/api/workspace/okr/projects/1/objectives", method: "GET" },
  { name: "Gantt (GET)", path: "/api/workspace/gantt/projects", method: "GET" },
  { name: "Analytics (GET)", path: "/api/workspace/analytics/summary", method: "GET" },
  { name: "Me (Permissions)", path: "/api/workspace/permissions/me", method: "GET" }
];

async function auditProfile(name, email) {
  console.log(`\n--- Auditing Profile: ${name} (${email}) ---`);
  const cookie = await login(email);
  if (!cookie) {
    console.log("FAILED: Could not login");
    return;
  }
  
  const results = [];
  for (const mod of MODULES) {
    const res = await request(mod.path, mod.method, mod.body, cookie);
    results.push({ module: mod.name, status: res.status });
    console.log(`[${res.status}] ${mod.name}`);
  }
  return results;
}

(async () => {
  await auditProfile("Sofia (Owner)", "sofia.ramos@phoenixtask.demo");
  await auditProfile("Elena (QA/Read-Only)", "elena.torres@phoenixtask.demo");
  await auditProfile("Pablo (Restricted)", "pablo.ruiz@phoenixtask.demo");
})();
