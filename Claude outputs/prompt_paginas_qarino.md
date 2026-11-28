# Prompt — Páginas del design system de Qariño Animal

Eres mi asistente de programación. Vamos a hacer la **capa de páginas** del nuevo design system de una app Android (Kotlin + Jetpack Compose, Material 3). Lee TODO este prompt antes de escribir código.

## 1. Contexto: qué está pasando

- El repo es `dog-adoption-app`; la app Android está en `apps/android`. Es la app del refugio de perros Qariño Animal (catálogo de perritos, solicitudes de adopción, voluntariado, donativos).
- Estamos rehaciendo **toda la UI** con atomic design: theme (base visual) → átomos → moléculas → organismos → **templates → páginas (tú)**. Cada capa la hace una persona distinta y luego se junta todo, por eso **los nombres y firmas de los templates y organismos tienen que usarse EXACTAMENTE como están en el repo**.
- Todo el design system nuevo vive en `com.app.android.presentation.designsystem` (carpeta `apps/android/app/src/main/java/com/app/android/presentation/designsystem/`), con subcarpetas `theme/`, `atoms/`, `molecules/`, `organisms/`, `templates/`, `model/` e `internal/`.
- **Ya está hecho** (rama `chore/design-system`): `theme/`, los átomos, las moléculas, los **16 organismos** (`organisms/`, con `QOrganismsGalleryPreview.kt`) y los **5 templates** (`templates/`). Funcionan y compilan.
- **Tu capa son las páginas.** Carpeta nueva: `designsystem/pages/` (sin subcarpetas). Cada página es una pantalla completa que **arma un template** con **organismos, moléculas y átomos**. Las páginas se conectan después a la navegación; **tú no tocas la navegación ni los ViewModels**.
- La UI vieja está en `presentation/view/atomicDesign/` y en `presentation/view/viewsXxx/`. **No se toca**; solo la consultas para reutilizar lógica (estados, validaciones, qué se muestra en cada caso). Sus colores y medidas NO se reutilizan.

## 2. Cómo vamos a trabajar

- **Si tienes acceso al repo:** parte de `chore/design-system` (ya incluye moléculas, organismos y templates): `git fetch && git checkout chore/design-system && git pull && git checkout -b chore/ds-pages`. Edita **solo** archivos dentro de `designsystem/pages/`. Compila con `./gradlew :app:assembleDebug`. No hagas push ni PR sin que yo te lo confirme. Commits: `feat(android): implementa QCatalogoPage`.
- **Si NO tienes acceso al repo (chat):** dame cada archivo **completo** en un bloque `kotlin` con su ruta exacta arriba, de 4 en 4, y espera a que te diga "siguiente". Yo compilo y te pego errores si los hay.
- **Antes de usar un template, organismo, molécula o átomo, lee su código real** (`templates/QX.kt`, `organisms/QX.kt`, etc.). Ahí ves exactamente cómo se comporta (padding, si `scrollable` pone scroll, si `stickyAction` se pega abajo, etc.).
- **Antes de hacer una página, lee la pantalla vieja correspondiente** (sección 7) para no perder estados, validaciones ni textos.

## 3. Reglas (no negociables)

