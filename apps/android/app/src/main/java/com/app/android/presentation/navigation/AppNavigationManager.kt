package com.app.android.presentation.navigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.android.presentation.view.atomicDesign.page.TuHogarScreen
import com.app.android.presentation.view.viewsCatalogo.DogDetailScreenView
import com.app.android.presentation.view.viewsSolicitudes.DatosGeneralesRoute
import com.app.android.presentation.view.viewsSolicitudes.RequisitosRoute
import com.app.android.presentation.view.viewsPerfiles.CuentaScreen
import com.app.android.presentation.view.viewsPerfiles.InicioSesionScreen
import com.app.android.presentation.view.viewsPerfiles.LoginRoute
import com.app.android.presentation.view.viewsPerfiles.RegistroRoute
import com.app.android.presentation.viewmodel.viewModelPerfiles.SesionViewModel
import com.app.android.presentation.view.viewsVoluntariado.ProgramasScreen


// Entrada al detalle existente del módulo de catálogo.
private const val PERRO_INICIAL = "0edfe707-cc43-4bcf-bd43-a7f61e312e17"

@Composable
fun AppNavigation(sesionViewModel: SesionViewModel = hiltViewModel()) {
    // 1. El NavController que controla el estado de la navegación
    val navController = rememberNavController()
    val sesionState by sesionViewModel.state.collectAsState()
    var adopcionPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    fun irAlInicio(ruta: String) {
        navController.navigate(ruta) {
            popUpTo(navController.graph.id) { inclusive = false }
            launchSingleTop = true
        }
    }
    fun catalogo() = irAlInicio(Screen.DetallePerro.crearRuta(PERRO_INICIAL))
    fun invitado() {
        sesionViewModel.salir(onFinished = { adopcionPendiente = null; catalogo() }, notificarServidor = false)
    }

    // 2. El NavHost donde defines la pantalla de inicio (startDestination)
    NavHost(
        navController = navController,
        startDestination = Screen.Inicio.ruta,
    ) {
        composable(Screen.Inicio.ruta) {
            LaunchedEffect(sesionState.comprobando, sesionState.sesion, sesionState.error) {
                if (!sesionState.comprobando && (sesionState.sesion != null || sesionState.error == null)) {
                    irAlInicio(if (sesionState.sesion != null) Screen.Cuenta.ruta else Screen.Login.ruta)
                }
            }
            InicioSesionScreen(sesionState, sesionViewModel::restaurar, ::invitado)
        }
        composable(Screen.Login.ruta) {
            LoginRoute(
                onBackToCatalog = ::invitado, onExploreWithoutAccount = ::invitado,
                onRegister = { navController.navigate(Screen.Registro.ruta) { launchSingleTop = true } },
                onAuthenticated = { sesion ->
                    sesionViewModel.autenticado(sesion)
                    val perro = adopcionPendiente
                    adopcionPendiente = null
                    irAlInicio(perro?.let(Screen.Requisitos::crearRuta) ?: Screen.Cuenta.ruta)
                },
            )
        }
        composable(Screen.Registro.ruta) {
            RegistroRoute(onCancel = { if (!navController.popBackStack()) irAlInicio(Screen.Login.ruta) }, onAuthenticated = { sesion ->
                sesionViewModel.autenticado(sesion)
                val perro = adopcionPendiente
                adopcionPendiente = null
                irAlInicio(perro?.let(Screen.Requisitos::crearRuta) ?: Screen.Cuenta.ruta)
            })
        }
        composable(Screen.Cuenta.ruta) {
            LaunchedEffect(sesionState.sesion, sesionState.comprobando) {
                if (!sesionState.comprobando && sesionState.sesion == null) irAlInicio(Screen.Login.ruta)
            }
            CuentaScreen(sesionState, sesionViewModel::consultarPerfil, ::catalogo, {
                sesionViewModel.salir(onFinished = { irAlInicio(Screen.Login.ruta) })
            })
        }
        // ========= Pantallas Catalogo ========

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
                onAdoptClick = { dogId ->
                    if (sesionState.sesion == null) {
                        adopcionPendiente = dogId
                        navController.navigate(Screen.Login.ruta) { launchSingleTop = true }
                    } else navController.navigate(Screen.Requisitos.crearRuta(dogId))
                },
                onAccountClick = { navController.navigate(if (sesionState.sesion == null) Screen.Login.ruta else Screen.Cuenta.ruta) { launchSingleTop = true } },
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
            val idPerro = backStackEntry.arguments?.getString("idPerro") ?: ""
            val idUsuario = sesionState.sesion?.usuario?.id
            if (idUsuario == null && sesionState.comprobando) {
                InicioSesionScreen(sesionState, sesionViewModel::restaurar, ::invitado)
            } else if (idUsuario == null) {
                LaunchedEffect(Unit) { adopcionPendiente = idPerro; irAlInicio(Screen.Login.ruta) }
            } else RequisitosRoute(
                idUsuario = idUsuario,
                idPerro = idPerro,
                onBack = { navController.navigateUp() },
                onIrADatosGenerales = {
                    navController.navigate(Screen.DatosGenerales.ruta)
                },
            )
        }

        // SOLICITUDES: Paso 2 - Datos generales
        composable(route = Screen.DatosGenerales.ruta) {
            val idUsuario = sesionState.sesion?.usuario?.id
            if (idUsuario == null && sesionState.comprobando) {
                InicioSesionScreen(sesionState, sesionViewModel::restaurar, ::invitado)
            } else if (idUsuario == null) {
                LaunchedEffect(Unit) { irAlInicio(Screen.Login.ruta) }
            } else DatosGeneralesRoute(
                onBack = { navController.navigateUp() },
                onIrATuHogar = {
                    navController.navigate(Screen.TuHogar.ruta)
                },
            )
        }

        // SOLICITUDES: Paso 3 - Tu hogar (Facu: aquí va TuHogarRoute cuando esté)
        composable(route = Screen.TuHogar.ruta) {
            TuHogarScreen(
                onBackClick = { navController.navigateUp() },
                onContinuarClick = {},
            )
        }

        // ========= Pantallas Voluntariado ========
        composable(route = Screen.Programas.ruta) {
            ProgramasScreen(onBackClick = { navController.popBackStack() })
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
