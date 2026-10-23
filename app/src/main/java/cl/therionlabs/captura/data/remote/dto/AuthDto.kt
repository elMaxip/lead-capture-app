package cl.therionlabs.captura.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String,
)

@Serializable
data class LoginResponseDto(
    val token: String,
    val usuario: UsuarioDto,
)

@Serializable
data class UsuarioDto(
    val username: String,
    val nombre: String,
    val rol: String,
)