1. **Cada página usa un template.** Nada de `QScreenScaffold` directo salvo que yo lo pida. Elige el template según la tabla de la sección 7.
2. **Las páginas NO tienen estado de negocio ni ViewModel.** Reciben todo por parámetros (datos ya listos, estados de carga/error, listas) y avisan con callbacks (`onXxxClick`, `onBack`, `onSubmit`…). Solo se permite estado **visual** local (por ejemplo, un campo de texto mientras lo escribe el usuario, o un diálogo abierto).
3. **No cambies firmas de templates, organismos, moléculas ni átomos.** Si hace falta algo que no existe, no lo arregles: dímelo y propón el cambio mínimo.
4. **Prohibido** usar `Text`, `Icon`, `IconButton`, `Button`, `Card`, `Checkbox`, `TextField`, etc. de Material. Usa `QText`, `QIcon`, `QIconButton`, `QButton`, `QCard`, `QCheckbox`, `QTextField`… **Excepciones** (contenedores que no tienen átomo): `Scaffold` no se usa (ya lo hace `QScreenScaffold`), `LazyColumn`/`LazyVerticalGrid` para listas largas y `AlertDialog`/`ModalBottomSheet` solo a través de los organismos `QConfirmDialog` y `QCatalogFilterSheet`.
5. **Cero valores sueltos:** nada de `Color(0x…)`, `fontSize`, `Icons.…`, `RoundedCornerShape(…)` ni números de `dp` para padding/radios/bordes. Usa `QTheme.colors.X`, `QText(style = QTextStyle.X)`, `QSpacing`, `QRadius`, `QShapes`, `QSize`, `QElevation`, `QIcons`. Única excepción: un tamaño que solo existe en esa página va como `private val NOMBRE = 56.dp` al inicio del archivo.
6. **Funciones auxiliares:** `private` en el mismo archivo. Nada de componentes públicos nuevos (la página es la única función pública del archivo).
7. Código en **inglés**, textos visibles en **español**.
8. **Accesibilidad:** `heading()` en títulos de página, `contentDescription` en íconos con significado, íconos decorativos con `contentDescription = null` cuando ya hay texto al lado, áreas táctiles de mínimo 48dp.
9. **Nunca reproduzcas datos reales de personas** en los previews (nombres, teléfonos, correos). Usa datos de ejemplo genéricos.

## 4. Base visual (ya existe en `theme/`, solo úsala)

**Colores — `QTheme.colors.<nombre>`:**
background #F3EEDF (fondo de pantallas) · backgroundPlain #FFFFFF · surface #FFFFFF (tarjetas, campos) · surfaceMuted #F3EEDF (recuadros dentro de tarjetas) · brand #FCF0C8 / brandStrong #F6D675 / brandDark #EFC95A (amarillo = SOLO marca, nunca botones) · **action #DCEAF8 (fondo de TODOS los botones, seleccionado) / onAction #2F6FB5 (texto de botones, links, íconos de campos) / onActionStrong #1F4F86** · positive #BFE3CB / onPositive #1F5C3A · pending #FCF0C8 / onPending #2E2A22 · negative #F6D5CC / onNegative #7A2E1F / negativeStrong #E3A08F · tag #FFFBF2 / tagBorder #2E2A22 · textPrimary #2E2A22 / textSecondary #6E685C · border #E6DFCF · fieldBorder #CFC6B2 · disabledContent #9A948A · error #BA1A1A · shadow #B8AC90.

**Texto — `QText(style = QTextStyle.X)`:** TopBarTitle · Title · SectionTitle · Body · BodyStrong · Button · Label · Caption. Si no pasas `color`, usa textPrimary (Caption usa textSecondary). `QText` NO tiene parámetro de peso.

**Medidas:** `QSpacing` xxs 2 · xs 4 · s 8 · m 12 · l 16 · xl 20 · xxl 24 · xxxl 32 · screenHorizontal 20 (dp). `QRadius` small 12 · medium 16 · large 24. `QShapes` small · card · large · pill. `QSize` border 1 · icon 24 · iconSmall 18 · buttonHeight 48 · buttonSmallHeight 36 · fieldHeight 52 · chipHeight 32 · navBarHeight 72 · topBarLogo 26 · topBarBlob 200.

**Íconos — `QIcons.<nombre>`:** Paw, Back, Notifications, Location, Calendar, Clock, Copy, Upload, Info, CheckCircle, Check, Search, Filter, ChevronRight, ExpandMore, ExpandLess, Money, Hash, Share, Close, Add, Remove, Error, Edit, Document, Email, Lock, Visibility, VisibilityOff, Help, Donate, Requests, Settings, Play.

**Tema:** `QarinoTheme { ... }` (paquete `com.app.android.presentation.designsystem.theme`). Todos los previews van dentro.

**Reglas de color:** botones SIEMPRE action/onAction (destructivos = negative); estados (`QStatusChip`) = positive/pending/negative; tags (`QTag`) = crema con borde negro; amarillo solo marca. Fondo de pantallas arena, tarjetas blancas.

