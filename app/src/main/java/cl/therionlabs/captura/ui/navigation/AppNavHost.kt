package cl.therionlabs.captura.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.therionlabs.captura.ui.formulario.FormularioScreen
import cl.therionlabs.captura.ui.lista.ListaScreen

private const val DURACION_MS = 350

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Lista,
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
        composable<Lista> {
            ListaScreen(onNuevoProspecto = { navController.navigate(Formulario) })
        }
        composable<Formulario> {
            FormularioScreen(onVolver = { navController.popBackStack() })
        }
    }
}
