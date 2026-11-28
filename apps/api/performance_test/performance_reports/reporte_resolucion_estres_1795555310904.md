# Reporte de Rendimiento k6 — Resolución de Solicitudes (ESTRES)

- **Fecha:** 11/24/2026, 15:21:50
- **Endpoint:** `POST http://localhost:3000/api/solicitudes/admin/solicitudes/bcca2f1c-2415-4c04-9e53-8085a34766e4/resolucion`
- **Estado enviado:** `en_revision`
- **VUs Máximos:** 500

## Métricas Globales

| Métrica                          | Valor       |
| :------------------------------- | :---------- |
| **Latencia Mínima (min)**        | 0.00 s      |
| **Latencia Mediana (p50)**       | 0.62 s      |
| **Latencia p95**                 | **4.71 s**  |
| **Latencia Máxima (max)**        | 11.94 s     |
| **Peticiones por segundo (RPS)** | 90.90 req/s |
| **Total de peticiones**          | 12742       |
| **Tasa de error global**         | 5.60%       |

## Desglose por Escalón (Punto de Quiebre)

| Escalón / Escenario | p95 Latencia | Tasa de Error |
| :------------------ | :----------- | :------------ |
| **recuperacion**    | 0.25 s       | 0.00%         |
| **vus_00050**       | 0.48 s       | 0.00%         |
| **vus_00100**       | 0.76 s       | 0.00%         |
| **vus_00300**       | 3.62 s       | 0.00%         |
| **vus_00500**       | 11.42 s      | 26.28%        |
