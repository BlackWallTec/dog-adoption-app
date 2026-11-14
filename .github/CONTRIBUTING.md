# Contributing

## Commits

Use Conventional Commits: `type(scope): description`

Types: `feat`, `fix`, `docs`, `style`, `refactor`,
`test`, `build`, `ci`, `chore`, `revert`

Scopes: `api`, `web`, `android`, `contract`,
`perfiles`, `donativos`, `solicitudes`, `catalogo`,
`voluntarios`, `deps`

Examples:

- `feat(catalogo): add dog filters`
- `fix(api): validate adoption requests`
- `docs: update setup instructions`
- `ci: add pull request checks`

Use `!` for breaking changes, e.g.
`feat(contract)!: rename adoption status`.

## Branches

Use lowercase with hyphens (`-`).

- **Feature branches:** `feature/<module>-hu<xx>-<short-description>` (minimum 2 digits for `hu<xx>`, description optional)
  - Allowed modules: `solicitudes`, `catalogo`, `perfiles`, `donativos`, `voluntarios`
- **Other branches:** `<type>/<short-description>`
  - Allowed types: `bugfix`, `fix`, `hotfix`, `release`, `chore`, `docs`

Examples: `feature/catalogo-hu39-menu`, `feature/voluntarios-hu12`, `bugfix/login`, `chore/repo-conventions`, `release/1.0.0`.

## Pull requests

- Use a Conventional Commit as the PR title.
- Keep PRs focused and explain what changed and why with the provided template.
- Require review and passing CI checks before merging.
- Squash and merge into `main, develop, and release/`.

## Code reviews

Use Conventional Comments when useful:

`label (decoration): subject`

Labels: `praise`, `nitpick`, `suggestion`, `issue`,
`question`, `thought`, `chore`, `todo`, `note`.

Decorations: `blocking`, `non-blocking`, `if-minor`.

Example:
`suggestion (non-blocking): simplify this validation`

## Versioning

Follow Semantic Versioning: `MAJOR.MINOR.PATCH`.

- MAJOR: incompatible changes.
- MINOR: backward-compatible functionality.
- PATCH: backward-compatible fixes.

Before version 1.0.0, breaking changes may use a
minor version. Document breaking API changes and
the clients affected.

Android's `versionCode` must increase with each
released app version.
