package cl.therionlabs.captura

import cl.therionlabs.captura.domain.model.Modalidad
import cl.therionlabs.captura.domain.validation.ProspectoValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProspectoValidatorTest {

    @Test
    fun nombreValido() {
        assertNull(ProspectoValidator.validarNombre("Camila Rojas"))
        assertNull(ProspectoValidator.validarNombre("Al"))
        assertNull(ProspectoValidator.validarNombre("José Núñez"))
        assertNull(ProspectoValidator.validarNombre("a".repeat(100)))
    }

    @Test
    fun nombreVacioOSoloEspacios() {
        assertEquals(ProspectoValidator.MSG_NOMBRE_VACIO, ProspectoValidator.validarNombre(""))
        assertEquals(ProspectoValidator.MSG_NOMBRE_VACIO, ProspectoValidator.validarNombre("   "))
    }

    @Test
    fun nombreFueraDeRango() {
        assertEquals(ProspectoValidator.MSG_NOMBRE_CORTO, ProspectoValidator.validarNombre("A"))
        assertEquals(ProspectoValidator.MSG_NOMBRE_CORTO, ProspectoValidator.validarNombre(" A "))
        assertEquals(ProspectoValidator.MSG_NOMBRE_LARGO, ProspectoValidator.validarNombre("a".repeat(101)))
    }

    @Test
    fun emailValido() {
        assertNull(ProspectoValidator.validarEmail("camila.rojas@example.com"))
        assertNull(ProspectoValidator.validarEmail("ana+ferias@sub.dominio.cl"))
        assertNull(ProspectoValidator.validarEmail("  ana@example.com  "))
    }

    @Test
    fun emailInvalido() {
        listOf(
            "",
            "camila",
            "camila@",
            "@example.com",
            "camila@example",
            "camila rojas@example.com",
            "camila..rojas@example.com",
            ".camila@example.com",
            "camila@example.c",
            "a".repeat(140) + "@example.com",
        ).forEach {
            assertEquals("\"$it\"", ProspectoValidator.MSG_EMAIL, ProspectoValidator.validarEmail(it))
        }
    }

    @Test
    fun telefonoValido() {
        assertNull(ProspectoValidator.validarTelefono("12345678"))
    }

    @Test
    fun telefonoInvalido() {
        listOf("", "1234567", "123456789", "1234567a", "+5691234").forEach {
            assertEquals("\"$it\"", ProspectoValidator.MSG_TELEFONO, ProspectoValidator.validarTelefono(it))
        }
    }

    @Test
    fun seleccionObligatoria() {
        assertEquals(ProspectoValidator.MSG_SELECCION, ProspectoValidator.validarSeleccion(null))
        assertNull(ProspectoValidator.validarSeleccion(Modalidad.ONLINE))
        assertNull(ProspectoValidator.validarSeleccion(3))
    }

    @Test
    fun observaciones() {
        assertNull(ProspectoValidator.validarObservaciones(""))
        assertNull(ProspectoValidator.validarObservaciones("a".repeat(500)))
        assertEquals(
            ProspectoValidator.MSG_OBSERVACIONES,
            ProspectoValidator.validarObservaciones("a".repeat(501)),
        )
    }
}
