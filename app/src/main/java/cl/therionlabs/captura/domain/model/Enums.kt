package cl.therionlabs.captura.domain.model

enum class NivelEducativo(val etiqueta: String) {
    BASICA("Básica"),
    MEDIA("Media"),
    TECNICO_PROFESIONAL("Técnico profesional"),
    UNIVERSITARIA("Universitaria"),
    POSTGRADO("Postgrado"),
}

enum class Modalidad(val etiqueta: String) {
    ONLINE("Online"),
    PRESENCIAL("Presencial"),
}

enum class CanalContacto(val etiqueta: String) {
    EMAIL("Correo"),
    TELEFONO("Llamada"),
    WHATSAPP("WhatsApp"),
}

enum class EstadoSync(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    SINCRONIZADO("Sincronizado"),
}
