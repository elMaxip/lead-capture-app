package cl.therionlabs.captura.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.therionlabs.captura.data.session.Sesion
import cl.therionlabs.captura.domain.model.Rol
import cl.therionlabs.captura.ui.formulario.FormularioScreen
import cl.therionlabs.captura.ui.lista.ListaScreen
import cl.therionlabs.captura.ui.login.LoginScreen

private const val DURACION_MS = 350

private fun destinoPara(rol: Rol?): Any = when (rol) {
    null -> Login
    Rol.CAPTADOR -> Formulario
    Rol.ADMINISTRATIVO -> Lista
}

@Composable
fun AppNavHost(sesion: Sesion?) {
    val navController = rememberNavController()
    val destinoInicial = remember { destinoPara(sesion?.rol) }

    LaunchedEffect(sesion?.rol) {
        val destino = destinoPara(sesion?.rol)
        if (navController.currentDestination?.hasRoute(destino::class) != true) {
            navController.navigate(destino) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = destinoInicial,
        enterTransition = {
            slideIntoContainer(SlideDirection.Start, tween(DURACION_MS)) + fadeIn(tween(DURACION_MS))
        },
        exitTransition = {
            slideOutOfContainer(SlideDirection.Start, tween(DURACION_MS)) + fadeOut(tween(DURACION_MS))
        },
        popEnterTransition = {
            slideIntoContainer(SlideDirection.End, tween(DURACION_MS)) + fadeIn(tween(DURACION_MS))
        },
        popExitTransition = {
            slideOutOfContainer(SlideDirection.End, tween(DURACION_MS)) + fadeOut(tween(DURACION_MS))
        },
    ) {
        composable<Login> {
            LoginScreen()
        }
        composable<Lista> {
            ListaScreen()
        }
        composable<Formulario> {
            FormularioScreen()
        }
    }
}
