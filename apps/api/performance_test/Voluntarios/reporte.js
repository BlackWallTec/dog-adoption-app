// Reporte en Markdown para carga.js y estres.js del módulo Voluntarios.
// Se guarda en performance_test/performance_reports/ (ignorado por git), por eso
// las pruebas se corren desde apps/api.

const P95_OBJETIVO = 200; // objetivo de rendimiento del equipo, en ms
const ERROR_MAXIMO = 0.01; // menos de 1 % de errores, igual que el umbral de carga.js

const ms = (v) => `${Math.round(v || 0)} ms`;
const pct = (v) => (v == null ? "-" : `${(v * 100).toFixed(2)}%`);

// Una fila por cada valor de la etiqueta (ruta en carga, scenario en estrés).
function tablaPorEtiqueta(metrics, etiqueta) {
  const prefijo = `http_req_duration{${etiqueta}:`;
  const claves = Object.keys(metrics)
    .filter((k) => k.startsWith(prefijo))
    .sort();
  if (claves.length === 0) return "_Sin desglose._\n";

  let tabla = `| ${etiqueta} | p50 | p95 | max | Errores |\n| :--- | :--- | :--- | :--- | :--- |\n`;
  for (const clave of claves) {
    const nombre = clave.slice(prefijo.length, -1);
    const d = metrics[clave].values;
    const err = metrics[`http_req_failed{${etiqueta}:${nombre}}`]?.values?.rate;
    tabla += `| ${nombre} | ${ms(d.med)} | ${ms(d["p(95)"])} | ${ms(d.max)} | ${pct(err)} |\n`;
  }
  return tabla;
}

export function generarReporte(data, { tipo, baseUrl, endpoints, etiqueta, notas = [] }) {
  const m = data.metrics;
  const d = m.http_req_duration?.values || {};
  const reqs = m.http_reqs?.values || {};
  const fallos = m.http_req_failed?.values || {};
  const p95 = d["p(95)"] || 0;
  const tasaError = fallos.rate || 0;
  // Cumple solo si es rápido Y casi no falla: una petición rechazada al instante
  // también es "rápida", así que el p95 solo no basta.
  const cumple = p95 < P95_OBJETIVO && tasaError < ERROR_MAXIMO ? "✅ cumple" : "❌ no cumple";
  const ahora = new Date();
  const dos = (n) => String(n).padStart(2, "0");
  const fecha = `${dos(ahora.getDate())}/${dos(ahora.getMonth() + 1)}/${ahora.getFullYear()} ${dos(ahora.getHours())}:${dos(ahora.getMinutes())}`;

  const md = `# Reporte k6 – Voluntarios (${tipo.toUpperCase()})

> **Entorno: LOCAL.** API corriendo en \`${baseUrl}\`. Estas pruebas NO se ejecutaron
> contra el despliegue por los límites de acciones y comandos de Vercel, Supabase
> y el rate limiter (Upstash).

- **Fecha:** ${fecha}
- **Endpoints:** ${endpoints.map((e) => `\`${e}\``).join(", ")}
- **VUs máximos:** ${m.vus_max?.values?.max ?? m.vus_max?.values?.value ?? "-"}
- **Objetivo de rendimiento:** p95 < ${P95_OBJETIVO} ms y errores < ${ERROR_MAXIMO * 100} % → **${cumple}** (p95 = ${ms(p95)}, errores = ${pct(tasaError)})
${notas.map((n) => `- ${n}`).join("\n")}

## Métricas globales

| Métrica | Valor |
| :--- | :--- |
| Latencia mínima | ${ms(d.min)} |
| Latencia mediana (p50) | ${ms(d.med)} |
| **Latencia p95** | **${ms(p95)}** |
| Latencia p99 | ${ms(d["p(99)"])} |
| Latencia máxima | ${ms(d.max)} |
| Peticiones por segundo | ${(reqs.rate || 0).toFixed(2)} req/s |
| Total de peticiones | ${reqs.count || 0} |
| Tasa de error global | ${pct(tasaError)} |

## Desglose por ${etiqueta === "scenario" ? "escalón" : "ruta"}

${tablaPorEtiqueta(m, etiqueta)}`;

  const archivo = `performance_test/performance_reports/voluntarios_${tipo}_${Date.now()}.md`;
  return { stdout: md, [archivo]: md };
}
