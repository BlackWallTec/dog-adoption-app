package com.app.android.presentation.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.presentation.view.atomicDesign.page.ConfirmarRoute
import com.app.android.presentation.view.atomicDesign.page.DonativoEnviadoScreen
import com.app.android.presentation.view.viewsSolicitudes.EnviadaRoute
import com.app.android.presentation.view.atomicDesign.page.SeccionSolicitud
import com.app.android.presentation.view.atomicDesign.page.TuHogarScreen
import com.app.android.presentation.view.atomicDesign.page.UploadDocumentsRoute
import com.app.android.presentation.view.viewsCatalogo.CatalogoFiltrosRoute
import com.app.android.presentation.view.viewsCatalogo.DogDetailScreenView
import com.app.android.presentation.view.viewsCatalogo.PerroScreen
import com.app.android.presentation.view.viewsDonativos.DatosFiscalesRoute
import com.app.android.presentation.view.viewsDonativos.DonarDineroRoute
import com.app.android.presentation.view.viewsPerfiles.CuentaScreen
import com.app.android.presentation.view.viewsPerfiles.InicioSesionScreen
import com.app.android.presentation.view.viewsPerfiles.LoginScreen
import com.app.android.presentation.view.viewsPerfiles.RegistroScreen
import com.app.android.presentation.view.viewsSolicitudes.CompromisoRoute
import com.app.android.presentation.view.viewsSolicitudes.DatosGeneralesRoute
import com.app.android.presentation.view.viewsSolicitudes.MisSolicitudesRoute
import com.app.android.presentation.view.viewsSolicitudes.RequisitosRoute
import com.app.android.presentation.view.viewsSolicitudes.TuHogarRoute
import com.app.android.presentation.view.viewsVoluntariado.ConfirmacionInscripcionRoute
import com.app.android.presentation.view.viewsVoluntariado.DetalleProgramaScreen
import com.app.android.presentation.view.viewsVoluntariado.MisProgramasScreen
import com.app.android.presentation.view.viewsVoluntariado.ProgramasScreen
import com.app.android.presentation.viewmodel.viewModelPerfiles.SesionUiState
import com.app.android.presentation.viewmodel.viewModelPerfiles.SesionViewModel
import com.app.android.presentation.viewmodel.viewModelVoluntariado.DetalleProgramaViewModel

// Regreso al catálogo sin dejar pantallas anteriores encima.
private fun NavController.irAlCatalogo() {
    navigate(Screen.Catalogo.ruta) {
        popUpTo(Screen.Catalogo.ruta) { inclusive = false }
        launchSingleTop = true
    }
}

// Al perder la sesión, saco las pantallas privadas de la pila.
private fun NavController.irAlLogin() {
    navigate(Screen.Login.ruta) {
        popUpTo(Screen.Catalogo.ruta) { inclusive = false }
        launchSingleTop = true
    }
}

// La opción de ajustes abre la cuenta; ahí se revisa si hace falta iniciar sesión.
// Barra de abajo: 0 Catálogo, 1 Ayudar, 2 Donar, 3 Solicitudes, 4 Ajustes.
private fun NavController.irAOpcionBarra(opcion: Int) {
    if (opcion == 0) {
        irAlCatalogo()
        return
    }
    val destino =
        when (opcion) {
            1 -> Screen.GrafoVoluntariado.ruta
            2 -> Screen.DonarDinero.ruta
            3 -> Screen.MisSolicitudes.ruta
            4 -> Screen.Cuenta.ruta
            else -> null
        }
    destino?.let {
        navigate(it) {
            popUpTo(Screen.Catalogo.ruta) { inclusive = false }
            launchSingleTop = true
        }
    }
}

// Ruta con sus argumentos ya sustituidos (p. ej. solicitud_requisitos/123), para volver a ella tras el login.
private fun NavBackStackEntry.rutaConcreta(): String? {
    var ruta = destination.route ?: return null
    val args = arguments ?: return ruta
    args.keySet().forEach { clave ->
        args.getString(clave)?.let { ruta = ruta.replace("{$clave}", Uri.encode(it)) }
    }
    return ruta
}

