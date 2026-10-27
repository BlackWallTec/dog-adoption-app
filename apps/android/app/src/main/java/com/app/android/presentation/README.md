# 🎨 Capa de Presentación (Presentation Layer)

> **La Interfaz de Usuario, Estado Visual y Captura de Eventos**

---

## 📌 1. Definición, Objetivo y Fundamentos

La capa de **Presentation** es la responsable de todo lo que el usuario ve y toca en la pantalla de su dispositivo móvil[cite: 3]. Se encarga de transformar los datos de negocio en un estado visual (`UiState`), renderizar los componentes gráficos usando Jetpack Compose y capturar las interacciones del usuario para enviarlas hacia la lógica de negocio[cite: 2, 3].

### Fundamentos Clave:

- **Arquitectura MVVM:** Separa estrictamente la interfaz visual (**View**) de la gestión del estado de la pantalla (**ViewModel**)[cite: 3].
- **Flujo Unidireccional de Datos (UDF):** La `View` envía **Eventos** de interacción al `ViewModel`. El `ViewModel` ejecuta `UseCases` y expone un **UiState** actualizado. La `View` observa ese estado y se **Recompone** (redibuja) automáticamente[cite: 2, 3, 7].
- **ViewModel Inmune a Rotaciones:** El `ViewModel` sostiene los datos y sobrevive a cambios de configuración (como rotar la pantalla) sin perder la información del usuario[cite: 3].

---

## 🗂️ 2. Menú de Navegación de Carpetas

Usa esta guía rápida para saber dónde encontrar o agregar tu código dentro de `presentation`[cite: 14]:

| Carpeta                       | ¿Qué hay aquí?                                                                       | ¿Para qué sirve? / Buscar aquí si necesitas...                                                                                        |
| :---------------------------- | :----------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------ |
| 🧠 **`viewmodel/`**[cite: 14] | Clases `ViewModel` (`@HiltViewModel`) y sellos de estado de UI (`UiState`)[cite: 3]. | Modificar la gestión de estados de la pantalla (Loading, Success, Error), capturar eventos de usuario o llamar a `UseCases`[cite: 3]. |
| 🎨 **`view/`**[cite: 14]      | Componentes visuales gráficos en Jetpack Compose (`@Composable`)[cite: 2].           | Modificar botones, tarjetas, pantallas, animaciones o elementos de diseño de la interfaz de usuario[cite: 2].                         |

---

> ⚠️ **Regla de Oro en Presentation:** La `View` no toma decisiones de negocio ni hace peticiones de red directas[cite: 2, 3]. El `ViewModel` no importa clases de Compose (`androidx.compose.*`) ni referencias a vistas visuales[cite: 3].
