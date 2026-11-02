package com.joseluiscruz.capturainventario.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the scan_history table.
 */
@Dao
interface ScanHistoryDao {
	@Query("SELECT * FROM scan_history ORDER BY scanDate DESC")
	fun getAllScans(): Flow<List<ScanHistory>>

	@Query("SELECT * FROM scan_history ORDER BY scanDate DESC")
	suspend fun getAllScansSnapshot(): List<ScanHistory>

	@Query("SELECT * FROM scan_history WHERE syncStatus = 0")
	fun getUnsyncedScans(): List<ScanHistory>
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertScan(scan: ScanHistory): Long
	
	@Update
	suspend fun updateScan(scan: ScanHistory)
	
	@Query("DELETE FROM scan_history")
	suspend fun deleteAllScans()
	
	@Query("SELECT * FROM scan_history WHERE sku = :sku")
	fun getScansBySku(sku: String): Flow<List<ScanHistory>>
}
