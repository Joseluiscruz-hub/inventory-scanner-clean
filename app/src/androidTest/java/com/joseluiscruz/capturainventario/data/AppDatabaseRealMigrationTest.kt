package com.joseluiscruz.capturainventario.data

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba opt-in con una copia representativa de la base v9.
 *
 * El archivo nunca se versiona. Consulta docs/VALIDACION_MIGRACION_ROOM_9_10.md
 * para preparar el asset local de forma segura.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseRealMigrationTest {

    @Test
    fun representativeVersion9Backup_migratesWithoutDataLoss() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val input = runCatching {
            instrumentation.context.assets.open(REAL_DATABASE_ASSET)
        }.getOrElse {
            assumeTrue(
                "Prueba omitida: agrega localmente $REAL_DATABASE_ASSET",
                false
            )
            return
        }

        context.deleteDatabase(TEST_DATABASE)
        val databaseFile = context.getDatabasePath(TEST_DATABASE)
        databaseFile.parentFile?.mkdirs()
        input.use { source ->
            databaseFile.outputStream().use(source::copyTo)
        }

        val rowsBefore = SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        ).use { legacyDatabase ->
            assertEquals(9, legacyDatabase.version)
            legacyDatabase.rawQuery(
                "SELECT COUNT(*) FROM inventario_escaneado",
                null
            ).use { cursor ->
                cursor.moveToFirst()
                cursor.getInt(0)
            }
        }

        val migrated = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            TEST_DATABASE
        )
            .addMigrations(AppDatabase.MIGRATION_9_10)
            .allowMainThreadQueries()
            .build()

        val sqlite = migrated.openHelper.writableDatabase
        assertEquals(10, sqlite.version)
        sqlite.query("SELECT COUNT(*) FROM inventario_escaneado").use { cursor ->
            cursor.moveToFirst()
            assertEquals(rowsBefore, cursor.getInt(0))
        }
        sqlite.query("PRAGMA table_info(inventario_escaneado)").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            val columns = buildSet {
                while (cursor.moveToNext()) add(cursor.getString(nameIndex))
            }
            assertTrue("cantidadRestos" in columns)
            assertTrue("tipoTarima" in columns)
        }

        migrated.close()
        context.deleteDatabase(TEST_DATABASE)
    }

    private companion object {
        const val REAL_DATABASE_ASSET = "real-db/inventory_database_v9.db"
        const val TEST_DATABASE = "representative-migration-9-10.db"
    }
}
