# Endpoints de Donativos

Prefijo: `/api/donativos`

**Auth:** las rutas con token usan `verificarToken`. En Android se manda `x-client: mobile` + `Authorization: Bearer <token>`; en web va en la cookie `token`. En las rutas de cliente el usuario sale del token (`req.user.uuid`), no de la URL; sin rol Cliente regresan 403.

- 401 `{ error: "No autenticado" }` o `{ error: "Token inválido o expirado" }`
- 403 `{ error: "No tienes permisos para esta acción" }` (cuando además pide rol)

## Cliente

### HU45 - Datos fiscales

**`GET /cliente/datos-fiscales`** · token + Cliente

- 200 `{ success, data: { id_usuario, rfc, razon_social, regimen_fiscal, codigo_postal, correo, correo_facturacion, constancia_url } }`
- 404 "No se encontró el usuario." · 500

**`PUT /cliente/datos-fiscales`** · token + Cliente

- Body JSON: `rfc`, `razon_social`, `regimen_fiscal`, `codigo_postal` (5 números), `correo_facturacion`
- 200 "Datos fiscales guardados."
- 400 "Datos incompletos." con `errores: { campo: mensaje }`
- 404 "No se encontró el usuario." · 500

**`PUT /cliente/datos-fiscales/constancia`** · token + Cliente

- Body: el archivo crudo (`application/pdf`, `image/jpeg` o `image/png`, máx. 10 MB)
- 200 "Constancia guardada." con `data: { constancia_url }`
- 400 "Sube tu constancia en PDF, JPG o PNG."
- 404 "No se encontró el usuario." · 500

### HU46 - Registrar donativo

**`POST /cliente/donativos?monto=&referencia=`** · token + Cliente

- `monto` > 0 con máx. 2 decimales, `referencia` opcional (máx. 200)
- Body: el comprobante crudo (PDF, JPG o PNG, máx. 10 MB)
- 201 "Donativo registrado." con `data: { id_donativo, estado }`
- 400 "Usuario inválido." / "Escribe un monto válido." / "La referencia es muy larga." / "Adjunta tu comprobante en PDF, JPG o PNG."
- 404 "No se encontró el usuario." · 500

### HU49 - Mis donativos y pedir CFDI

**`GET /cliente/mis-donativos`** · token + Cliente

- 200 `{ success, data: [ { id_donativo, monto, fecha, estado, cfdi_solicitado } ] }`
- 500

**`POST /cliente/mis-donativos/:idDonativo/cfdi`** · token + Cliente

- 201 "Solicitud de CFDI enviada"
- 400 "Donativo inválido" / "Tu donativo no esta validado" / "Ya existe una solicitud de CFDI para este donativo"
- 404 "No se encontró el donativo" · 500

### HU57 - Descargar CFDI

**`GET /cliente/cfdi`** · token + Cliente

- 200 `[ { id_solicitud_cfdi, uuid, fecha_carga, donativo: { id_donativo, monto, fecha } } ]`

**`GET /cliente/cfdi/:idSolicitud/:tipoArchivo`** · token + Cliente

- `tipoArchivo`: `pdf` o `xml`
- 200 el archivo
- 400 "Identificador de solicitud inválido." / "Tipo de archivo inválido."
- 404 "No se encontró el CFDI solicitado."

## Administrador (token + Administrador o Root)

**`GET /pendientes/resumen`** · sin auth

- 200 `{ totalPendientes }`

**`GET /admin/pendientes`** (HU44)

- 200 `[ { id_donativo, monto, fecha, estado, usuario: { nombre, apellidos } } ]`

**`PATCH /admin/pendientes/:idDonativo/aprobar`** (HU44)

- 200 `{ id_donativo, estado }`
- 400 id inválido · 409 "El donativo ya no está pendiente o no existe."

**`GET /admin/solicitudes-cfdi`** (HU53)

- 200 `[ { id_solicitud_cfdi, id_donativo, fecha_solicitud, estado, donante, requiere_revision } ]`

**`GET /admin/solicitudes-cfdi/:idSolicitud`** (HU53)

- 200 detalle con `donativo`, `datos_fiscales`, `cfdi`, `listo_para_emitir` y `motivos_revision`
- 400 id inválido · 404 "No se encontró la solicitud pendiente de CFDI."

**`POST /admin/solicitudes-cfdi/:idSolicitud/validar-cfdi`** (HU57)

- Multipart: `pdf` (máx. 10 MB) y `xml` (máx. 5 MB)
- 200 `{ valido, uuid, rfc_receptor, monto, donativo }`
- 400 archivos faltantes o XML mal formado · 404 solicitud no encontrada · 409 ya cargado o UUID duplicado · 413 archivo muy pesado · 415 formato incorrecto · 422 RFC o monto no coinciden, donativo sin validar

**`POST /admin/solicitudes-cfdi/:idSolicitud/cargar-cfdi`** (HU57)

- Igual que `validar-cfdi` más el campo `confirmar_pdf: "true"`
- 201 `{ almacenado, correo: { estado: "Enviado" | "Error" } }`

**`POST /admin/solicitudes-cfdi/:idSolicitud/reintentar-correo`** (HU57)

- 200 `{ almacenado, correo: { estado } }`
- 409 sin CFDI cargado, correo ya enviado o en proceso
