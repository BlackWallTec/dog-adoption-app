# Reporte de Rendimiento k6 — Resolución de Solicitudes (ESTRES)

- **Fecha:** 11/24/2026, 12:28:43
- **Endpoint:** `POST http://localhost:3000/api/solicitudes/admin/solicitudes/f3c2672c-59b6-40e2-b95c-8b148e9cae03/resolucion`
- **Estado enviado:** `en_revision`
- **VUs Máximos:** 50

## Métricas Globales

| Métrica                          | Valor       |
| :------------------------------- | :---------- |
| **Latencia Mínima (min)**        | 0.18 s      |
| **Latencia Mediana (p50)**       | 0.23 s      |
| **Latencia p95**                 | **0.59 s**  |
| **Latencia Máxima (max)**        | 9.90 s      |
| **Peticiones por segundo (RPS)** | 54.18 req/s |
| **Total de peticiones**          | 7596        |
| **Tasa de error global**         | 0.00%       |

## Desglose por Escalón (Punto de Quiebre)

| Escalón / Escenario | p95 Latencia | Tasa de Error |
| :------------------ | :----------- | :------------ |
| **recuperacion**    | 0.31 s       | 0.00%         |
| **vus_00010**       | 0.24 s       | 0.00%         |
| **vus_00025**       | 0.30 s       | 0.00%         |
| **vus_00040**       | 0.31 s       | 0.00%         |
| **vus_00050**       | 0.68 s       | 0.00%         |
