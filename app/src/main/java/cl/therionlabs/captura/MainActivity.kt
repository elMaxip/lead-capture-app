package cl.therionlabs.captura

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.therionlabs.captura.data.session.SessionManager
import cl.therionlabs.captura.data.sync.SyncScheduler
import cl.therionlabs.captura.ui.navigation.AppNavHost
import cl.therionlabs.captura.ui.theme.CapturaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var syncScheduler: SyncScheduler

    @Inject lateinit var sessionManager: SessionManager

    private val pedirRedLocal =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) syncScheduler.programar()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        solicitarAccesoRedLocal()
        setContent {
            CapturaTheme {
                val sesion by sessionManager.sesion.collectAsStateWithLifecycle()
                AppNavHost(sesion)
            }
        }
    }

    private fun solicitarAccesoRedLocal() {
        if (Build.VERSION.SDK_INT < API_RED_LOCAL) return
        val permiso = PERMISO_RED_LOCAL
        if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
            pedirRedLocal.launch(permiso)
        }
    }

    private companion object {
        const val API_RED_LOCAL = 37
        const val PERMISO_RED_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK"
    }
}
