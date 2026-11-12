package com.app.android.presentation.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.presentation.view.atomicDesign.page.DonativoEnviadoScreen
import com.app.android.presentation.view.viewsCatalogo.DogDetailScreenView
import com.app.android.presentation.view.viewsCatalogo.PerroScreen
import com.app.android.presentation.view.viewsDonativos.DatosFiscalesRoute
import com.app.android.presentation.view.viewsDonativos.DonarDineroRoute
import com.app.android.presentation.view.viewsPerfiles.CuentaScreen
import com.app.android.presentation.view.viewsPerfiles.InicioSesionScreen
import com.app.android.presentation.view.viewsPerfiles.LoginRoute
import com.app.android.presentation.view.viewsPerfiles.RegistroRoute
import com.app.android.presentation.view.viewsSolicitudes.CompromisoRoute
import com.app.android.presentation.view.viewsSolicitudes.DatosGeneralesRoute
import com.app.android.presentation.view.viewsSolicitudes.RequisitosRoute
import com.app.android.presentation.view.viewsSolicitudes.TuHogarRoute
import com.app.android.presentation.view.viewsVoluntariado.ConfirmacionInscripcionRoute
import com.app.android.presentation.view.viewsVoluntariado.ProgramasScreen
import com.app.android.presentation.viewmodel.viewModelPerfiles.SesionUiState
import com.app.android.presentation.viewmodel.viewModelPerfiles.SesionViewModel


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
private fun NavController.irAOpcionBarra(opcion: Int) {
    if (opcion == 0) {
        irAlCatalogo()
        return
    }
    val destino = when (opcion) {
        1 -> Screen.GrafoVoluntariado.ruta
        2 -> Screen.DonarDinero.ruta
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

// Espero la revisión de sesión antes de mostrar datos de una cuenta.
@Composable
private fun ConSesion(
    estado: SesionUiState,
    navController: NavController,
    sesionViewModel: SesionViewModel,
    content: @Composable (Sesion) -> Unit,
) {
    val sesion = estado.sesion
    when {
        estado.comprobando -> InicioSesionScreen(
            state = estado,
            onRetry = sesionViewModel::restaurar,
            onGuest = navController::irAlCatalogo,
        )
        sesion != null -> content(sesion)
        // Esto también se ejecuta si la sesión vence o se cierra.
        else -> LaunchedEffect(Unit) { navController.irAlLogin() }
    }
}

@Composable
fun AppNavigation() {
    // 1. El NavController que controla el estado de la navegación
    val navController = rememberNavController()
    val context = LocalContext.current
    // Sesión compartida entre las pantallas de la aplicación.
    val sesionViewModel: SesionViewModel = hiltViewModel()
    val estadoSesion by sesionViewModel.state.collectAsStateWithLifecycle()

    // 2. El NavHost donde defines la pantalla de inicio (startDestination)
    NavHost(
        navController = navController,
        //startDestination = Screen.DetallePerro.crearRuta("0edfe707-cc43-4bcf-bd43-a7f61e312e17"),
        startDestination = Screen.Catalogo.ruta,
    ) {
        // El login entrega la sesión al mismo ViewModel que usa la cuenta.
        composable(route = Screen.Login.ruta) {
            LoginRoute(
                onBackToCatalog = navController::irAlCatalogo,
                onExploreWithoutAccount = navController::irAlCatalogo,
                onRegister = {
                    navController.navigate(Screen.Registro.ruta) { launchSingleTop = true }
                },
                onAuthenticated = { sesion ->
                    sesionViewModel.autenticado(sesion)
                    navController.navigate(Screen.Cuenta.ruta) {
                        popUpTo(Screen.Catalogo.ruta) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }

        // Crear una cuenta termina en el login; el registro no trae un token.
        composable(route = Screen.Registro.ruta) {
            RegistroRoute(
                onCancel = navController::irAlLogin,
                onRegistered = {
                    Toast.makeText(context, "Cuenta creada. Inicia sesión.", Toast.LENGTH_LONG).show()
                    navController.irAlLogin()
                },
            )
        }

        // Conecto los botones de cuenta sin meter peticiones de red en la vista.
        composable(route = Screen.Cuenta.ruta) {
            ConSesion(estadoSesion, navController, sesionViewModel) {
                CuentaScreen(
                    state = estadoSesion,
                    onRetry = sesionViewModel::consultarPerfil,
                    onCatalog = navController::irAlCatalogo,
                    onLogout = { sesionViewModel.salir() },
                    onLogin = navController::irAlLogin,
                )
            }
        }

        // ========= Pantallas Catalogo ========
        composable(route=Screen.Catalogo.ruta){
            PerroScreen(
                onDogClick = { dogId ->
                    navController.navigate(
                        Screen.DetallePerro.crearRuta(dogId)
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
                onAdoptClick = { dogId ->
                    navController.navigate(Screen.Requisitos.crearRuta(dogId))
                },
            )
        }

        // ========= Pantallas Solicitudes ========

        // SOLICITUDES: Paso 1 - Requisitos
        composable(
            route = Screen.Requisitos.ruta,
            arguments =
                listOf(
                    navArgument("idPerro") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            // Estos datos necesitan una sesión válida.
            ConSesion(estadoSesion, navController, sesionViewModel) { sesion ->
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
            ConSesion(estadoSesion, navController, sesionViewModel) { _ ->
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
            ConSesion(estadoSesion, navController, sesionViewModel) { _ ->
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
            ConSesion(estadoSesion, navController, sesionViewModel) { _ ->
                CompromisoRoute(
                    onBack = { navController.navigateUp() },
                    onIrADocumentoss = {
                        // navegar a documentos
                    },
                )
            }
        }

        // ========= Pantallas Donativos ========

        // DONATIVOS - HU46: registrar donativo económico
        composable(route = Screen.DonarDinero.ruta) {
            // Estos datos necesitan una sesión válida.
            ConSesion(estadoSesion, navController, sesionViewModel) { sesion ->
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
            ConSesion(estadoSesion, navController, sesionViewModel) { _ ->
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
            ConSesion(estadoSesion, navController, sesionViewModel) { sesion ->
                DatosFiscalesRoute(
                    idUsuario = sesion.usuario.id, // El id sale de la sesión actual.
                    onBack = { navController.navigateUp() },
                    onNavSelect = navController::irAOpcionBarra,
                )
            }
        }

        // ========= Pantallas Voluntariado ========
// Programas y Confirmación comparten la misma instancia de ProgramasViewModel
        navigation(
            startDestination = Screen.Programas.ruta,
            route = Screen.GrafoVoluntariado.ruta,
        ) {
            composable(route = Screen.Programas.ruta) { entry ->
                val grafo = remember(entry) { navController.getBackStackEntry(Screen.GrafoVoluntariado.ruta) }
                ProgramasScreen(
                    viewModel = hiltViewModel(grafo),
                    onBackClick = { navController.navigateUp() },
                    onNavSelect = navController::irAOpcionBarra,
                    onInscripcionExitosa = {
                        navController.navigate(Screen.ConfirmacionInscripcion.ruta) { launchSingleTop = true }
                    },
                )
            }

            composable(route = Screen.ConfirmacionInscripcion.ruta) { entry ->
                val grafo = remember(entry) { navController.getBackStackEntry(Screen.GrafoVoluntariado.ruta) }
                ConfirmacionInscripcionRoute(
                    viewModel = hiltViewModel(grafo),
                    onBack = { navController.navigateUp() },
                    onExplorarProgramas = {
                        navController.popBackStack(Screen.Programas.ruta, inclusive = false)
                    },
                    onNavSelect = navController::irAOpcionBarra,
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
