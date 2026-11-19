package com.app.android.voluntariado

import androidx.lifecycle.SavedStateHandle
import com.app.android.domain.entities.entitiesVoluntariado.AccionDetalle
import com.app.android.domain.entities.entitiesVoluntariado.DetallePrograma
import com.app.android.domain.entities.entitiesVoluntariado.MarcaPrograma
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.app.android.domain.repoInterface.repoIntVoluntariado.DetalleProgramaRepository
import com.app.android.domain.useCases.useCasesVoluntariado.ObtenerDetalleProgramaUseCase
import com.app.android.presentation.viewmodel.viewModelVoluntariado.DetalleProgramaUiState
import com.app.android.presentation.viewmodel.viewModelVoluntariado.DetalleProgramaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/*
 * PRUEBAS DE DetalleProgramaViewModel (HU36)
 *
 * El ViewModel lee el id de la ruta (SavedStateHandle), pide el detalle al caso de uso
 * y publica un estado: Loading, Success (con la acción del botón), NoEncontrado o Error.
 * Usamos RELOJ_FIJO para que "ahora" sea siempre AHORA.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetalleProgramaViewModelTest {
    // Fake del repositorio: responde lo que le pidamos y anota con qué id lo llamaron.
    private class RepositorioDetalleFalso(
        var respuesta: Resultado<DetallePrograma> = Resultado.Exito(detalleDePrueba()),
    ) : DetalleProgramaRepository {
        val ids = mutableListOf<String>()

        override suspend fun obtenerDetalle(idPrograma: String): Resultado<DetallePrograma> {
            ids.add(idPrograma)
            return respuesta
        }
    }

    @Before
    fun instalarHiloPrincipalDePrueba() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun quitarHiloPrincipalDePrueba() {
        Dispatchers.resetMain()
    }

    private fun viewModel(repositorio: RepositorioDetalleFalso = RepositorioDetalleFalso()) =
        DetalleProgramaViewModel(
            SavedStateHandle(mapOf(DetalleProgramaViewModel.ARG_ID_PROGRAMA to "programa-1")),
            ObtenerDetalleProgramaUseCase(repositorio),
            RELOJ_FIJO,
        )

    @Test
    fun `usa el id que llega en la ruta`() {
        val repositorio = RepositorioDetalleFalso()

        viewModel(repositorio)

        assertEquals(listOf("programa-1"), repositorio.ids)
    }

    @Test
    fun `éxito - publica el detalle con la acción del botón`() {
        val vm = viewModel()

        assertEquals(DetalleProgramaUiState.Success(detalleDePrueba(), AccionDetalle.Inscribirme), vm.uiState.value)
    }

    @Test
    fun `la acción se calcula con el reloj inyectado, no con la hora real`() {
        // Empieza justo AHORA: con el reloj fijo debe salir En curso
        val detalle = detalleDePrueba(programa = programaValido().copy(fechaInicio = AHORA))
        val vm = viewModel(RepositorioDetalleFalso(Resultado.Exito(detalle)))

        val estado = vm.uiState.value as DetalleProgramaUiState.Success
        assertEquals(AccionDetalle.SinAccion(MarcaPrograma.EN_CURSO), estado.accion)
    }

    @Test
    fun `programa no encontrado - estado propio para volver al catálogo`() {
        val vm = viewModel(RepositorioDetalleFalso(Resultado.Error(VoluntariadoError.ProgramaNoEncontrado)))

        assertEquals(DetalleProgramaUiState.NoEncontrado, vm.uiState.value)
    }

    @Test
    fun `sin conexión - mensaje de internet para reintentar`() {
        val vm = viewModel(RepositorioDetalleFalso(Resultado.Error(VoluntariadoError.SinConexion)))

        assertEquals(
            DetalleProgramaUiState.Error("Sin conexión. Revisa tu internet e inténtalo de nuevo."),
            vm.uiState.value,
        )
    }

    @Test
    fun `cualquier otro error - mensaje genérico, nunca un detalle a medias`() {
        val errores = TODOS_LOS_ERRORES - VoluntariadoError.ProgramaNoEncontrado - VoluntariadoError.SinConexion

        for (error in errores) {
            val vm = viewModel(RepositorioDetalleFalso(Resultado.Error(error)))

            assertEquals(
                "error=$error",
                DetalleProgramaUiState.Error("No pudimos cargar el programa. Inténtalo de nuevo."),
                vm.uiState.value,
            )
        }
    }

    @Test
    fun `cargar otra vez refleja cambios recientes (por ejemplo, ya inscrito)`() {
        val repositorio = RepositorioDetalleFalso()
        val vm = viewModel(repositorio)

        repositorio.respuesta = Resultado.Exito(detalleDePrueba(programa = programaValido().copy(usuarioInscrito = true)))
        vm.cargar()

        assertEquals(2, repositorio.ids.size)
        assertEquals(AccionDetalle.YaInscrito, (vm.uiState.value as DetalleProgramaUiState.Success).accion)
    }
}
