package com.joseluiscruz.capturainventario.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migration9To10_preservesInventoryAndAddsPalletFields() {
        helper.createDatabase(TEST_DATABASE, 9).apply {
            execSQL(
                """
                INSERT INTO inventario_escaneado (
                    operationId,
                    sessionId,
                    codigo,
                    descripcion,
                    cantidadTotal,
                    fechaEtiqueta,
                    fechaCaducidadEpochDay,
                    lote,
                    almacen,
                    ubicacion,
                    origenFecha,
                    tipoMovimiento,
                    syncStatus,
                    createdAt,
                    ultimoScanTimestamp
                ) VALUES (
                    'operation-1',
                    'session-legacy',
                    'SKU-123',
                    'Producto existente',
                    7,
                    NULL,
                    NULL,
                    'L-01',
                    'PT',
                    'A-01',
                    'UNKNOWN',
                    'SCAN',
                    'PENDING',
                    1000,
                    1000
                )
                """.trimIndent()
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            TEST_DATABASE,
            10,
            true,
            AppDatabase.MIGRATION_9_10
        )

        migrated.query(
            """
            SELECT codigo, cantidadTotal, cantidadRestos, tipoTarima
            FROM inventario_escaneado
            WHERE operationId = 'operation-1'
            """.trimIndent()
        ).use { cursor ->
            assertEquals(1, cursor.count)
            cursor.moveToFirst()
            assertEquals("SKU-123", cursor.getString(0))
            assertEquals(7, cursor.getInt(1))
            assertEquals(0, cursor.getInt(2))
            assertNull(cursor.getString(3))
        }

        migrated.close()
    }

    private companion object {
        const val TEST_DATABASE = "migration-9-10-test"
    }
}
