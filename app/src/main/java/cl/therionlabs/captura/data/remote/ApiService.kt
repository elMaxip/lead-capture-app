package cl.therionlabs.captura.data.remote

import cl.therionlabs.captura.data.remote.dto.CatalogoDto
import cl.therionlabs.captura.data.remote.dto.SyncRequestDto
import cl.therionlabs.captura.data.remote.dto.SyncResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("api/catalogo")
    suspend fun obtenerCatalogo(): CatalogoDto

    @POST("api/prospectos/sync")
    suspend fun sincronizar(@Body request: SyncRequestDto): SyncResponseDto
}
