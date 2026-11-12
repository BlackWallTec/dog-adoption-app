package com.app.android.catalogo

import com.app.android.domain.entities.entitiesCatalogo.FinalFeliz
import com.app.android.domain.repoInterface.repoIntCatalogo.FinalFelizRepository
import com.app.android.domain.useCases.useCasesCatalogo.GetFinalesFelicesUseCase
import com.app.android.presentation.viewmodel.viewModelCatalogo.FinalesFelicesUiState
import com.app.android.presentation.viewmodel.viewModelCatalogo.FinalesFelicesViewModel
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FinalesFelicesViewModelTest {
    private class RepoFalso(
        var finales: List<FinalFeliz> = emptyList(),
        var falla: Exception? = null,
    ) : FinalFelizRepository {
        override suspend fun getFinalesFelices(): List<FinalFeliz> {
            falla?.let { throw it }
            return finales
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

    @Test
    fun `vacios - sin finales publicables la seccion queda vacia`() {
        val vm = viewModel(RepoFalso(listOf(final(publicado = false), final(historia = ""))))

        assertEquals(FinalesFelicesUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `exito - el caso de uso deja fuera al perro que no esta publicado`() {
        val vm =
            viewModel(
                RepoFalso(
                    listOf(
                        final(id = "luna"),
                        final(id = "sol", estado = "Disponible"),
                    ),
                ),
            )

        val estado = vm.uiState.value as FinalesFelicesUiState.Success
        assertEquals(listOf("luna"), estado.finales.map { it.idPerro })
    }

    @Test
    fun `error - una falla de red avisa y reintentar recupera la lista`() {
        val repo = RepoFalso(falla = IOException("sin red"))
        val vm = viewModel(repo)

        val error = vm.uiState.value as FinalesFelicesUiState.Error
        assertEquals("No se pudieron cargar los finales felices", error.mensaje)

        repo.falla = null
        repo.finales = listOf(final())
        vm.cargar()

        assertTrue(vm.uiState.value is FinalesFelicesUiState.Success)
    }

    private fun viewModel(repositorio: FinalFelizRepository) =
        FinalesFelicesViewModel(GetFinalesFelicesUseCase(repositorio))

    private fun final(
        id: String = "luna",
        historia: String = "Ya tiene hogar.",
        estado: String = "Adoptado",
        publicado: Boolean = true,
    ) = FinalFeliz(
        idPerro = id,
        nombre = "Luna",
        fotoHogarUrl = "https://foto",
        historia = historia,
        fechaAdopcion = "2026-03-01",
        estado = estado,
        publicado = publicado,
    )
}
