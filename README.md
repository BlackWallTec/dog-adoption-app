# Qariño Animal — Web/API

Dog adoption platform. This README covers the **Express API + admin panel** only. Android setup tbd

---

## Prerequisites

- **Node.js 24** (Vite+ will manage this for you if you use the global CLI)
- **Git** (GH CLI helps too for command line pull requests)

Vite+ (`vp`) is the only tooling you need for install, checks, tests, pre-commit hooks all that good stuff.

---

## Install Vite+

Install the global `vp` CLI first.

**macOS / Linux**

```bash
curl -fsSL https://vite.plus | bash
```

**Windows**

```powershell
irm https://vite.plus/ps1 | iex
```

After installation, open a new shell and verify:

```bash
vp --version
```

---

## Clone the repository

```bash
git clone <repo> 
cd dog-adoption-app
```

---

## Install dependencies

Run from the repository root:

```bash
vp install
```

This detects the workspace package manager (pnpm) and installs all web dependencies for the monorepo.

---

## Verify the setup

Run the repository-wide checks and tests:

```bash
vp check
vp test
```

`vp check` runs formatting, and linting. `vp test` runs the test suites. Both must pass before opening a PR.

---

## Run the API locally

The API package has a `dev` script that starts Express with Node’s watch mode:

```bash
vp run --filter api dev
```

This runs the Express API seperately from the admin panel so you can test that seperately. 

---

## Common commands

| Command | What it does |
|---|---|
| `vp install` | Install workspace dependencies |
| `vp check` | Format + lint + type-check the repo |
| `vp check --fix` | Auto-fix formatting issues |
| `vp test` | Run tests across the repo (--coverage flag adds what files are tested and to what percent)|
| `vp run --filter api dev` | Start the Express API in watch mode |

---

## Project structure (web/API only)

```
apps/api/
├─ openapi/          # One YAML per module (tbd)
├─ public/           # 
├─ views/            # 
└─ src/
   ├─ index.js       # Local listen entry
   ├─ app.js         # createApp() — mounts all modules
   ├─ shared/        # Auth, DB client, RBAC, notifications, exports
   └─ modules/       # perfiles, donativos, solicitudes, catalogo, voluntarios
```

---

## Need help?

- Vite+ docs: https://viteplus.dev/guide/
- Check existing PRs and issues for module-specific patterns.
- Ask in the Discord discussion thread before inventing a new folder or layer.
