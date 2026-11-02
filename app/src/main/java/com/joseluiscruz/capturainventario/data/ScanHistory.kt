package com.joseluiscruz.capturainventario.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Entity representing a scan history record in the database.
 */
@Entity(tableName = "scan_history")
data class ScanHistory(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val sku: String,
	val description: String,
	val quantity: Int,
	val location: String,
	val scanDate: Date = Date(),
	val syncStatus: Boolean = false
)
