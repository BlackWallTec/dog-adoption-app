---
name: Analysis
about: Para la fase de analysis, aqui van todo el escrito y se ponen links para los
  artefactos necesarios
title: "[ANÁLISIS] [HU00]"
labels: ''
assignees: ''
type: Analysis

---

---
name: "Análisis de Historia de Usuario"
about: "Análisis funcional, reglas de negocio, flujos y riesgos de una HU"
title: "[ANÁLISIS] [HU00] "
labels: "analysis"
assignees: ""
---

## Historia relacionada

- **HU:** #
- **Responsable del análisis:** @

<!--
Enlaza aquí la Historia de Usuario padre.
El análisis debe partir de la HU y sus criterios de aceptación.
-->

---

## Objetivo del análisis

<!--
Explica brevemente qué se necesita comprender o definir antes de diseñar la solución.
No describas todavía cómo se implementará técnicamente.
-->

-

---

## Reglas de negocio

<!--
Condiciones, restricciones o reglas que debe respetar la funcionalidad.

Ejemplos:
- Solo se permiten solicitudes de usuarios autenticados.
- Una solicitud aprobada ya no puede modificarse.
- El usuario debe cargar todos los documentos obligatorios antes de continuar.
-->

-

---

## Precondiciones

<!--
¿Qué debe ser verdadero antes de que pueda comenzar este flujo?
-->

-

---

## Postcondiciones

<!--
¿Qué debe ser verdadero después de completar correctamente el flujo?
-->

-

---

## Flujo principal

<!--
Describe el comportamiento esperado desde el inicio hasta completar exitosamente la HU.
Mantén el flujo independiente de detalles técnicos de implementación.
-->

1.
2.
3.

---

## Flujos alternos y casos de borde

<!--
Considera errores, información inválida, acciones canceladas,
falta de datos, problemas de conexión, estados previos, etc.
-->

### Flujo alterno 1
- **Condición:**
- **Comportamiento esperado:**

### Flujo alterno 2
- **Condición:**
- **Comportamiento esperado:**

---

## Datos involucrados

<!--
No es necesario definir todavía clases, tablas o modelos técnicos.
Identifica únicamente qué información necesita, consulta o modifica la HU.
-->

### Entradas
-

### Información consultada
-

### Información generada o modificada
-

---

## Dependencias y restricciones

<!--
Otras HU, módulos, servicios externos, decisiones pendientes,
permisos, autenticación, restricciones del socio, etc.
-->

- **Dependencias:**
- **Restricciones:**

---

## Spike / Investigación

<!--
Un Spike solo debe realizarse cuando existe una incertidumbre que no puede
resolverse razonablemente con el conocimiento actual del equipo.
-->

- [ ] No se requiere Spike
- [ ] Se requiere Spike

### Si se requiere

- **Pregunta a resolver:**
- **Motivo de la investigación:**
- **Resultado esperado:**
- **Conclusión:**
- **Issue / documentación relacionada:**

---

## Diagrama de actividades

<!--
Representa el flujo principal, decisiones relevantes y caminos alternos.
-->

- **Enlace al diagrama:**

---

## Decisiones y conclusiones del análisis

<!--
Resume las decisiones importantes obtenidas durante esta fase.
Estas conclusiones deben proporcionar suficiente información para iniciar Diseño.
-->

-

---
## Consideraciones de seguridad y privacidad

<!--
Completar únicamente si la HU maneja autenticación, autorización,
datos personales, documentos, información sensible o acciones restringidas.
Escribe "No aplica" cuando corresponda.
-->

- **Datos sensibles involucrados:**
- **Quién puede consultar/modificar la información:**
- **Riesgos identificados:**
- **Restricciones relevantes:**

## Checklist de finalización

- [ ] La HU y sus criterios de aceptación fueron revisados.
- [ ] Las reglas de negocio están identificadas.
- [ ] Las precondiciones y postcondiciones están definidas.
- [ ] El flujo principal está documentado.
- [ ] Los principales flujos alternos y casos de borde están considerados.
- [ ] Los datos involucrados están identificados.
- [ ] Las dependencias y restricciones están identificadas.
- [ ] Se determinó si es necesario realizar un Spike.
- [ ] El Spike fue concluido, en caso de ser necesario.
- [ ] El diagrama de actividades está terminado.
- [ ] No existen preguntas abiertas que bloqueen la fase de Diseño.