// Espero la revisión de sesión antes de mostrar datos de una cuenta.
@Composable
private fun ConSesion(
    estado: SesionUiState,
    navController: NavController,
    sesionViewModel: SesionViewModel,
    recordarDestino: (String?) -> Unit,
    content: @Composable (Sesion) -> Unit,
) {
    val sesion = estado.sesion
    when {
        estado.comprobando -> {
            InicioSesionScreen(
                state = estado,
                onRetry = sesionViewModel::restaurar,
                onGuest = navController::irAlCatalogo,
            )
        }

        sesion != null -> {
            content(sesion)
        }

        // No convertir un fallo de red al restaurar en una sesión cerrada.
        estado.error != null -> {
            InicioSesionScreen(
                state = estado,
                onRetry = sesionViewModel::restaurar,
                onGuest = navController::irAlCatalogo,
            )
        }

        // Esto también se ejecuta si la sesión vence o se cierra.
        else -> {
            LaunchedEffect(Unit) {
                // Recuerdo a dónde iba (salvo Cuenta, que es también el destino al cerrar sesión).
                val ruta = navController.currentBackStackEntry?.rutaConcreta()
                recordarDestino(ruta?.takeIf { it != Screen.Cuenta.ruta })
                navController.irAlLogin()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    // 1. El NavController que controla el estado de la navegación
    val navController = rememberNavController()
    val context = LocalContext.current
    var destinoTrasLogin by rememberSaveable { mutableStateOf<String?>(null) }
    // Sesión compartida entre las pantallas de la aplicación.
    val sesionViewModel: SesionViewModel = hiltViewModel()
    val estadoSesion by sesionViewModel.state.collectAsStateWithLifecycle()

    // 2. El NavHost donde defines la pantalla de inicio (startDestination)
    NavHost(
        navController = navController,
        // startDestination = Screen.DetallePerro.crearRuta("0edfe707-cc43-4bcf-bd43-a7f61e312e17"),
        startDestination = Screen.Catalogo.ruta,
    ) {
        // El login entrega la sesión al mismo ViewModel que usa la cuenta.
        composable(route = Screen.Login.ruta) {
            LoginScreen(
                onBackToCatalog = {
                    destinoTrasLogin = null
                    navController.irAlCatalogo()
                },
                onExploreWithoutAccount = {
                    destinoTrasLogin = null
                    navController.irAlCatalogo()
                },
                onRegister = {
                    navController.navigate(Screen.Registro.ruta) { launchSingleTop = true }
                },
                onAuthenticated = { sesion ->
                    sesionViewModel.autenticado(sesion)
                    // Si venía de una pantalla protegida (p. ej. adoptar un perro), vuelve ahí.
                    val destino = destinoTrasLogin ?: Screen.Cuenta.ruta
                    destinoTrasLogin = null
                    navController.navigate(destino) {
                        popUpTo(Screen.Catalogo.ruta) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }

        // Crear una cuenta termina en el login; el registro no trae un token.
        composable(route = Screen.Registro.ruta) {
            RegistroScreen(
                onCancel = navController::irAlLogin,
                onRegistered = {
                    Toast
                        .makeText(context, "Cuenta creada. Inicia sesión.", Toast.LENGTH_LONG)
                        .show()
                    navController.irAlLogin()
                },
            )
        }

        // Conecto los botones de cuenta sin meter peticiones de red en la vista.
        composable(route = Screen.Cuenta.ruta) {
            ConSesion(estadoSesion, navController, sesionViewModel, { destinoTrasLogin = it }) {
                CuentaScreen(
                    state = estadoSesion,
                    onRetry = sesionViewModel::consultarPerfil,
                    onCatalog = navController::irAlCatalogo,
                    onLogout = {
                        destinoTrasLogin = null
                        sesionViewModel.salir()
                    },
                    onLogin = navController::irAlLogin,
                )
            }
        }

        // ========= Pantallas Catalogo ========
        composable(route = Screen.Catalogo.ruta) {
            CatalogoFiltrosRoute(
                onDogClick = { dogId ->
                    navController.navigate(
                        Screen.DetallePerro.crearRuta(dogId),
                    )
                },
                // Ajustes abre la cuenta o pide login según el estado de sesión.
                onNavSelect = navController::irAOpcionBarra,
            )
        }
        // PANTALLA 2: El detalle del perro
        composable(
            route = Screen.DetallePerro.ruta,
            arguments =
                listOf(
                    navArgument("dogId") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            // Extraemos de forma segura el ID que viaja en la ruta
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""

            // Llamamos a la función composable de la pantalla pasándole el ID extraído
            DogDetailScreenView(
                dogId = dogId,
                onNavSelect = navController::irAOpcionBarra,
                onBackClick = { navController.navigateUp() },
                onAdoptClick = { dogId ->
                    navController.navigate(Screen.Requisitos.crearRuta(dogId))
                },
            )
        }

        // ========= Pantallas Solicitudes ========

        // SOLICITUDES - HU06: Mis solicitudes

        // SOLICITUDES: Paso 1 - Requisitos
        composable(
            route = Screen.Requisitos.ruta,
            arguments =
                listOf(
                    navArgument("idPerro") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            // Estos datos necesitan una sesión válida.
            ConSesion(
                estadoSesion,
                navController,
                sesionViewModel,
                { destinoTrasLogin = it },
            ) { sesion ->
                val idPerro = backStackEntry.arguments?.getString("idPerro") ?: ""

                RequisitosRoute(
                    idUsuario = sesion.usuario.id, // El id sale de la sesión actual.
                    idPerro = idPerro,
                    onBack = { navController.navigateUp() },
                    onIrADatosGenerales = {
                        navController.navigate(Screen.DatosGenerales.ruta)
                    },
                )
            }
        }

        // SOLICITUDES: Paso 2 - Datos generales
        composable(route = Screen.DatosGenerales.ruta) {
            // Estos datos necesitan una sesión válida.
            ConSesion(
                estadoSesion,
                navController,
                sesionViewModel,
                { destinoTrasLogin = it },
            ) { _ ->
                DatosGeneralesRoute(
                    onBack = { navController.navigateUp() },
                    onIrATuHogar = {
                        navController.navigate(Screen.TuHogar.ruta)
                    },
                )
            }
        }

        // SOLICITUDES: Paso 3 - Tu Hogar
        composable(route = Screen.TuHogar.ruta) {
            // Estos datos necesitan una sesión válida.
            ConSesion(
                estadoSesion,
                navController,
                sesionViewModel,
                { destinoTrasLogin = it },
            ) { _ ->
                TuHogarRoute(
                    onBack = { navController.navigateUp() },
                    onIrACompromiso = {
                        navController.navigate(Screen.Compromiso.ruta)
                    },
                )
            }
        }

        // Paso 4 - Compromiso
        composable(route = Screen.Compromiso.ruta) {
            // Estos datos necesitan una sesión válida.
            ConSesion(
                estadoSesion,
                navController,
                sesionViewModel,
                { destinoTrasLogin = it },
            ) { _ ->
                CompromisoRoute(
                    onBack = { navController.navigateUp() },
                    onIrADocumentoss = {
                        navController.navigate(Screen.Documentos.ruta)
                    },
                )
            }
        }
        // Paso 5 - Documentos
        composable(route = Screen.Documentos.ruta) {
            ConSesion(estadoSesion, navController, sesionViewModel, { destinoTrasLogin = it }) { _ ->
                UploadDocumentsRoute(
                    onBackClick = { navController.navigateUp() },
                    onContinuarClick = {
                        navController.navigate(Screen.Confirmar.ruta)
                    },
                )
            }
        }

        // Paso 6 - Confirmar
        composable(route = Screen.Confirmar.ruta) {
            ConSesion(
                estadoSesion,
                navController,
                sesionViewModel,
                { destinoTrasLogin = it },
            ) { _ ->
                ConfirmarRoute(
                    onBack = { navController.navigateUp() },
                    onEditarSeccion = { seccion ->
                        val rutaDestino =
                            when (seccion) {
                                SeccionSolicitud.DATOS_GENERALES -> Screen.DatosGenerales.ruta
                                SeccionSolicitud.TU_HOGAR -> Screen.TuHogar.ruta
                                SeccionSolicitud.COMPROMISO -> Screen.Compromiso.ruta
                                SeccionSolicitud.DOCUMENTOS -> Screen.Documentos.ruta
                            }
                        navController.popBackStack(rutaDestino, inclusive = false)
                    },
                    onEnviarExitoso = { solicitudId ->
                        navController.navigate(Screen.SolicitudEnviada.crearRuta(solicitudId)) {
                            popUpTo(Screen.Catalogo.ruta) { inclusive = false }
                        }
                    },
                    onUnauthorized = {
                        navController.irAlLogin()
                    },
                )
            }
        }

        // Paso 7 - Enviado
        composable(
            route = Screen.SolicitudEnviada.ruta,
            arguments =
                listOf(
                    navArgument("idSolicitud") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val solicitudId = backStackEntry.arguments?.getString("idSolicitud") ?: ""

            ConSesion(
                estado = estadoSesion,
                navController = navController,
                sesionViewModel = sesionViewModel,
                recordarDestino = { destinoTrasLogin = it },
            ) { _ ->
                EnviadaRoute(
                    solicitudId = solicitudId,
                    onBack = navController::irAlCatalogo,
                    onVerSolicitudes = navController::irAlCatalogo,
                    onNavSelect = navController::irAOpcionBarra,
                )
            }
        }

        // SOLICITUDES - HU04: Mis solicitudes (solo lectura)
        composable(route = Screen.MisSolicitudes.ruta) {
            // Las solicitudes son del usuario con sesión; sin sesión se va al login.
            ConSesion(
                estadoSesion,
                navController,
                sesionViewModel,
                { destinoTrasLogin = it },
            ) {
                MisSolicitudesRoute(
                    onEditar = {
                        // navegar a la edición de la solicitud HU09.
                    },
                    onVerCatalogo = { navController.popBackStack(Screen.Catalogo.ruta, inclusive = false) },
                    onNavSelect = navController::irAOpcionBarra,
                )
            }
        }

        // ========= Pantallas Donativos ========

        // DONATIVOS - HU46: registrar donativo económico
        composable(route = Screen.DonarDinero.ruta) {
            // Estos datos necesitan una sesión válida.
            ConSesion(
                estadoSesion,
                navController,
                sesionViewModel,
                { destinoTrasLogin = it },
            ) { sesion ->
                DonarDineroRoute(
                    idUsuario = sesion.usuario.id, // El id sale de la sesión actual.
                    onBack = { navController.popBackStack() },
                    onNavSelect = navController::irAOpcionBarra,
                    onEnviado = { monto ->
                        navController.navigate(Screen.DonativoEnviado.crearRuta(monto)) {
                            // saca el formulario de la pila para que "atrás" no deje mandarlo dos veces
                            popUpTo(Screen.DonarDinero.ruta) { inclusive = true }
                        }
                    },
                )
            }
        }

        composable(
            route = Screen.DonativoEnviado.ruta,
            arguments =
                listOf(
                    navArgument("monto") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            // Estos datos necesitan una sesión válida.
            ConSesion(
                estadoSesion,
                navController,
                sesionViewModel,
                { destinoTrasLogin = it },
            ) { _ ->
                val monto = backStackEntry.arguments?.getString("monto") ?: ""
                DonativoEnviadoScreen(
                    monto = "\$$monto MXN",
                    onBackClick = { navController.popBackStack() },
                    onVolverDonarClick = { navController.navigate(Screen.DonarDinero.ruta) },
                    onVerMisDonativosClick = {}, // Mis donativos todavía no existe
                    onNavSelect = navController::irAOpcionBarra,
                )
            }
        }

        // DONATIVOS - HU45: datos fiscales del donante
        composable(route = Screen.DatosFiscales.ruta) {
            // Estos datos necesitan una sesión válida.
            ConSesion(
                estadoSesion,
                navController,
                sesionViewModel,
                { destinoTrasLogin = it },
            ) { sesion ->
                DatosFiscalesRoute(
                    idUsuario = sesion.usuario.id, // El id sale de la sesión actual.
                    onBack = { navController.navigateUp() },
                    onNavSelect = navController::irAOpcionBarra,
                )
            }
        }

        // ========= Pantallas Voluntariado ========

// Programas, Detalle y Confirmación comparten la misma instancia de ProgramasViewModel
        navigation(
            startDestination = Screen.Programas.ruta,
            route = Screen.GrafoVoluntariado.ruta,
        ) {
            composable(route = Screen.Programas.ruta) { entry ->
                ConSesion(
                    estadoSesion,
                    navController,
                    sesionViewModel,
                    { destinoTrasLogin = it },
                ) { _ ->
                    val grafo =
                        remember(entry) { navController.getBackStackEntry(Screen.GrafoVoluntariado.ruta) }
                    ProgramasScreen(
                        viewModel = hiltViewModel(grafo),
                        onBackClick = { navController.navigateUp() },
                        onNavSelect = navController::irAOpcionBarra,
                        onInscripcionExitosa = {
                            navController.navigate(Screen.ConfirmacionInscripcion.ruta) {
                                launchSingleTop = true
                            }
                        },
                        onMisVoluntariados = {
                            navController.navigate(Screen.MisVoluntariados.ruta) {
                                launchSingleTop = true
                            }
                        },
                        // HU36: al tocar una tarjeta abre su detalle
                        onVerDetalle = { idPrograma ->
                            navController.navigate(Screen.DetallePrograma.crearRuta(idPrograma))
                        },
                    )
                }
            }

            composable(route = Screen.ConfirmacionInscripcion.ruta) { entry ->
                ConSesion(
                    estadoSesion,
                    navController,
                    sesionViewModel,
                    { destinoTrasLogin = it },
                ) { _ ->
                    val grafo =
                        remember(entry) { navController.getBackStackEntry(Screen.GrafoVoluntariado.ruta) }
                    ConfirmacionInscripcionRoute(
                        viewModel = hiltViewModel(grafo),
                        onBack = { navController.navigateUp() },
                        onExplorarProgramas = {
                            navController.popBackStack(Screen.Programas.ruta, inclusive = false)
                        },
                        onNavSelect = navController::irAOpcionBarra,
                        onVerMisVoluntariados = {
                            // HU40
                            navController.navigate(Screen.MisVoluntariados.ruta) {
                                launchSingleTop = true
                            }
                        },
                        onVerDetalle = { idPrograma ->
                            navController.navigate(Screen.DetallePrograma.crearRuta(idPrograma))
                        },
                    )
                }
            }

            composable(route = Screen.MisVoluntariados.ruta) {
                ConSesion(
                    estadoSesion,
                    navController,
                    sesionViewModel,
                    { destinoTrasLogin = it },
                ) { _ ->
                    MisProgramasScreen(
                        onBackClick = { navController.popBackStack(Screen.Programas.ruta, inclusive = false) },
                        onNavSelect = navController::irAOpcionBarra,
                        onVerDetalle = { idPrograma ->
                            navController.navigate(Screen.DetallePrograma.crearRuta(idPrograma))
                        },
                    )
                }
            }

            // HU36: detalle del programa. Comparte ProgramasViewModel para inscribirse (HU33)
            composable(
                route = Screen.DetallePrograma.ruta,
                arguments =
                    listOf(
                        navArgument(DetalleProgramaViewModel.ARG_ID_PROGRAMA) { type = NavType.StringType },
                    ),
            ) { entry ->
                ConSesion(
                    estadoSesion,
                    navController,
                    sesionViewModel,
                    { destinoTrasLogin = it },
                ) { _ ->
                    val grafo =
                        remember(entry) { navController.getBackStackEntry(Screen.GrafoVoluntariado.ruta) }
                    DetalleProgramaScreen(
                        programasViewModel = hiltViewModel(grafo),
                        onBackClick = { navController.navigateUp() },
                        onNavSelect = navController::irAOpcionBarra,
                        onInscripcionExitosa = {
                            navController.navigate(Screen.ConfirmacionInscripcion.ruta) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }

            // Plantilla para nueva pantalla
            // composable(
            //     route = Screen.EditarPerro.ruta,
            //     arguments =
            //         listOf(
            //             navArgument("dogId") { type = NavType.StringType },
            //             navArgument("categoria") { type = NavType.StringType },
            //         ),
            // ) { backStackEntry ->
            // val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            // val categoria = backStackEntry.arguments?.getString("categoria") ?: ""
            // editDogScreen(dogId = dogId, categoria = categoria) // <- Page/View Composable
            // }
        }
    }
}
