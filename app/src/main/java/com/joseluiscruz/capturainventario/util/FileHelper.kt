package com.joseluiscruz.capturainventario.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileHelper {

    /**
     * Descarga de forma segura un archivo desde un URI (OneDrive, Google Drive, etc.)
     * a la carpeta de caché local de la aplicación.
     * * Esto soluciona los problemas de timeout y lecturas corruptas de archivos en la nube.
     *
     * @param context El contexto de la aplicación.
     * @param uri El URI del archivo seleccionado.
     * @return Un objeto File local que puedes leer de forma segura, o null si falló.
     */
    suspend fun getFileFromUriSeguro(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            // 1. Obtener el nombre real del archivo
            val fileName = getFileName(context, uri) ?: "temp_import.csv"
            
            // 2. Crear un archivo temporal en la caché interna de la app
            val tempFile = File(context.cacheDir, fileName)
            
            // Opcional: Si el archivo ya existe en caché, borrarlo para asegurar una versión fresca
            if (tempFile.exists()) {
                tempFile.delete()
            }

            // 3. Abrir el flujo de entrada desde el URI (la nube)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("FileHelper", "No se pudo abrir el InputStream del URI: $uri")
                return@withContext null
            }

            // 4. Copiar los datos de la nube al archivo local bloque a bloque
            val outputStream = FileOutputStream(tempFile)
            val buffer = ByteArray(4 * 1024) // Buffer de 4KB
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }

            // 5. Cerrar los flujos
            outputStream.flush()
            outputStream.close()
            inputStream.close()

            Log.d("FileHelper", "Archivo descargado exitosamente a caché: ${tempFile.absolutePath}")
            return@withContext tempFile

        } catch (e: Exception) {
            Log.e("FileHelper", "Error crítico copiando archivo del URI", e)
            return@withContext null
        }
    }

    /**
     * Extrae el nombre del archivo del ContentResolver.
     */
    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
}
