package com.app.android.presentation.navigation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.android.presentation.view.viewsCatalogo.dogDetailScreen
import com.app.android.presentation.view.atomicDesign.page.DonarDineroScreen
import com.app.android.presentation.view.atomicDesign.page.DonativoEnviadoScreen

@Composable
fun AppNavigation() {
    // 1. El NavController que controla el estado de la navegación
    val navController = rememberNavController()

    // 2. El NavHost donde defines la pantalla de inicio (startDestination)
    NavHost(
        navController = navController,
        startDestination = Screen.DetallePerro.crearRuta("3f2c0f61-f44f-491f-8931-d964badba024"),
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
        // Donativos - HU46: registrar donativo económico
        composable(route = Screen.DonarDinero.ruta) {
            DonarDineroScreen(
                onBackClick = { navController.popBackStack() },
                onEnviarClick = { monto ->
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
