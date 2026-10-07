---
name: Design
about: Template para organizer la fase de diseño como sub-issue
title: "[DISEÑO] HU00"
labels: ''
assignees: ''
type: Design

---

---
name: "Diseño de Historia de Usuario"
about: "Diseño UI/UX y técnico de una Historia de Usuario"
title: "[DISEÑO] [HU00] "
labels: "design"
assignees: ""
---

## Historia relacionada

- **HU:** #
- **Responsable del diseño:** @

<!--
Este diseño debe partir de las conclusiones de la fase de Análisis.
No repetir reglas de negocio, flujos o casos de borde ya documentados ahí.
-->

---

## Objetivo del diseño

<!--
Describe brevemente qué solución se propone para satisfacer la HU.
Enfócate en la solución, no en volver a explicar el problema.
-->

-

---

## Diseño UI/UX

### Referencia visual

- **Figma / prototipo:**

### Pantallas o componentes de UI afectados

<!--
Ejemplos:
- Pantalla de detalle de solicitud
- Modal de carga de documentos
- Componente de estado de carga
-->

-

### Navegación

<!--
Indica de dónde viene el usuario, qué acciones puede realizar
y hacia dónde puede navegar.
-->

-

### Estados de interfaz

<!--
Incluye solamente los estados relevantes para esta HU.
-->

- [ ] Estado inicial
- [ ] Loading
- [ ] Success
- [ ] Error
- [ ] Empty
- [ ] Otro:

**Descripción de estados relevantes:**

-

---

## Diseño técnico

### Componentes afectados

<!--
Identifica los componentes o capas involucradas.
No es necesario listar cada clase si todavía no aporta valor.
-->

- **UI / Screen / Composable:**
- **ViewModel:**
- **Repository:**
- **Servicio / API / Data Source:**
- **Otros:**

---

## Flujo de datos

<!--
Describe brevemente cómo viaja la información a través del sistema.

Ejemplo:
UI → ViewModel → Repository → API → Repository → ViewModel → UI
-->

-

---

## API / Backend

<!--
Completar solamente cuando la HU interactúe con backend.
-->

- **Endpoint:**
- **Método:**
- **Datos enviados:**
- **Datos recibidos:**
- **Errores relevantes:**

- [ ] No aplica

---

## Persistencia y modelo de datos

<!--
Indica si se consulta, crea o modifica información persistente.
Incluye cambios necesarios al modelo de datos, si existen.
-->

- **Datos consultados:**
- **Datos creados/modificados:**
- **Cambios al modelo:**

- [ ] No aplica

---

## Manejo de errores

<!--
Define cómo responderá técnicamente la aplicación ante los errores
identificados durante Análisis.
-->

| Situación | Respuesta de la aplicación |
|---|---|
| | |
| | |

---

## Seguridad y permisos

<!--
Completar cuando existan requisitos de autenticación, autorización,
permisos Android, archivos, datos personales, etc.
-->

- **Autenticación / autorización:**
- **Permisos requeridos:**
- **Protección de datos:**
- **Otras consideraciones:**

- [ ] No aplica

---

## Diagrama de secuencia

<!--
Representa la interacción entre los componentes involucrados en la HU.

Ejemplo típico:
Usuario → UI → ViewModel → Repository → API / DB
-->

- **Enlace al diagrama:**

---

## Decisiones técnicas

<!--
Registra decisiones relevantes tomadas durante Diseño y su justificación.

Ejemplo:
- Se utilizará Storage Access Framework en lugar de solicitar acceso directo
  al almacenamiento porque...
-->

-

---

## Desviaciones respecto al análisis

<!--
Si durante Diseño se descubre que algo definido en Análisis debe cambiar,
documentarlo aquí y actualizar el análisis correspondiente.
-->

- [ ] No existen desviaciones.

**En caso de existir:**

-

---

## Resultado del diseño

<!--
Resume la solución acordada. Debe ser suficiente para que Desarrollo
pueda comenzar sin tener que tomar decisiones arquitectónicas importantes
que correspondían a esta fase.
-->

-

---

## Checklist de finalización

- [ ] Se revisó el análisis de la HU.
- [ ] El diseño UI/UX está definido o referenciado.
- [ ] La navegación relevante está definida.
- [ ] Los estados principales de la interfaz están contemplados.
- [ ] Los componentes técnicos involucrados están identificados.
- [ ] El flujo de datos está definido.
- [ ] La interacción con API/backend está definida, si aplica.
- [ ] Los cambios de persistencia/modelo de datos están definidos, si aplica.
- [ ] El manejo de errores relevantes está definido.
- [ ] Las consideraciones de seguridad y permisos están definidas, si aplican.
- [ ] El diagrama de secuencia está terminado.
- [ ] Las decisiones técnicas relevantes están documentadas.
- [ ] No existen decisiones pendientes que bloqueen Desarrollo.