## 5. Templates disponibles (ya hechos — úsalos con estas firmas exactas)

Paquete `com.app.android.presentation.designsystem.templates`. Además existe `enum class QBackground { Default, Plain }` en `QScreenScaffold.kt`.

```kotlin
// Base de todas las pantallas: topBar pegada arriba, contenido con padding, stickyAction fijo sobre bottomBar.
@Composable
fun QScreenScaffold(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    stickyAction: (@Composable () -> Unit)? = null,
    background: QBackground = QBackground.Default,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
)

// Pantallas de la nav bar (Catálogo, Programas, Donar, Solicitudes, Ajustes): topBar SIN flecha + QNavBar.
@Composable
fun QMainTabTemplate(
    title: String,
    subtitle: String?,
    selectedTab: QTab,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
    background: QBackground = QBackground.Default,
    stickyAction: (@Composable () -> Unit)? = null,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
)

// Pantallas a las que se llega desde otra (detalle del perro, mis programas, mis donativos…): topBar CON flecha + QNavBar opcional.
@Composable
fun QDetailTemplate(
    title: String,
    subtitle: String?,
    onBack: () -> Unit,
    selectedTab: QTab,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    showNavBar: Boolean = true,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
    background: QBackground = QBackground.Default,
    stickyAction: (@Composable () -> Unit)? = null,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
)

// Pasos del formulario de solicitud: topBar con flecha + QStepIndicator + botón principal fijo + SIN nav bar.
@Composable
fun QFormStepTemplate(
    title: String,
    subtitle: String?,
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    primaryActionText: String,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
    primaryActionEnabled: Boolean = true,
    isLoading: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
)

// Estado de la pantalla: cargando, error (con Reintentar) o vacío; si no, muestra content().
@Composable
fun QStateContent(
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isEmpty: Boolean = false,
    emptyMessage: String = "No hay información por ahora.",
    emptyActionText: String? = null,
    onEmptyAction: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
)
```

- `QStateContent` **no** es un template de pantalla: va **dentro** de un template (en su `content`) para manejar cargando / error / vacío.
- Si `scrollable = false`, el contenido debe usar `LazyColumn` o `LazyVerticalGrid` (no anides scroll).

## 6. Organismos, moléculas y átomos disponibles (ya hechos — úsalos con estas firmas)

Organismos (paquete `com.app.android.presentation.designsystem.organisms`). Revisa cada archivo para ver la firma completa:

- `QTopBar(title, modifier, subtitle, onBack, onNotificationsClick, notificationCount)` — normalmente lo pone el template; no la llames tú.
- `QNavBar(selected: QTab, onSelect, modifier, containerColor)` — lo pone el template.
- `QSearchBar(query, onQueryChange, onFilterClick, modifier, placeholder)`
- `QPetCard(photo: Any?, name, age, sex, onClick, modifier)`
- `QDogHeader(name, statusText, status: QStatus, location, onShare, modifier)`
- `QInfoTileRow(items: List<Pair<String, String?>>, modifier)`
- `QPhotoCarousel(media: List<QMediaItem>, modifier, onVideoClick)`
- `QProgramCard(type, title, date, time, location, capacityText, capacityProgress, onClick, modifier, isFull, enrolled, onEnroll, onCancel)`
- `QRequestCard(photo, name, folio, date, statusText, status, expanded, onToggleDetails, modifier, statusIcon, onEdit, details)`
- `QStateMessage(message, modifier, icon, actionText, onAction)`
- `QSuccessHeader(title, subtitle, modifier)`
- `QSummaryCard(title, onEdit, modifier, content)`
- `QBankDataCard(rows: List<Pair<String, String>>, onCopy: (label, value) -> Unit, modifier)`
- `QDateTimeFilterPanel(fromDate, toDate, fromTime, toTime, onFromDateClick, onToDateClick, onFromTimeClick, onToTimeClick, onClear, onApply, modifier, title, description, errorText)`
- `QConfirmDialog(title, description, confirmText, onConfirm, onDismiss, modifier, dismissText, destructive)` — diálogo modal: la página lo muestra con un `if (visible)`.
- `QCatalogFilterSheet(applied: FilterSelection, options: FilterOptions, onApply, onClear, onDismiss, isVisible, modifier)` — sheet modal. `FilterSelection` y `FilterOptions` vienen de `presentation.viewmodel.viewModelCatalogo`.

