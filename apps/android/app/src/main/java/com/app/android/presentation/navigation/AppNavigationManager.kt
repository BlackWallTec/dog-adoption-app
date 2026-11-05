package com.app.android.presentation.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.android.presentation.view.atomicDesign.page.DonarDineroScreen
import com.app.android.presentation.view.atomicDesign.page.DonativoEnviadoScreen
import com.app.android.presentation.view.atomicDesign.page.TuHogarScreen
import com.app.android.presentation.view.viewsCatalogo.DogDetailScreenView
import com.app.android.presentation.view.viewsDonativos.DatosFiscalesRoute
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
        startDestination = Screen.DetallePerro.crearRuta("0edfe707-cc43-4bcf-bd43-a7f61e312e17"),
    ) {
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

        // SOLICITUDES: Paso 3 - Tu hogar (Facu: aquí va TuHogarRoute cuando esté)
        composable(route = Screen.TuHogar.ruta) {
            TuHogarScreen(
                onBackClick = { navController.navigateUp() },
                onContinuarClick = {},
            )
        }

        // ========= Pantallas Donativos ========

        // DONATIVOS - HU46: registrar donativo económico
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

        // DONATIVOS - HU45: datos fiscales del donante
        composable(route = Screen.DatosFiscales.ruta) {
            DatosFiscalesRoute(
                idUsuario = ID_USUARIO_PRUEBA,
                onBack = { navController.navigateUp() },
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
