# Changelog

Todas las versiones de Qariño Animal se documentan aquí.

**Esquema de versiones:** `0.<HUs completadas>.<parche>`. El número menor es la cantidad de historias de usuario (HU) MUST terminadas; el parche sube con cada corrección que no agrega HUs. La versión `1.0.0` se reserva para el MVP completo.

## [0.40.0] - 2026-11-20

Primer release del proyecto, al cierre del sprint MVP (6 nov – 20 nov 2026). Incluye la API, el dashboard web y la app Android, todos en la misma versión. Se completaron 40 de 42 HUs.

### Resumen

- **Solicitudes:** los adoptantes llenan la solicitud, suben su documentación y siguen el progreso; los administradores filtran, revisan candidatos y aprueban o rechazan.
- **Catálogo:** catálogo público de perritos con detalle, requisitos especiales y filtros; CRUD de fichas para administradores, incluida la desactivación.
- **Voluntariado:** los administradores crean, editan y cancelan jornadas y ven inscritos; los voluntarios consultan programas y cupos en tiempo real, se inscriben y cancelan.
- **Donativos:** registro de donativos económicos y datos fiscales (CSF), solicitud de CFDI y listas de pendientes para administradores.
- **Perfiles:** inicio de sesión web y móvil, creación de cuentas (root y cliente), validación por cookie y token Bearer, y permisos por rol.

### Historias incluidas

| Módulo       | HUs                                                                          |
| ------------ | ---------------------------------------------------------------------------- |
| Solicitudes  | HU01, HU02, HU03, HU04, HU05, HU06, HU08, HU11, HU13                         |
| Catálogo     | HU16, HU19A, HU19B, HU19C, HU19D, HU20, HU21, HU24                           |
| Voluntariado | HU31, HU32, HU33, HU34, HU37, HU38, HU39A, HU39B                             |
| Donativos    | HU44, HU45, HU46, HU49, HU53                                                 |
| Perfiles     | HU63, HU63-A, HU63-B, HU63-C, HU63-D, HU63-E, HU63-F, HU63-G, HU63-H, HU63-I |

### Pendiente (limitaciones conocidas)

- **HU57:** los administradores todavía no pueden subir el PDF/XML del CFDI.
- **HU63-J:** no hay confirmación de correo ni recuperación de contraseña.

[0.40.0]: https://github.com/BlackWallTec/dog-adoption-app/releases/tag/v0.40.0