Moléculas (paquete `...designsystem.molecules`): `QIconLabel`, `QInfoTile`, `QCheckItem`, `QLabeledTextField`, `QLabeledSelectField`, `QLabeledCheckbox`, `QLabeledRadioButton`, `QLabeledSwitch`, `QInfoMessage`, `QErrorMessage`, `QSectionHeader`, `QSection(title, modifier, subtitle, content)`, `QCopyRow`, `QUploadButton(onClick, modifier, text, helperText, fileName, onRemoveFile, enabled, isLoading)`, `QCapacity`, `QFilterChipGroup`, `QCounter`, `QNavItem`, `QStepIndicator(currentStep, totalSteps, modifier)`, `QListItem(title, onClick, modifier, subtitle, icon)`.

Átomos (paquete `...designsystem.atoms`): `QText`, `QIcon`, `QIconButton`, `QButton(text, onClick, modifier, variant, size, icon, enabled, isLoading, fillMaxWidth)`, `QTextLink`, `QStatusChip`, `QTag`, `QFilterChip`, `QTextField`, `QSelectField`, `QCheckbox`, `QRadioButton`, `QSwitch`, `QProgressBar`, `QPhoto`, `QCard`, `QDivider`, `QLoadingIndicator`. Enums: `QButtonVariant { Action, Destructive }`, `QButtonSize { Regular, Small }`, `QStatus { Positive, Pending, Negative, Neutral }`.

**Ojo con `QCard`:** aplica `clip(QShapes.card)`, fondo y padding interno `QSpacing.l`.

## 7. Páginas a construir (paquete `com.app.android.presentation.designsystem.pages`)

Nombre de archivo y función: `Q<Nombre>Page.kt` / `Q<Nombre>Page`. La ruta de navegación está en `presentation/navigation/routes.kt`. Las firmas de cada página las defines tú a partir de la pantalla vieja (qué datos necesita y qué callbacks); **anótalas todas en el resumen** para que la persona de navegación conecte los ViewModels.

