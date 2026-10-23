package cl.therionlabs.captura.data.remote

import cl.therionlabs.captura.data.remote.dto.CatalogoDto
import cl.therionlabs.captura.data.remote.dto.LoginRequestDto
import cl.therionlabs.captura.data.remote.dto.LoginResponseDto
import cl.therionlabs.captura.data.remote.dto.ProspectoConsolidadoDto
import cl.therionlabs.captura.data.remote.dto.SyncRequestDto
import cl.therionlabs.captura.data.remote.dto.SyncResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/login")
    suspend fun iniciarSesion(@Body request: LoginRequestDto): LoginResponseDto

    @POST("api/auth/logout")
    suspend fun cerrarSesion(@Header("Authorization") autorizacion: String)

    @GET("api/catalogo")
    suspend fun obtenerCatalogo(): CatalogoDto

    @POST("api/prospectos/sync")
    suspend fun sincronizar(@Body request: SyncRequestDto): SyncResponseDto

    @GET("api/prospectos")
    suspend fun listarProspectos(): List<ProspectoConsolidadoDto>
}
