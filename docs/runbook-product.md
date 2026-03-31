# PhoenixTask® Product Runbook

This is the functional map of PhoenixTask® for day‑to‑day product understanding.

## Core concepts
- **Tenant**: isolated workspace (demo tenant: `demo`).
- **Project**: primary execution container for work. Scrum/Kanban/Gantt/OKR are **project‑scoped**.
- **Issue hub**: single source of truth for work items, comments, attachments, activity, and dev activity.

## Modules (what they do)
- **Home**: internal landing. Orientation + quick access. Not a navigation section.
- **Issues**: create, triage, and track work. Issue detail is the hub (comments, attachments, activity, dev activity).
- **Projects**: define scope; manage memberships and delivery context.
- **Teams**: people organization and memberships.
- **Messages**: team/project/direct threads linked to workspace context.
- **Scrum**: sprints and backlog **per project**.
- **Kanban**: flow view **per project**.
- **Gantt**: timeline view **per project**.
- **OKR**: objectives, key results, check‑ins, and closures **per project** (project‑scoped OKR).
- **Insights**: analytics and delivery signals. Distinct from Home.
- **Settings**: workspace settings and operational controls.

## How methodologies fit together
- **Scrum/Kanban/Gantt/OKR are all project‑scoped**.
- Membership + permissions decide which project data a user can see.
- Issue detail is the cross‑methodology hub (execution + traceability).

## Navigation semantics
- Home is the post‑login landing and orientation.
- Insights is analytics/reporting and never mixed with Home content.

## Roles (demo)
- **Owner**: full access.
- **Team Leader**: project management + OKR create/update/close.
- **Developer**: execution‑focused; no OKR permissions.
- **Support / QA**: read‑only OKR.

For exact permissions and admin channels, see `docs/access-guide.md`.
