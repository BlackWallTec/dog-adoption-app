# Flujo de Trabajo en Git y PRs

Las convenciones se definen en `.github/CONTRIBUTING.md` y se validan en dos niveles:

- **Localmente**, con los hooks de `.vite-hooks/`:
  - `commit-msg` — valida el formato Conventional Commits.
  - `pre-commit` — valida el nombre de la rama y ejecuta `vp staged` (`vp check --fix` sobre archivos preparados).
  - `pre-push` — rechaza el push si el nombre de la rama no es válido.
- **En GitHub Actions**:
  - `pr-conventions.yml` — valida el título del PR y el nombre de la rama.
  - `ci.yml` — ejecuta `vp check` y pruebas (y el build de Android cuando cambia `apps/android`) en cada PR y en cada push a `develop`.

## Convención de Ramas

Usa minúsculas separadas por guiones (`-`):

- **Ramas de Historia de Usuario:** `feature/<modulo>-hu<xx>-<descripcion-corta>`
  - Módulos permitidos: `solicitudes`, `catalogo`, `perfiles`, `donativos`, `voluntarios`.
  - `hu<xx>` requiere al menos 2 dígitos (la descripción corta es opcional).
  - Ejemplos: `feature/catalogo-hu39-menu`, `feature/voluntarios-hu12`.
- **Otras ramas:** `<tipo>/<descripcion-corta>`
  - Tipos permitidos: `bugfix`, `fix`, `hotfix`, `release`, `chore`, `docs`.
  - Ejemplos: `bugfix/login`, `docs/gitbooks-setup`, `release/1.0.0`.
- `main`, `master` y `develop` están exentas de la validación.

## Conventional Commits

Todos los commits y títulos de Pull Request deben seguir el formato:

```text
type(scope): descripción en imperativo
```

- **Tipos (`type`):** `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `build`, `ci`, `chore`, `revert`
- **Alcances (`scope`, opcional):** `api`, `web`, `android`, `contract`, `perfiles`, `donativos`, `solicitudes`, `catalogo`, `voluntarios`, `deps`
- **Cambios incompatibles (Breaking Changes):** Agrega `!` antes de `:`, por ejemplo `feat(contract)!: rename adoption status`.

Ejemplos válidos:

- `feat(catalogo): add dog filters`
- `fix(api): validate adoption requests`
- `docs(android): document hilt network module`
- `ci: add pull request checks`

## Pull Requests y Revisiones

1. Asegúrate de que `vp check` y `vp run -r test` pasen localmente.
2. Usa un título con formato Conventional Commit y completa la plantilla de `.github/pull_request_template.MD`.
3. Se requiere revisión aprobada y CI en verde antes de fusionar.
4. Se realiza **Squash and merge** hacia `main`, `develop` y `release/*`.
5. En revisiones de código se recomienda usar Conventional Comments (`label (decoration): comentario`):
   - Etiquetas: `praise`, `nitpick`, `suggestion`, `issue`, `question`, `thought`, `chore`, `todo`, `note`.
   - Decoraciones: `blocking`, `non-blocking`, `if-minor`.
   - Ejemplo: `suggestion (non-blocking): simplificar esta validación`.

## Versionado

Se sigue Semantic Versioning (`MAJOR.MINOR.PATCH`). Antes de `1.0.0`, los cambios incompatibles pueden publicarse como versión menor. El `versionCode` de Android debe incrementarse con cada versión publicada de la app.
