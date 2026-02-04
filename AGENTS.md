# AGENTS.md



## Scope
- Applies to this directory and all subdirectories unless a deeper AGENTS.md exists.



## Repo Layout
- Frontend: ./frontend
    - frontend/src/components/: Page-level and shared components (AdminFlow/AdminTable/AdminConfig, etc.)
    - frontend/src/assets/: Static assets (icons, svg, images)
    - frontend/src/request/: API wrappers (api.js + request.js)
    - frontend/src/router/: Routing and Pinia store (router.js / pinia.js)
    - frontend/src/utils/: Utility helpers (date, string, constants, etc.)
    - frontend/src/style.css: Global styles + theme tokens (incl. dark theme)
    - frontend/src/App.vue: Root component (layout + sidebar control)
    - frontend/src/main.js: App entry (mount, Pinia, Router)
    - frontend/public/: Public static assets (if any)
- Backend: ./backend (DDD Architecture)
    - backend/ai-agent-trigger/: Interface/adapter layer (Controller, exception handling, HTTP entry)
    - backend/ai-agent-api/: Public API / facade layer (project conventions)
    - backend/ai-agent-domain/: Domain layer (entities, VO, domain services, rules)
    - backend/ai-agent-infrastructure/: Infrastructure layer (DAO/Mapper/DB/Redis/external integrations)
    - backend/ai-agent-types/: Shared DTOs/requests/responses/Result/PageResult
    - backend/ai-agent-app/: Application bootstrap & config (Spring Boot startup, wiring)
- MCP servers: ./mcp-server-*



## Tech Stack
- Frontend: Vue 3 + TailwindCSS + Vite + Pinia + vue-router + vue-flow (listed in frontend/package.json)
- Backend: Spring Boot 3 + Spring AI + OpenAI + PostgreSQL + MySQL + MyBatis + Redis + Docker (listed in backend/pom.xml)



## UI/UX Conventions
- Use Tailwind utility classes.
- Global styles & themes are in frontend/src/style.css (CSS variables + dark theme).
- Keep component JS in \<script setup> (no TS unless the file already uses TS).
- Do NOT introduce new UI component libraries. (unless the user explicitly states)



## Request/Response Handling
- Frontend requests: frontend/src/request/api.js + frontend/src/request/request.js.
- Backend admin APIs: backend/ai-agent-trigger/src/main/java/com/dasi/trigger/http.
- Request/response DTOs: backend/ai-agent-types/src/main/java/com/dasi/types/dto/request + backend/ai-agent-types/src/main/java/com/dasi/types/dto/response.
- Unified response format: backend/ai-agent-types/src/main/java/com/dasi/types/dto/result/Result.java.
- Paginated format: backend/ai-agent-types/src/main/java/com/dasi/types/dto/result/PageResult.java.
- If Result.code != 200: show Result.info (error).
- Axios errors: use normalizeError(err).message.



## Do/Don’t
- Do NOT change unrelated files.
- Do NOT add global styles unless explicitly required.
- Prefer minimal dependency additions.


