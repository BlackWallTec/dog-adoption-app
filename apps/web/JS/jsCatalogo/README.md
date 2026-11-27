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

## Edición de la ficha (HU19C)

El botón **Editar** de cada fila abre `adminPerros.edit.js`, que permite cambiar todo lo
que se captura en el alta: nombre, sexo, edad, raza, tamaño, altura, **estado**,
descripción, esterilizado, vacunado, **características**, el texto de **cuidados
especiales** (columna `cuidado_especial`, hasta 500 caracteres, como en el alta de HU20) y
las **fotos y videos**.

- `perroForm.js` pinta, lee y valida el formulario y calcula qué cambió (`cambiosRespectoA`
  para las columnas, `cambiosEnRelaciones` para las características). No hace peticiones.
- `perroMultimedia.js` es la galería, un mosaico de fotos: la X de cada foto la marca para
  quitar (se atenúa con "Se quitará" y el botón pasa a **Deshacer**) y el mosaico punteado
  **Agregar**, o arrastrar archivos sobre la galería, suma archivos nuevos (etiqueta "Nueva";
  JPG, MP4 y MOV, hasta 20 por edición). Un resumen indica cuántos archivos quedarán, cuántos
  son nuevos y cuántos se quitan. Nada se aplica hasta guardar; si se cancela, no se toca nada.
- Al guardar, los archivos nuevos se suben al bucket `multimedia_perros` con
  `POST /api/catalogo/admin/solicitar-subida` y luego se manda un solo
  `PATCH /api/catalogo/admin/perros/:id` con **solo lo que cambió**:
  columnas de la ficha (incluidos `estado` y `cuidado_especial`), `caracteres` (lista
  completa de ids), `multimedia_agregar` (`[{ url, tipo }]`) y `multimedia_eliminar` (ids de `multimedia`).
- Si falla la subida no se guarda nada y el formulario conserva lo capturado; los archivos
  que ya se subieron no se vuelven a subir al reintentar.
- Al quitar una foto o video también se borra el archivo del bucket (si ese borrado falla,
  la ficha se guarda igual y el error queda en la consola del servidor).
- No se editan `id_perro`, `fecha_ingreso` ni `activo` (este último es de HU27). El orden de
  las fotos no se puede cambiar: la foto de la lista es la primera que devuelve la consulta.
