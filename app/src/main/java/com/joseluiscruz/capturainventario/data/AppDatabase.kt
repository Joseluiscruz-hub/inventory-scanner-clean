package com.joseluiscruz.capturainventario.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.joseluiscruz.capturainventario.model.RiesgoTPM

/**
 * The Room database for this app.
 */
@Database(
    entities = [
        ScanHistory::class,
        com.joseluiscruz.capturainventario.model.Pallet::class,
        com.joseluiscruz.capturainventario.model.Rumba::class,
        com.joseluiscruz.capturainventario.model.Restos::class,
        TraspasoEntity::class,
        InventarioEscaneadoEntity::class,
        TpmEntity::class,
        CatalogoPlantaEntity::class
    ],
    version = 10,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun palletDao(): PalletDao
    abstract fun rumbaDao(): RumbaDao
    abstract fun restosDao(): RestosDao
    abstract fun traspasoDao(): TraspasoDao
    abstract fun inventarioDao(): InventarioDao
    abstract fun tpmDao(): TpmDao
    abstract fun catalogoPlantaDao(): CatalogoPlantaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `catalogos_planta` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `planta` TEXT NOT NULL,
                        `sku` TEXT NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `tamanoMl` INTEGER NOT NULL,
                        `empaque` TEXT NOT NULL,
                        `marca` TEXT NOT NULL,
                        `botellasXCaja` INTEGER NOT NULL,
                        `segmento` TEXT NOT NULL,
                        `tpmDias` INTEGER NOT NULL,
                        `cajasEnMerc` INTEGER NOT NULL,
                        `cajasXTarima` INTEGER NOT NULL,
                        `camasXTarima` INTEGER NOT NULL,
                        `cajasXCama` INTEGER NOT NULL,
                        `nivelEstiba` REAL NOT NULL,
                        `timestampActualizacion` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_catalogos_planta_planta_sku` ON `catalogos_planta` (`planta`, `sku`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_catalogos_planta_sku` ON `catalogos_planta` (`sku`)")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No structural changes were made between v1 and v2.
                // This migration is intentionally empty (schema was compatible).
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No structural changes were made between v2 and v3.
                // This migration is intentionally empty (schema was compatible).
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
					CREATE TABLE IF NOT EXISTS `traspasos` (
						`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
						`firestoreId` TEXT NOT NULL,
						`sku` TEXT NOT NULL,
						`descripcion` TEXT NOT NULL,
						`almacenOrigen` TEXT NOT NULL,
						`almacenDestino` TEXT NOT NULL,
						`pallets` INTEGER NOT NULL,
						`restos` INTEGER NOT NULL,
						`tipoTarima` TEXT NOT NULL,
						`operador` TEXT NOT NULL,
						`fechaHora` TEXT NOT NULL,
						`estado` TEXT NOT NULL,
						`sincronizado` INTEGER NOT NULL
					)
				""".trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `inventario_escaneado` (
                        `codigo` TEXT PRIMARY KEY NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `cantidadTotal` INTEGER NOT NULL,
                        `ultimoScanTimestamp` INTEGER NOT NULL
                    )
                """.trimIndent()
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Crear nueva tabla con la estructura de Eventos (Append-Only)
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `inventario_escaneado_new` (
                        `operationId` TEXT PRIMARY KEY NOT NULL,
                        `sessionId` TEXT NOT NULL,
                        `codigo` TEXT NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `cantidadTotal` INTEGER NOT NULL,
                        `syncStatus` TEXT NOT NULL,
                        `ultimoScanTimestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // 2. Migrar los datos existentes sin perderlos 
                // (Los convertimos en eventos "LEGACY" ya sincronizados)
                database.execSQL(
                    """
                    INSERT INTO `inventario_escaneado_new` 
                    (`operationId`, `sessionId`, `codigo`, `descripcion`, `cantidadTotal`, `syncStatus`, `ultimoScanTimestamp`)
                    SELECT 
                        'LEGACY_' || codigo || '_' || ultimoScanTimestamp, 
                        'SESION_POR_DEFECTO', 
                        codigo, 
                        descripcion, 
                        cantidadTotal, 
                        'SENT', 
                        ultimoScanTimestamp 
                    FROM `inventario_escaneado`
                    """.trimIndent()
                )

                // 3. Eliminar la tabla antigua
                database.execSQL("DROP TABLE `inventario_escaneado`")

                // 4. Renombrar la nueva tabla al nombre oficial
                database.execSQL("ALTER TABLE `inventario_escaneado_new` RENAME TO `inventario_escaneado`")
                
                // 5. Recrear los índices para que sea rápido buscar
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_inventario_escaneado_sessionId` ON `inventario_escaneado` (`sessionId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_inventario_escaneado_codigo` ON `inventario_escaneado` (`codigo`)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `inventario_escaneado` ADD COLUMN `fechaEtiqueta` TEXT")
                database.execSQL("ALTER TABLE `inventario_escaneado` ADD COLUMN `fechaCaducidadEpochDay` INTEGER")
                database.execSQL("ALTER TABLE `inventario_escaneado` ADD COLUMN `lote` TEXT")
                database.execSQL("ALTER TABLE `inventario_escaneado` ADD COLUMN `almacen` TEXT")
                database.execSQL("ALTER TABLE `inventario_escaneado` ADD COLUMN `ubicacion` TEXT")
                database.execSQL("ALTER TABLE `inventario_escaneado` ADD COLUMN `origenFecha` TEXT NOT NULL DEFAULT 'UNKNOWN'")
                database.execSQL("ALTER TABLE `inventario_escaneado` ADD COLUMN `tipoMovimiento` TEXT NOT NULL DEFAULT 'SCAN'")
                database.execSQL("ALTER TABLE `inventario_escaneado` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")

                // Backfill: usamos el timestamp existente para conservar temporalidad de históricos.
                database.execSQL("UPDATE `inventario_escaneado` SET `createdAt` = `ultimoScanTimestamp` WHERE `createdAt` = 0")

                database.execSQL("CREATE INDEX IF NOT EXISTS `index_inventario_escaneado_codigo_fechaCaducidadEpochDay` ON `inventario_escaneado` (`codigo`, `fechaCaducidadEpochDay`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_inventario_escaneado_codigo_almacen_ubicacion` ON `inventario_escaneado` (`codigo`, `almacen`, `ubicacion`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tpm_data` (
                        `sku` TEXT NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `fechaEtiqueta` INTEGER,
                        `fechaCalculada` INTEGER,
                        `diasRestantes` INTEGER,
                        `riesgoNivel` TEXT NOT NULL,
                        `lote` TEXT,
                        `bodega` TEXT NOT NULL,
                        PRIMARY KEY(`sku`)
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_tpm_data_bodega` ON `tpm_data` (`bodega`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_tpm_data_riesgoNivel` ON `tpm_data` (`riesgoNivel`)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE inventario_escaneado " +
                        "ADD COLUMN cantidadRestos INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE inventario_escaneado ADD COLUMN tipoTarima TEXT"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10
                    )
                    // .fallbackToDestructiveMigration() // Solo para desarrollo, comentar en producción
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Type converters for Room database.
 */
@TypeConverters
class Converters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: java.util.Date?): Long? {
        return date?.time
    }

    @androidx.room.TypeConverter
    fun fromRiesgoTPM(value: RiesgoTPM?): String? {
        return value?.name
    }

    @androidx.room.TypeConverter
    fun toRiesgoTPM(value: String?): RiesgoTPM? {
        return value?.let { RiesgoTPM.valueOf(it) }
    }
}
