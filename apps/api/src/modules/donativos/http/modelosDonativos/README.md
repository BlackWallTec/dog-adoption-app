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
