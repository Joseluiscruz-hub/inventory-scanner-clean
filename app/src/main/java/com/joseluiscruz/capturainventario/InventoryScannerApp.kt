package com.joseluiscruz.capturainventario

import android.app.Application
import com.joseluiscruz.capturainventario.data.sync.OfflineInventorySyncScheduler
import com.joseluiscruz.capturainventario.domain.repository.AuthRepository
import com.joseluiscruz.capturainventario.manager.AuthManager
import com.joseluiscruz.capturainventario.security.AppCheckInstaller
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class InventoryScannerApp : Application() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var offlineInventorySyncScheduler: OfflineInventorySyncScheduler

    override fun onCreate() {
        super.onCreate()
        // Mantener el estado de sesión disponible aunque Android restaure una Activity
        // directamente después de que Android finalice el proceso de la aplicación.
        AuthManager.init(authRepository)

        // Un checkout de CI puede no traer configuración Firebase. En instalaciones
        // configuradas, App Check se activa antes de cualquier acceso a Firestore.
        if (FirebaseApp.getApps(this).isNotEmpty()) {
            AppCheckInstaller.install()
            offlineInventorySyncScheduler.kick("app-start")
        }
    }
}