| #   | Página                         | Ruta (`routes.kt`)                      | Template                                                 | Organismos principales                                                                             | Pantalla vieja (consultar)                                                  |
| --- | ------------------------------ | --------------------------------------- | -------------------------------------------------------- | -------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------- |
| 1   | `QCatalogoPage`                | `Catalogo`                              | `QMainTabTemplate(Catalogo)`                             | QSearchBar, QPetCard, QCatalogFilterSheet, QStateContent                                           | `view/viewsCatalogo/CatalogoFiltradoScreen.kt`, `CatalogoFiltrosRoute.kt`   |
| 2   | `QDetallePerroPage`            | `DetallePerro`                          | `QDetailTemplate(Catalogo)`                              | QPhotoCarousel, QDogHeader, QInfoTileRow, QSection, QButton ("¡Quiero Adoptar!") en `stickyAction` | `view/viewsCatalogo/DogDetailScreenView.kt`, `page/DogDetailTemplate.kt`    |
| 3   | `QProgramasPage`               | `Programas`                             | `QMainTabTemplate(Ayudar)`                               | QDateTimeFilterPanel, QProgramCard, QStateContent                                                  | `page/ProgramasPage.kt`                                                     |
| 4   | `QMisVoluntariadosPage`        | `MisVoluntariados`                      | `QDetailTemplate(Ayudar)`                                | QProgramCard (con `onCancel`), QStateContent                                                       | `page/ProgramasPage.kt` (estado "Mis programas")                            |
| 5   | `QDetalleProgramaPage`         | `DetallePrograma`                       | `QDetailTemplate(Ayudar)`                                | QProgramCard o secciones con QSection; QButton "Inscribirme" en `stickyAction`                     | `page/DetalleProgramaPage.kt`                                               |
| 6   | `QConfirmacionInscripcionPage` | `ConfirmacionInscripcion`               | `QDetailTemplate(Ayudar)`                                | QSuccessHeader, QButton                                                                            | `page/VolunteeringConfirmationScreen.kt`, `ConfirmacionInscripcionRoute.kt` |
| 7   | `QMisSolicitudesPage`          | `MisSolicitudes`                        | `QMainTabTemplate(Solicitudes)`                          | QRequestCard, QStateContent                                                                        | `page/MisSolicitudesScreen.kt`                                              |
| 8   | `QRequisitosPage`              | `Requisitos`                            | `QFormStepTemplate` paso 1                               | QSection, QCheckItem, QButton                                                                      | `page/RequisitosScreen.kt`                                                  |
| 9   | `QDatosGeneralesPage`          | `DatosGenerales`                        | `QFormStepTemplate` paso 2                               | QLabeledTextField, QLabeledSelectField                                                             | `page/DatosGeneralesScreen.kt`                                              |
| 10  | `QTuHogarPage`                 | `TuHogar`                               | `QFormStepTemplate` paso 3                               | QLabeledRadioButton, QLabeledSwitch, QLabeledTextField                                             | `page/TuHogarScreen.kt`                                                     |
| 11  | `QCompromisoPage`              | `Compromiso`                            | `QFormStepTemplate` paso 4                               | QLabeledCheckbox, QInfoMessage                                                                     | `page/CompromisoScreen.kt`                                                  |
| 12  | `QDocumentosPage`              | `Documentos`                            | `QFormStepTemplate` paso 5                               | QUploadButton, QStateContent                                                                       | `page/UploadDocuments.kt`                                                   |
| 13  | `QConfirmarSolicitudPage`      | (revisar `routes.kt`: `ConfirmarRoute`) | `QFormStepTemplate` paso final                           | QSummaryCard, QInfoMessage, QButton                                                                | `page/ConfirmarScreen.kt`                                                   |
| 14  | `QSolicitudEnviadaPage`        | (revisar `routes.kt`: `EnviadaScreen`)  | `QDetailTemplate(Solicitudes)` sin nav bar si hace falta | QSuccessHeader                                                                                     | `page/EnviadaScreen.kt`                                                     |
| 15  | `QDonarDineroPage`             | `DonarDinero`                           | `QMainTabTemplate(Donar)`                                | QBankDataCard, QUploadButton, QTextField (referencia), QButton en `stickyAction`                   | `page/DonarDineroScreen.kt`                                                 |
| 16  | `QDonativoEnviadoPage`         | `DonativoEnviado`                       | `QDetailTemplate(Donar)`                                 | QSuccessHeader, QButton                                                                            | `page/DonativoEnviadoScreen.kt`                                             |
| 17  | `QDatosFiscalesPage`           | `DatosFiscales`                         | `QDetailTemplate(Donar)`                                 | QLabeledTextField, QLabeledSelectField, QButton                                                    | `page/DatosFiscalesScreen.kt` (archivo grande: leer por partes)             |
| 18  | `QMisDonativosPage`            | `MisDonativos`                          | `QDetailTemplate(Donar)`                                 | QStateContent, QSummaryCard o tarjetas con QCard                                                   | `page/MisDonativosScreen.kt`                                                |
| 19  | `QConfiguracionPage`           | `Configuracion`                         | `QMainTabTemplate(Ajustes)`                              | QListItem, QDivider                                                                                | `view/viewsConfiguracion/ConfiguracionScreen.kt`                            |

**Fuera de este alcance (fase 2, no las hagas ahora):** Login, Registro, Cuenta, Correo (`viewsPerfiles/`). Las pedirá después otra persona.

**Notas por página:**

