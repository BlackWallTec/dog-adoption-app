package com.app.android.presentation.navigation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.android.presentation.view.atomicDesign.page.DonativoEnviadoScreen
import com.app.android.presentation.view.atomicDesign.page.TuHogarScreen
import com.app.android.presentation.view.viewsCatalogo.DogDetailScreenView
import com.app.android.presentation.view.viewsCatalogo.PerroScreen
import com.app.android.presentation.view.viewsDonativos.DatosFiscalesRoute
import com.app.android.presentation.view.viewsDonativos.DonarDineroRoute
import com.app.android.presentation.view.viewsSolicitudes.CompromisoRoute
import com.app.android.presentation.view.viewsSolicitudes.DatosGeneralesRoute
import com.app.android.presentation.view.viewsSolicitudes.MisSolicitudesRoute
import com.app.android.presentation.view.viewsSolicitudes.RequisitosRoute
import com.app.android.presentation.view.viewsSolicitudes.TuHogarRoute
import com.app.android.presentation.view.viewsVoluntariado.ConfirmacionInscripcionRoute
import com.app.android.presentation.view.viewsVoluntariado.ProgramasScreen

// TEMPORAL: usuario de prueba (Abraham) mientras no hay login.
// Cuando exista login, este id vendra de la sesion del usuario.
private const val ID_USUARIO_PRUEBA = "f31fc2d1-c4d8-46a2-9a52-0ee84d637217"

// Barra de abajo: 0 Catálogo, 1 Ayudar, 2 Donar, 3 Solicitudes, 4 Ajustes.
private fun NavController.irAOpcionBarra(opcion: Int) {
    val destino =
        when (opcion) {
            1 -> Screen.GrafoVoluntariado.ruta
            2 -> Screen.DonarDinero.ruta
            3 -> Screen.MisSolicitudes.ruta
            4 -> Screen.DatosFiscales.ruta
            else -> null
        }
    when {
        opcion == 0 -> {
            popBackStack(Screen.DetallePerro.ruta, inclusive = false)
        }

        destino != null -> {
            navigate(destino) {
                popUpTo(Screen.DetallePerro.ruta) // para que atrás regrese al inicio y no se apilen
                launchSingleTop = true
            }
        }
    }
}

@Composable
fun AppNavigation() {
    // 1. El NavController que controla el estado de la navegación
    val navController = rememberNavController()

    // 2. El NavHost donde defines la pantalla de inicio (startDestination)
    NavHost(
        navController = navController,
        // startDestination = Screen.DetallePerro.crearRuta("0edfe707-cc43-4bcf-bd43-a7f61e312e17"),
        startDestination = Screen.Catalogo.ruta,
    ) {
        // ========= Pantallas Catalogo ========
        composable(route = Screen.Catalogo.ruta) {
            PerroScreen(
                onDogClick = { dogId ->
                    navController.navigate(
                        Screen.DetallePerro.crearRuta(dogId),
                    )
                },
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

        // SOLICITUDES - HU06: Mis solicitudes

        // SOLICITUDES: Paso 1 - Requisitos
        composable(
            route = Screen.Requisitos.ruta,
            arguments =
                listOf(
                    navArgument("idPerro") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val idPerro = backStackEntry.arguments?.getString("idPerro") ?: ""

            RequisitosRoute(
                idUsuario = ID_USUARIO_PRUEBA,
                idPerro = idPerro,
                onBack = { navController.navigateUp() },
                onIrADatosGenerales = {
                    navController.navigate(Screen.DatosGenerales.ruta)
                },
            )
        }

        // SOLICITUDES: Paso 2 - Datos generales
        composable(route = Screen.DatosGenerales.ruta) {
            DatosGeneralesRoute(
                onBack = { navController.navigateUp() },
                onIrATuHogar = {
                    navController.navigate(Screen.TuHogar.ruta)
                },
            )
        }

        // SOLICITUDES: Paso 3 - Tu Hogar
        composable(route = Screen.TuHogar.ruta) {
            TuHogarRoute(
                onBack = { navController.navigateUp() },
                onIrACompromiso = {
                    navController.navigate(Screen.Compromiso.ruta)
                },
            )
        }

        // Paso 4 - Compromiso
        composable(route = Screen.Compromiso.ruta) {
            CompromisoRoute(
                onBack = { navController.navigateUp() },
                onIrADocumentoss = {
                    // navegar a documentos
                },
            )
        }

        // SOLICITUDES - HU04: Mis solicitudes (solo lectura)
        composable(route = Screen.MisSolicitudes.ruta) {
            MisSolicitudesRoute(
                onEditar = {
                    // navegar a editar documentos (HU de edición, todavía no existe)
                },
                onVerCatalogo = { navController.popBackStack(Screen.Catalogo.ruta, inclusive = false) },
                onNavSelect = navController::irAOpcionBarra,
            )
        }

        // ========= Pantallas Donativos ========

        // DONATIVOS - HU46: registrar donativo económico
        composable(route = Screen.DonarDinero.ruta) {
            DonarDineroRoute(
                idUsuario = ID_USUARIO_PRUEBA,
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

        composable(
            route = Screen.DonativoEnviado.ruta,
            arguments =
                listOf(
                    navArgument("monto") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val monto = backStackEntry.arguments?.getString("monto") ?: ""
            DonativoEnviadoScreen(
                monto = "\$$monto MXN",
                onBackClick = { navController.popBackStack() },
                onVolverDonarClick = { navController.navigate(Screen.DonarDinero.ruta) },
                onVerMisDonativosClick = {}, // Mis donativos todavía no existe
                onNavSelect = navController::irAOpcionBarra,
            )
        }

        // DONATIVOS - HU45: datos fiscales del donante
        composable(route = Screen.DatosFiscales.ruta) {
            DatosFiscalesRoute(
                idUsuario = ID_USUARIO_PRUEBA,
                onBack = { navController.navigateUp() },
                onNavSelect = navController::irAOpcionBarra,
            )
        }

        // ========= Pantallas Donativos ========

        // DONATIVOS - HU46: registrar donativo económico
        composable(route = Screen.DonarDinero.ruta) {
            DonarDineroRoute(
                idUsuario = ID_USUARIO_PRUEBA,
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

        composable(
            route = Screen.DonativoEnviado.ruta,
            arguments =
                listOf(
                    navArgument("monto") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val monto = backStackEntry.arguments?.getString("monto") ?: ""
            DonativoEnviadoScreen(
                monto = "\$$monto MXN",
                onBackClick = { navController.popBackStack() },
                onVolverDonarClick = { navController.navigate(Screen.DonarDinero.ruta) },
                onVerMisDonativosClick = {}, // Mis donativos todavía no existe
                onNavSelect = navController::irAOpcionBarra,
            )
        }

        // DONATIVOS - HU45: datos fiscales del donante
        composable(route = Screen.DatosFiscales.ruta) {
            DatosFiscalesRoute(
                idUsuario = ID_USUARIO_PRUEBA,
                onBack = { navController.navigateUp() },
                onNavSelect = navController::irAOpcionBarra,
            )
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
