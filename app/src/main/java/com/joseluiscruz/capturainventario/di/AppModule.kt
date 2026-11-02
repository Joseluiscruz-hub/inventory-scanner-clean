package com.joseluiscruz.capturainventario.di

import android.content.Context
import com.joseluiscruz.capturainventario.data.AppDatabase
import com.joseluiscruz.capturainventario.data.InventarioDao
import com.joseluiscruz.capturainventario.data.CatalogoPlantaDao
import com.joseluiscruz.capturainventario.data.TpmDao
import com.joseluiscruz.capturainventario.data.FirebaseRepository
import com.joseluiscruz.capturainventario.data.repository.CatalogoPlantaRepository
import com.joseluiscruz.capturainventario.data.repository.AuthRepositoryImpl
import com.joseluiscruz.capturainventario.domain.repository.AuthRepository
import com.joseluiscruz.capturainventario.data.repository.CsvInventarioService
import com.joseluiscruz.capturainventario.data.repository.InventorySessionStore
import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import com.joseluiscruz.capturainventario.data.repository.SharedPreferencesInventorySessionStore
import com.joseluiscruz.capturainventario.ui.comparacion.InventarioTiempoRealManager
import com.joseluiscruz.capturainventario.ui.comparacion.TPMMonitorManager
import dagger.Module
import java.io.File
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import com.joseluiscruz.capturainventario.util.ConnectivityNetworkStatusMonitor
import com.joseluiscruz.capturainventario.util.NetworkStatusMonitor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideInventarioDao(database: AppDatabase): InventarioDao = database.inventarioDao()

    @Provides
    fun provideCatalogoPlantaDao(database: AppDatabase): CatalogoPlantaDao = database.catalogoPlantaDao()

    @Provides
    fun provideTpmDao(database: AppDatabase): TpmDao = database.tpmDao()

    @Provides
    @Singleton
    fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository = impl

    @Provides
    @Singleton
    fun provideNetworkStatusMonitor(
        @ApplicationContext context: Context
    ): NetworkStatusMonitor = ConnectivityNetworkStatusMonitor(context)

    @Provides
    @Singleton
    fun provideInventorySessionStore(
        @ApplicationContext context: Context,
        inventarioDao: InventarioDao
    ): InventorySessionStore = SharedPreferencesInventorySessionStore(
        context,
        inventarioDao,
        Dispatchers.IO
    )

    @Provides
    @Singleton
    fun provideInventarioRepository(
        inventarioDao: InventarioDao,
        sessionStore: InventorySessionStore
    ): InventarioRepository {
        return InventarioRepository(inventarioDao, sessionStore)
    }

    @Provides
    @Singleton
    fun provideCatalogoPlantaRepository(catalogoPlantaDao: CatalogoPlantaDao): CatalogoPlantaRepository {
        return CatalogoPlantaRepository(catalogoPlantaDao)
    }

    @Provides
    @Singleton
    fun provideFirebaseRepository(@ApplicationContext context: Context): FirebaseRepository {
        return FirebaseRepository(context)
    }

    @Provides
    @Singleton
    fun provideCsvInventarioService(@ApplicationContext context: Context): CsvInventarioService {
        return CsvInventarioService(context)
    }

    @Provides
    fun provideFilesDir(@ApplicationContext context: Context): File {
        return context.filesDir
    }

    @Provides
    @Singleton
    fun provideInventarioTiempoRealManager(repository: InventarioRepository): InventarioTiempoRealManager {
        return InventarioTiempoRealManager(repository)
    }

    @Provides
    @Singleton
    fun provideTPMMonitorManager(tpmDao: TpmDao, manager: InventarioTiempoRealManager): TPMMonitorManager {
        return TPMMonitorManager(tpmDao, manager)
    }
}
