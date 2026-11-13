# JavaScript de donativos

`adminDonativos.view.js` carga y renderiza la lista pendiente; `adminDonativos.js` consulta
`GET /api/donativos/admin/pendientes` y adapta sus datos para la tabla. El endpoint requiere
una sesión administrativa (Administrador o Root).

`resumenDonativos.view.js` usa `GET /api/donativos/pendientes/resumen`, que es público y devuelve
solo el total agregado de donativos pendientes, sin detalles por donativo.
