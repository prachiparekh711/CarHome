package ro.westaco.carhome.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.DatabaseUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Integer.min
import java.nio.charset.StandardCharsets


class FileUtil {

    companion object {
        private val DEBUG = true

        @SuppressLint("NewApi")
        fun getPath(uri: Uri, context: Context): String? {
            var uri: Uri = uri
            val needToCheckUri = Build.VERSION.SDK_INT >= 19
            var selection: String? = null
            var selectionArgs: Array<String>? = null
            // Uri is different in versions after KITKAT (Android 4.4), we need to
            // deal with different Uris.
            if (needToCheckUri && DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else if (isDownloadsDocument(uri)) {
                    val fileName: String? = getFilePath(context, uri)
                    if (fileName != null) {
                        return Environment.getExternalStorageDirectory()
                            .toString() + "/Download/" + fileName
                    }

                    var id = DocumentsContract.getDocumentId(uri)
                    if (id.startsWith("raw:")) {
                        id = id.replaceFirst("raw:".toRegex(), "")
                        val file = File(id)
                        if (file.exists()) return id
                    }

                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    when (type) {
                        "image" -> uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    selection = "_id=?"
                    selectionArgs = arrayOf(
                        split[1]
                    )
                }
            }
            if ("content".equals(uri.scheme, ignoreCase = true)) {
                val projection = arrayOf(
                    MediaStore.Images.Media.DATA
                )
                try {
                    context.contentResolver
                        .query(uri, projection, selection, selectionArgs, null)
                        .use { cursor ->
                            if (cursor != null && cursor.moveToFirst()) {
                                val columnIndex: Int =
                                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                                return cursor.getString(columnIndex)
                            }
                        }
                } catch (e: Exception) {
                }
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        private fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }


        fun getFilePath(context: Context, uri: Uri): String? {
            var cursor: Cursor? = null
            val projection = arrayOf(
                MediaStore.MediaColumns.DISPLAY_NAME
            )
            try {
                cursor = context.contentResolver.query(
                    uri, projection, null, null,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index: Int =
                        cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        private fun getDataColumn(
            context: Context, uri: Uri, selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = context.contentResolver.query(
                    uri, projection, selection, selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    if (DEBUG) DatabaseUtils.dumpCursor(cursor)
                    val column_index: Int = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
            return null
        }

        fun getSize(size: Long): String {
            val kilo: Long = 1024
            val mega = kilo * kilo
            val giga = mega * kilo
            val tera = giga * kilo
            var s = ""
            val kb = size.toDouble() / kilo
            val mb = kb / kilo
            val gb = mb / kilo
            val tb = gb / kilo
            if (size < kilo) {
                s = "$size Bytes"
            } else if (size in kilo until mega) {
                s = String.format("%.2f", kb) + " KB"
            } else if (size in mega until giga) {
                s = String.format("%.2f", mb) + " MB"
            } else if (size in giga until tera) {
                s = String.format("%.2f", gb) + " GB"
            } else if (size >= tera) {
                s = String.format("%.2f", tb) + " TB"
            }
            return s
        }

        fun getFilePathFor11(context: Context, contentUri: Uri): String? {
            try {
                val filePathColumn = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.TITLE,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                )

                val returnCursor = contentUri.let {
                    context.contentResolver.query(
                        it,
                        filePathColumn,
                        null,
                        null,
                        null
                    )
                }

                if (returnCursor != null) {

                    returnCursor.moveToFirst()
                    val nameIndex = returnCursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    val name = returnCursor.getString(nameIndex)
                    val file = File(context.cacheDir, name)
                    val inputStream = context.contentResolver.openInputStream(contentUri)
                    val outputStream = FileOutputStream(file)
                    var read: Int
                    val maxBufferSize = 1 * 1024 * 1024
                    val bytesAvailable = inputStream?.available()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val bufferSize = bytesAvailable?.let { min(it, maxBufferSize) }
                        val buffers = ByteArray(bufferSize as Int)
                        while (inputStream.read(buffers).also { read = it } != -1) {
                            outputStream.write(buffers, 0, read)
                        }
                    }
                    inputStream?.close()
                    outputStream.close()
                    return file.absolutePath
                } else {
                    Log.d("", "returnCursor is null")
                    return null
                }
            } catch (e: Exception) {
                Log.d("", "exception caught at getFilePath(): $e")
                return null
            }
        }

        fun checkPermission(context: Context): Boolean {

            val result =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            val result1 =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }

        fun requestPermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                200
            )

        }

        fun checkCameraPermission(context: Context): Boolean {

            val result =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                )

            return result == PackageManager.PERMISSION_GRANTED
        }

        fun requestCamerasPermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                201
            )

        }

        fun loadJSONFromAsset(context: Context): String? {
            var json: String? = null
            json = try {
                val `is`: InputStream = context.assets.open("Country_Code.json")
                val size = `is`.available()
                val buffer = ByteArray(size)
                `is`.read(buffer)
                `is`.close()
                String(buffer, StandardCharsets.UTF_8)
            } catch (ex: IOException) {
                ex.printStackTrace()
                return null
            }
            return json
        }

    }


}