- **Catálogo:** la barra de búsqueda y el filtro van arriba del grid. El sheet de filtros se abre con un estado visual local (`isVisible`).
- **Detalle del perro:** el botón "¡Quiero Adoptar!" va en `stickyAction` y ocupa todo el ancho.
- **Donar dinero:** el monto y el folio son campos; los datos bancarios se copian con `QBankDataCard`; el botón "Enviar donativo" va en `stickyAction`.
- **Formularios de solicitud (8–13):** el botón principal (`primaryActionText`) cambia según el paso ("Continuar", "Enviar solicitud"…); revisa la pantalla vieja para los textos exactos.
- **Pantallas de éxito (6, 14, 16):** usan `QSuccessHeader`; el botón de regreso va al inicio del flujo.
- **Estados:** toda lista usa `QStateContent` para cargando, error y vacío. Los textos vacíos salen de la pantalla vieja.

## 8. Lecciones de capas anteriores (para no repetir problemas)

- **Imports:** el repo usa ktlint (`ktlint_official`): imports en **orden ASCII** (mayúsculas antes que minúsculas dentro del mismo paquete), **sin imports sin usar**. **Ningún `import` puede ir después de código ni de un `@OptIn`**: el error típico es "imports are only allowed in the beginning of file". Si una función necesita `@OptIn`, va justo encima de `@Composable`, después de todos los imports.
- **`Modifier.weight` solo dentro de `Row`/`Column`:** es un miembro de `RowScope`/`ColumnScope`. Si lo usas fuera, da error.
- **Dialogs y sheets no se ven en el panel de preview de Android Studio:** `QConfirmDialog` y `QCatalogFilterSheet` se ven con el botón Run del preview, en el emulador.
- **`QText` no tiene `fontWeight`:** elige el estilo más cercano y anótalo en el resumen.
- **Callbacks que no pueden ser null:** `QCheckbox` y `QSwitch` exigen `onCheckedChange` no nulo; `QRadioButton` sí acepta `onClick = null`.
- **`QCard` ya tiene padding interno `QSpacing.l`.** No agregues otro encima salvo que sea intencional.
- **Textos largos:** pon `weight(1f)` + `maxLines`/`Ellipsis` donde el texto pueda empujar a otro elemento fuera de la pantalla.
- **Preview del template:** los templates usan `QTab`, así que el preview de una página necesita un `remember { mutableStateOf(QTab.X) }` para la nav bar.

## 9. Formato de cada archivo

```kotlin
package com.app.android.presentation.designsystem.pages

import ... // solo los necesarios, en orden ASCII (ktlint: sin imports sin usar)

/**
 * <qué pantalla es y qué muestra>
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): <template>, <organismos>.
 * Reemplaza a: <pantalla vieja>.
 */
@Composable
fun QNombrePage(
    param: Tipo,
    onCallback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // implementación
}

@Preview(showBackground = true, name = "QNombrePage - estados")
@Composable
private fun QNombrePagePreview() {
    QarinoTheme {
        // estados: normal, cargando, vacío, error, texto largo
    }
}
```

Estilo ktlint: firma en varias líneas cuando hay 2+ parámetros, coma final en listas multilínea, máx. 140 caracteres por línea, `modifier =` en líneas aparte cuando encadenas varios modificadores.

## 10. Al final

1. Crea también `designsystem/pages/QPagesGalleryPreview.kt`: **un solo** `@Preview(showBackground = true, widthDp = 360, heightDp = 3200, name = "Galería de páginas")` privado que muestre **una vista previa resumida** de cada página (datos de ejemplo, un estado por página). Las páginas con diálogo o sheet no se incluyen; tienen su propio preview.
2. Verifica: `grep -r "QPlaceholder" designsystem/pages` = 0; ningún `Color(0x`, `fontSize`, `Icons.`, `RoundedCornerShape(` ni `Text(`/`Icon(`/`Button(`/`Card(` de Material en `pages/`; imports ordenados y sin sobrantes; ninguna línea de más de 140 caracteres; compila con `./gradlew :app:assembleDebug`.
3. Dame un resumen: páginas terminadas (con su **firma completa**), parámetros opcionales que hayas agregado (si alguno), problemas que viste en templates, organismos, moléculas o átomos, y decisiones visuales que tomaste.
