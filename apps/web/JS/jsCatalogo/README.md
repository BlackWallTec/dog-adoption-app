# JavaScript de catálogo

`adminPerros.view.js` abre el panel de perros en `/admin/perros` y pinta el listado de
HU19B. `adminPerros.fila.js` arma cada fila y decide si la respuesta es un listado.
La consulta es `GET /api/catalogo/admin/perros`.

Ese endpoint lee los perros con `activo = true`, ordenados por `fecha_ingreso`
descendente, e incluye raza, tamaño y multimedia. Responde `{ success: true, data }`
o `{ success: false, message }`. La lectura usa la clave anónima. Esta historia no
escribe en Supabase y no implementa login ni permisos.

## Fila

De cada perro se muestra:

- Foto: la primera URL de `multimedia`. Si no hay URL, o si la imagen no carga, se usa
  `/public/favicon.svg`.
- Nombre, raza (`raza.nombre`), edad (`edad_aprox`, en años), tamaño (`tamano.nombre`)
  y estado.
- Un dato vacío se muestra como **No especificado**. No se rellena con Mestizo, Mediano,
  Disponible ni otro valor supuesto.
- El contador describe el total de la última consulta exitosa. En cero dice que no hay
  perros registrados.

## Carga y error

Mientras la consulta no responde, la tabla muestra el texto de carga y no conserva filas
anteriores. Si la respuesta falla, no es JSON o no trae un arreglo, no se pintan perros,
el contador queda en 0 y el aviso usa la clase compartida `estado-error` con el botón
`btn-primary` **Reintentar**.

## Fuera de esta historia

La búsqueda, los filtros, la paginación interactiva, el registro, la edición y la
desactivación se ven en la misma pantalla porque pertenecen a otras historias. HU19B
solo consume el listado.
