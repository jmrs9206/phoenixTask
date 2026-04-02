# PhoenixTask Demo Runbook

This runbook is a practical guide to present PhoenixTask® smoothly with the demo dataset.

## Quick start

1. Start the environment
   - `./scripts/dev-up.sh --force`
2. Optional: reset demo data (clean slate)
   - `./scripts/dev-reset.sh`
3. Ensure demo attachments exist
   - `./scripts/dev-demo-assets.sh`
4. Open frontend
   - `http://localhost:3000`
5. Observe login hint
   - The login page now displays a `Demo Access` hint with the recommended user.

## Demo users (all use the same password)

Password for all demo users: `PhoenixTask2026!`

- **Owner/Admin**: `sofia.ramos@phoenixtask.demo`
  - Full access, best for main walkthrough.
- **Team Lead**: `mateo.cruz@phoenixtask.demo`
  - Strong permissions; good to show leadership view.
- **Contributor (Developer)**: `lucia.vega@phoenixtask.demo`
  - Day‑to‑day contributor; use for limited create/edit scenarios.
- **Support/Restricted**: `pablo.ruiz@phoenixtask.demo`
  - Useful to show permission‑aware UX.
- **QA/Read‑only**: `elena.torres@phoenixtask.demo`
  - Mostly read‑only, good for “view only” flows.

## Core demo flow (owner)

1. **Login** (Sofia)
2. **Home**
   - Quick status overview, recent activity
3. **Issues + Issue Detail**
   - Projects: PTW / CORE / CSOPS
   - Issues like PTW‑3/4, CORE‑2/3, CSOPS‑2/3/4
   - Show comments, attachments, dev activity
4. **Scrum**
   - Sprint 1 has commitment snapshot + burndown points
5. **Kanban**
   - WIP limits + flow policies + throughput/WIP age
6. **OKR**
   - Project‑scoped objectives and check‑ins
7. **Gantt**
   - Dependencies + baseline + critical path + resource load
8. **Insights**
   - Core metrics + Advanced Insights with honest definitions

## Demo flow for permissions (support user)

1. Login as `pablo.ruiz@phoenixtask.demo`
2. Open Issues and a single issue
3. Show that some actions are hidden or read‑only
4. Confirm there are no global error banners or noisy 403s

## Demo flow for read‑only (QA)

1. Login as `elena.torres@phoenixtask.demo`
2. Navigate to Scrum/Kanban/OKR/Gantt
3. Show read‑only experience and permission‑aware degradation

## Data highlights

- **Issues**
  - PTW‑3..7, CORE‑2..5, CSOPS‑2..4
- **Attachments**
  - PTW‑1: Onboarding‑Checklist.pdf
  - PTW‑2: UI‑Preview.png
  - CSOPS‑4: CS‑Health‑Review.txt
- **Git traceability**
  - Demo GitHub integration + links for PTW‑2 / CORE‑2
- **Scrum**
  - Sprint 1 commitment snapshot + burndown points
- **OKR**
  - “Increase onboarding completion”
  - “Stabilize platform readiness”

## Troubleshooting

- Backend not running: check `logs/backend.log`
- Frontend not running: check `logs/frontend.log`
- Missing demo data: run `./scripts/dev-reset.sh`
- Missing attachments: run `./scripts/dev-demo-assets.sh`
