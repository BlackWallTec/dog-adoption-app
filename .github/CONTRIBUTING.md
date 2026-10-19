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

Use `type/short-description`, lowercase with hyphens.

- `feature/` - new functionality
- `bugfix/` - bug fixes
- `docs/` - documentation
- `chore/` - maintenance and tooling

Examples: `feature/dog-catalog`, `bugfix/login`,
`chore/repo-conventions`.

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
