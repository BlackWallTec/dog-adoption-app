package com.app.android.domain.useCases.useCasesSolicitudes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.app.android.data.TokenProvider
import com.app.android.data.repoImp.repoImpSolicitudes.SolicitudHttpException
import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.domain.entities.entitiesSolicitudes.PickedFile
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject

class EnviarSolicitudUseCase
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val borradorRepo: BorradorRepositoryInt,
        private val solicitudRepo: SolicitudRepositoryInt,
        private val validarBorradorUseCase: ValidarBorradorUseCase,
        private val tokenProvider: TokenProvider,
    ) {
        constructor(
            context: Context,
            borradorRepo: BorradorRepositoryInt,
            solicitudRepo: SolicitudRepositoryInt,
            validarBorradorUseCase: ValidarBorradorUseCase,
            tokenProvider: TokenProvider,
            fileReader: (String) -> ByteArray?,
        ) : this(context, borradorRepo, solicitudRepo, validarBorradorUseCase, tokenProvider) {
            this.customFileReader = fileReader
        }

        private var customFileReader: ((String) -> ByteArray?)? = null

        private val effectiveFileReader: (String) -> ByteArray? =
            { uriStr ->
                customFileReader?.invoke(uriStr) ?: try {
                    val uri = Uri.parse(uriStr)
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } catch (_: Exception) {
                    null
                }
            }

        suspend operator fun invoke(): String {
            val validacion = validarBorradorUseCase()
            if (validacion is ValidacionBorradorResult.Invalido) {
                throw IllegalStateException(validacion.mensaje)
            }

            val borrador =
                borradorRepo.borrador.value
                    ?: throw IllegalStateException("No hay una solicitud en borrador activa.")

            try {
                // 1. Create or reuse draft
                val solicitudId = solicitudRepo.crearOReutilizarBorrador(borrador)

                val docs =
                    borrador.documentos
                        ?: throw IllegalStateException("Faltan los documentos adjuntos.")

                val ineLocal =
                    docs.identificacion
                        ?: throw IllegalStateException("Falta el documento de identificación (INE).")
                val domLocal =
                    docs.comprobanteDomicilio
                        ?: throw IllegalStateException("Falta el comprobante de domicilio.")

                // 2. Compress both files to no more than 2 MB with aligned metadata
                val (ineCompressed, inePickedFile) = compressFileToMax2Mb(ineLocal.uri, ineLocal.mimeType, ineLocal.nombre)
                val (domCompressed, domPickedFile) = compressFileToMax2Mb(domLocal.uri, domLocal.mimeType, domLocal.nombre)

                // 3. Upload INE and Domicilio
                solicitudRepo.subirDocumento(
                    solicitudId = solicitudId,
                    type = DocumentType.INE,
                    file = inePickedFile,
                    bytes = ineCompressed,
                )

                solicitudRepo.subirDocumento(
                    solicitudId = solicitudId,
                    type = DocumentType.DOMICILIO,
                    file = domPickedFile,
                    bytes = domCompressed,
                )

                // 4. Call finalize
                solicitudRepo.finalizarSolicitud(solicitudId)

                // 5. Clear only the locally saved draft after success
                borradorRepo.limpiar()

                return solicitudId
            } catch (e: Exception) {
                if (e is SolicitudHttpException && e.statusCode == 401) {
                    runCatching { tokenProvider.clear() }
                }
                // Keep the draft on network/document failures
                throw e
            }
        }

        private fun compressFileToMax2Mb(
            uriString: String,
            mimeType: String,
            originalName: String,
        ): Pair<ByteArray, PickedFile> {
            val bytes =
                effectiveFileReader(uriString)
                    ?: throw IOException("No se pudo leer el archivo: $uriString")

            val maxBytes = 2 * 1024 * 1024 // 2 MB
            val isImage = mimeType.contains("image", ignoreCase = true) || isImageUri(uriString)

            if (bytes.size <= maxBytes) {
                return Pair(
                    bytes,
                    PickedFile(uri = uriString, name = originalName, sizeBytes = bytes.size.toLong(), mimeType = mimeType),
                )
            }

            if (!isImage) {
                throw IllegalStateException("El archivo supera los 2 MB y no es una imagen. Por favor seleccione un archivo más pequeño.")
            }

            val bitmap =
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: throw IllegalStateException("El archivo supera los 2 MB y no pudo procesarse como imagen. Por favor recapture o seleccione una imagen más pequeña.")

            try {
                var quality = 90
                var stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

                while (stream.size() > maxBytes && quality > 10) {
                    stream = ByteArrayOutputStream()
                    quality -= 10
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                }

                if (stream.size() <= maxBytes) {
                    val compressedBytes = stream.toByteArray()
                    val nameWithoutExt = originalName.substringBeforeLast('.', originalName)
                    val newName = "$nameWithoutExt.jpg"
                    val newPickedFile =
                        PickedFile(
                            uri = uriString,
                            name = newName,
                            sizeBytes = compressedBytes.size.toLong(),
                            mimeType = "image/jpeg",
                        )
                    return Pair(compressedBytes, newPickedFile)
                } else {
                    throw IllegalStateException("La imagen no pudo reducirse a menos de 2 MB. Por favor recapture o seleccione una imagen más pequeña.")
                }
            } finally {
                bitmap.recycle()
            }
        }

        private fun isImageUri(uriString: String): Boolean {
            return try {
                val type = context.contentResolver.getType(Uri.parse(uriString))
                type?.startsWith("image/") == true
            } catch (_: Exception) {
                false
            }
        }
    }
