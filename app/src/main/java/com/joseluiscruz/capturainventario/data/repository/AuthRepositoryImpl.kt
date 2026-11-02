package com.joseluiscruz.capturainventario.data.repository

import android.content.Context
import com.joseluiscruz.capturainventario.BuildConfig
import com.joseluiscruz.capturainventario.domain.model.User
import com.joseluiscruz.capturainventario.domain.repository.AuthRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AuthRepository {
    private val auth: FirebaseAuth?
        get() = if (FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseAuth.getInstance()
        } else {
            null
        }

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // Conserva sesión local DEBUG (demo/demo); no hay Firebase user que sincronizar.
            if (_currentUser.value?.id != "debug-demo") {
                _currentUser.value = null
            }
            return@AuthStateListener
        }

        _currentUser.value = User(
            id = firebaseUser.uid,
            username = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Usuario",
            email = firebaseUser.email ?: "",
            role = ROLE_UNASSIGNED
        )
        refreshUserRole(firebaseUser)
    }

    init {
        auth?.addAuthStateListener(authStateListener)
    }

    override suspend fun login(username: String, password: String): Result<User> {
        // Solo DEBUG: permite probar UI/ops sin google-services.json ni Firebase Auth.
        if (BuildConfig.DEBUG &&
            username.trim().equals(DEBUG_DEMO_USER, ignoreCase = true) &&
            password == DEBUG_DEMO_PASSWORD
        ) {
            val demoUser = User(
                id = "debug-demo",
                username = DEBUG_DEMO_USER,
                email = "demo@localhost",
                role = "ADMIN"
            )
            _currentUser.value = demoUser
            return Result.success(demoUser)
        }

        val firebaseAuth = auth ?: return Result.failure(
            IllegalStateException("Firebase no está configurado en este build")
        )

        return try {
            // Asumimos que "username" aquí es el correo electrónico en Firebase
            val email = if (username.contains("@")) username else "$username@example.com"
            
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Obtener rol desde Firebase Custom Claims (set via Admin SDK / Cloud Functions)
                val rol = obtenerRolDesdeCustomClaims(firebaseUser)
                
                val user = User(
                    id = firebaseUser.uid,
                    username = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: username,
                    email = firebaseUser.email ?: email,
                    role = rol
                )
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Error al obtener datos del usuario"))
            }
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Error de autenticación"
            Result.failure(Exception(errorMsg))
        }
    }

    override suspend fun logout() {
        auth?.signOut()
        _currentUser.value = null
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return _currentUser.value != null || auth?.currentUser != null
    }

    /**
     * Obtiene el rol del usuario desde Firebase Custom Claims.
     *
     * Los Custom Claims se configuran desde el backend (Admin SDK o Cloud Functions):
     * ```
     * admin.auth().setCustomUserClaims(uid, { role: 'ADMIN' })
     * ```
     *
     * Si no existe el claim "role", se devuelve "SIN_ROL". Firestore exige que
     * el claim venga del servidor; el cliente no debe promover usuarios localmente.
     *
     * @param firebaseUser El usuario de Firebase autenticado
     * @return El rol del usuario ("ADMIN" o "OPERADOR")
     */
    private suspend fun obtenerRolDesdeCustomClaims(
        firebaseUser: com.google.firebase.auth.FirebaseUser
    ): String {
        return try {
            // Forzar refresh del token para obtener claims actualizados
            val tokenResult = firebaseUser.getIdToken(true).await()
            val claims = tokenResult.claims
            // Leer el claim "role" configurado desde el Admin SDK
            (claims["role"] as? String)?.uppercase() ?: ROLE_UNASSIGNED
        } catch (e: Exception) {
            android.util.Log.w(
                "AuthRepository",
                "No se pudieron obtener Custom Claims, asignando SIN_ROL: ${e.message}"
            )
            ROLE_UNASSIGNED
        }
    }

    /**
     * Refresca el rol del usuario actual sin bloquear el init.
     */
    private fun refreshUserRole(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        firebaseUser.getIdToken(false).addOnSuccessListener { tokenResult ->
            val rol = (tokenResult.claims["role"] as? String)?.uppercase() ?: ROLE_UNASSIGNED
            _currentUser.value = _currentUser.value?.copy(role = rol)
        }
    }

    private companion object {
        const val ROLE_UNASSIGNED = "SIN_ROL"
        const val DEBUG_DEMO_USER = "demo"
        const val DEBUG_DEMO_PASSWORD = "demo"
    }
}
