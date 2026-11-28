# Reporte de Rendimiento k6 — Resolución de Solicitudes (ESTRES)

- **Fecha:** 11/24/2026, 15:13:20
- **Endpoint:** `POST http://localhost:3000/api/solicitudes/admin/solicitudes/f3c2672c-59b6-40e2-b95c-8b148e9cae03/resolucion`
- **Estado enviado:** `en_revision`
- **VUs Máximos:** 500

## Métricas Globales

| Métrica                          | Valor         |
| :------------------------------- | :------------ |
| **Latencia Mínima (min)**        | 0.00 s        |
| **Latencia Mediana (p50)**       | 0.02 s        |
| **Latencia p95**                 | **0.04 s**    |
| **Latencia Máxima (max)**        | 0.10 s        |
| **Peticiones por segundo (RPS)** | 2258.97 req/s |
| **Total de peticiones**          | 26992         |
| **Tasa de error global**         | 100.00%       |

## Desglose por Escalón (Punto de Quiebre)

| Escalón / Escenario | p95 Latencia | Tasa de Error |
| :------------------ | :----------- | :------------ |
| **recuperacion**    | 0.00 s       | 0.00%         |
| **vus_00050**       | 0.04 s       | 100.00%       |
| **vus_00100**       | 0.00 s       | 0.00%         |
| **vus_00300**       | 0.00 s       | 0.00%         |
| **vus_00500**       | 0.00 s       | 0.00%         |
