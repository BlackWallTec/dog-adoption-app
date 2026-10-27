# 🧠 Capa de Dominio (Domain Layer)

> **El Corazón e Identidad de la Aplicación**

---

## 📌 1. Definición, Objetivo y Fundamentos

La capa de **Domain** es el núcleo de la aplicación. Contiene únicamente las **reglas de negocio puras** y la lógica que hace única a esta app, independientemente del framework que se use para mostrar la UI o de la base de datos que se use para guardar la información[cite: 3].

### Fundamentos Clave:

- **Inmunidad al Framework (Kotlin Puro):** No depende de librerías de Android, Jetpack Compose, Retrofit, Room o Supabase[cite: 3]. Si mañana cambias Android por otro entorno, esta capa debería compilar intacta.
- **Inversión de Dependencias (DIP):** Define los contratos (`interfaces`) que la capa de datos debe implementar[cite: 3, 4]. Nadie en `Domain` sabe cómo se guardan o traen los datos por la red, solo saben _qué_ se puede solicitar[cite: 3].
- **Modelos Limpios:** Trabaja exclusivamente con `Entities`, las cuales son inmunes a los formatos feos o cambiantes de los JSONs del servidor (DTOs)[cite: 3, 4].

---

## 🗂️ 2. Menú de Navegación de Carpetas

Usa esta guía rápida para saber dónde encontrar o agregar tu código dentro de `domain`:

| Carpeta                           | ¿Qué hay aquí?                                                 | ¿Para qué sirve? / Buscar aquí si necesitas...                                                                     |
| :-------------------------------- | :------------------------------------------------------------- | :----------------------------------------------------------------------------------------------------------------- |
| 📦 **`entities/`**[cite: 14]      | Modelos de datos limpios (`data class` en Kotlin)[cite: 3].    | Crear o consultar los modelos de negocio principales (ej. `Dog`, `User`, `AdoptionForm`)[cite: 3].                 |
| 🤝 **`repoInterface/`**[cite: 14] | Interfaces y contratos de repositorios[cite: 3].               | Definir un nuevo "menú de acciones" o métodos que la app requiere para pedir o enviar datos[cite: 3].              |
| ⚙️ **`useCases/`**[cite: 14]      | Casos de uso (Lógica de Negocio ejecutada por la UI)[cite: 3]. | Modificar u organizar reglas de negocio específicas (ej. `GetDogByIdUseCase`, `ValidateAdoptionUseCase`)[cite: 3]. |

---

> ⚠️ **Regla de Oro en Domain:** Jamás importes `android.*`, `com.google.gson.*` o paquetes de la capa `Data` en esta carpeta[cite: 3].
