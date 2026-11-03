package com.app.android.presentation.navigation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.android.presentation.view.atomicDesign.page.TuHogarScreen
import com.app.android.presentation.view.viewsCatalogo.dogDetailScreen
import com.app.android.presentation.view.viewsSolicitudes.DatosGeneralesRoute
import com.app.android.presentation.view.viewsSolicitudes.RequisitosRoute
import com.app.android.presentation.view.viewsVoluntariado.ProgramasScreen


// TEMPORAL: usuario de prueba (Abraham) mientras no hay login.
// Cuando exista login, este id vendra de la sesion del usuario.
private const val ID_USUARIO_PRUEBA = "f31fc2d1-c4d8-46a2-9a52-0ee84d637217"

@Composable
fun AppNavigation() {
    // 1. El NavController que controla el estado de la navegación
    val navController = rememberNavController()

    // 2. El NavHost donde defines la pantalla de inicio (startDestination)
    NavHost(
        navController = navController,
        // TEMPORAL (HU39A): para probar la pantalla de programas.
        // startDestination = Screen.DetallePerro.crearRuta("3f2c0f61-f44f-491f-8931-d964badba024"),
        startDestination = Screen.Programas.ruta,
    ) {
        // PANTALLA 2: El detalle del perro (La pantalla que ya hiciste)
        composable(
            route = Screen.DetallePerro.ruta,
            arguments =
                listOf(
                    navArgument("dogId") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            // Extraemos de forma segura el ID que viaja en la ruta
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""

            // Llamamos a tu pantalla pasándole el ID extraído
            dogDetailScreen(dogId = dogId)
        }

        // HU39A: catálogo de programas de voluntariado
        composable(route = Screen.Programas.ruta) {
            ProgramasScreen(onBackClick = { navController.popBackStack() })
        }

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
                onIrADatosGenerales = { idSolicitud ->
                    navController.navigate(Screen.DatosGenerales.crearRuta(idSolicitud))
                },
            )
        }

        // SOLICITUDES: Paso 2 - Datos generales
        composable(
            route = Screen.DatosGenerales.ruta,
            arguments =
                listOf(
                    navArgument("idSolicitud") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val idSolicitud = backStackEntry.arguments?.getString("idSolicitud") ?: ""

            DatosGeneralesRoute(
                idSolicitud = idSolicitud,
                idUsuario = ID_USUARIO_PRUEBA,
                onBack = { navController.navigateUp() },
                onIrATuHogar = {
                    navController.navigate(Screen.TuHogar.crearRuta(idSolicitud))
                },
            )
        }

        // SOLICITUDES: Paso 3 - Tu hogar (metqnlo team)
        composable(
            route = Screen.TuHogar.ruta,
            arguments =
                listOf(
                    navArgument("idSolicitud") { type = NavType.StringType },
                ),
        ) {
            TuHogarScreen(
                onBackClick = { navController.navigateUp() },
                onContinuarClick = {},
            )
        }

        // Pantilla para nueva pantalla
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
        // editDogScreen(dogId = dogId, categoria = categoria)
        // }
    }
}