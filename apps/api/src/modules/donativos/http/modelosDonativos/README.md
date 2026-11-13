# Modelos de donativos

`donativo.model.js` consulta los donativos con estado `Pendiente de validación` y los ordena
por fecha ascendente para la HU44. La consulta incluye el nombre y los apellidos del usuario
donante; el endpoint administrativo devuelve el listado al panel web. El modelo también expone
un conteo agregado para el resumen público, sin retornar registros individuales.
