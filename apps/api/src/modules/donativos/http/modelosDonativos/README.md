# Modelos de donativos

`donativo.model.js` consulta los donativos con estado `Pendiente de validación` y los ordena
por fecha ascendente para la HU44. La consulta incluye el nombre y los apellidos del usuario
donante; el endpoint administrativo devuelve el listado al panel web. El modelo también expone
un conteo agregado para el resumen público, sin retornar registros individuales.

`solicitudCfdi.model.js` consulta solicitudes con estado `Pendiente de atención`, estado inicial
definido por HU49. La migración `supabase/migrations/20261115000000_create_solicitud_cfdi.sql`
crea una solicitud única por donativo. Los endpoints administrativos de HU53 requieren sesión
de Administrador o Root; los datos fiscales y el enlace temporal de la constancia solo se
devuelven al consultar el detalle. El endpoint de lectura no modifica el estado de la solicitud.
Aplica la migración en Supabase antes de probar la bandeja; HU49 debe registrar las solicitudes
en esa tabla.

HU57 valida el PDF y XML del CFDI antes de guardarlos en el bucket privado `cfdi-documentos`.
La API compara UUID, RFC receptor y total del XML con la solicitud; el administrador confirma
visualmente que el PDF corresponda al comprobante. Los archivos se entregan al titular mediante
rutas autenticadas de cliente. La migración `supabase/migrations/20261119000000_add_cfdi_delivery_to_solicitud.sql`
agrega referencias y estado de correo. El envío usa `RESEND_API_KEY` y `MAIL_FROM`; si falla, los
archivos permanecen disponibles en la app y un administrador puede reintentar el correo.
