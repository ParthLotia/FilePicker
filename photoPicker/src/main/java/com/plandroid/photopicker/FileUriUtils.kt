package com.plandroid.photopicker

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import java.io.*


object FileUriUtils {

    fun getRealPath(context: Context, uri: Uri): String? {
        var path = getPathFromLocalUri(context, uri)
        if (path == null) {
            path = getPathFromRemoteUri(context, uri)
        }
        return path
    }


    fun checkExtensionFile(file: File?) : Boolean{

        return (file?.extension.equals("jpg", true)) ||
                (file?.extension.equals("jpeg", true)) ||
                (file?.extension.equals("png", true))

    }

    private fun getPathFromLocalUri(context: Context, uri: Uri): String? {

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]


                    return if ("primary".equals(type, ignoreCase = true)) {
                        if (split.size > 1) {
                            Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                        } else {
                            Environment.getExternalStorageDirectory().toString() + "/"
                        }

                    } else {
                        val path = "storage" + "/" + docId.replace(":", "/")
                        if (File(path).exists()) {
                            path
                        } else {
                            "/storage/sdcard/" + split[1]
                        }
                    }
                }
                isDownloadsDocument(uri) -> {
                    var id = DocumentsContract.getDocumentId(uri)
                    if (id.contains(":")) {
                        id = id.split(":")[1]
                    }
                    if (id.isNotBlank()) {
                        return try {
                            val contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                java.lang.Long.valueOf(id)
                            )
                            getDataColumn(context, contentUri, null, null)
                        } catch (e: NumberFormatException) {
                            Log.i("ImagePicker", e.message.toString())
                            null
                        }
                    }
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])

                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            }
        }
        else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        }
        else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }

        return null
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor =
                context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } catch (ex: Exception) {
        } finally {
            cursor?.close()
        }
        return null
    }


    private fun getPathFromRemoteUri(context: Context, uri: Uri): String? {
        // The code below is why Java now has try-with-resources and the Files utility.
        var file: File? = null
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var success = false
        var extensions: String? = null
        try {

            val imagePath = uri.path
            if (imagePath != null && imagePath.lastIndexOf(".") != -1) {
                extensions = imagePath.substring(imagePath.lastIndexOf(".") + 1)
                extensions = ".$extensions"
            }
            else{
                var pathString = getRealPathFromURI(context,uri)
                extensions = pathString?.substring(pathString.lastIndexOf(".") + 1)
                extensions = ".$extensions"
                Log.e("pathString", "$pathString")
            }

            if (extensions.equals(".pdf", true)) {

                inputStream = context.contentResolver.openInputStream(uri)
                file = FileUtil.getPdfFile(context, context.cacheDir, extensions)
                if (file == null) return null
                outputStream = FileOutputStream(file)
                if (inputStream != null) {
                    inputStream.copyTo(outputStream, bufferSize = 4 * 1024)
                    success = true
                }


            }
            else if ((extensions.equals(".doc", true)) || (extensions.equals(".docx", true))) {

                inputStream = context.contentResolver.openInputStream(uri)
                file = FileUtil.getDocumentFile(context, context.cacheDir, extensions)
                if (file == null) return null
                outputStream = FileOutputStream(file)
                if (inputStream != null) {
                    inputStream.copyTo(outputStream, bufferSize = 4 * 1024)
                    success = true
                }


            }
            else if ((extensions.equals(".jpg", true)) || (extensions.equals(".jpeg", true)) || (extensions.equals(".png", true))) {
                val extension = getImageExtension(uri)
                inputStream = context.contentResolver.openInputStream(uri)
                file = FileUtil.getImageFile(context, context.cacheDir, extension)
                if (file == null) return null
                outputStream = FileOutputStream(file)
                if (inputStream != null) {
                    inputStream.copyTo(outputStream, bufferSize = 4 * 1024)
                    success = true
                }
            }
            else if (extensions.equals(".mp3",true)){

                inputStream = context.contentResolver.openInputStream(uri)
                file = FileUtil.getAudioFile(context, context.cacheDir, extensions)
                if (file == null) return null
                outputStream = FileOutputStream(file)
                if (inputStream != null) {
                    inputStream.copyTo(outputStream, bufferSize = 4 * 1024)
                    success = true
                }
            }
            else if (extensions.equals(".mp4",true)){
                inputStream = context.contentResolver.openInputStream(uri)
                file = FileUtil.getVideoFile(context, context.cacheDir, extensions)
                if (file == null) return null
                outputStream = FileOutputStream(file)
                if (inputStream != null) {
                    inputStream.copyTo(outputStream, bufferSize = 4 * 1024)
                    success = true
                }
            }


        } catch (ignored: IOException) {
        } finally {
            try {
                inputStream?.close()
            } catch (ignored: IOException) {
            }

            try {
                outputStream?.close()
            } catch (ignored: IOException) {
                // If closing the output stream fails, we cannot be sure that the
                // target file was written in full. Flushing the stream merely moves
                // the bytes into the OS, not necessarily to the file.
                success = false
            }
        }
        return if (success) file!!.path else null
    }

    private fun getImageExtension(uriImage: Uri): String {
        var extension: String? = null

        try {
            val imagePath = uriImage.path
            if (imagePath != null && imagePath.lastIndexOf(".") != -1) {
                extension = imagePath.substring(imagePath.lastIndexOf(".") + 1)
            }
        } catch (e: Exception) {
            extension = null
        }

        if (extension == null || extension.isEmpty()) {
            extension = "jpg"
        }

        return ".$extension"
    }



    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }


    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }


    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
    fun getRealPathFromURI(context: Context?, contentUri: Uri?): String? {
        Log.e("contentUri",""+contentUri)


        var splitUri = contentUri.toString().substring(contentUri.toString().lastIndexOf("/") + 1)
        Log.e("splitUri ",""+splitUri)


        val file = File(getFileName(context,contentUri)!!)
        if (file != null){
            Log.e("filePath","123:->>"+file.path)
            return file.path
        }
        return null
    }

    @Throws(IOException::class)
    private fun getBytes(inputStream: InputStream?): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream!!.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

   @SuppressLint("Range")
   open fun getFileName(context: Context?, uri: Uri?): String? {
       var result: String? = null
       if (uri!!.scheme == "content") {
           val cursor: Cursor = context!!.contentResolver.query(uri, null, null, null, null)!!
           try {
               if (cursor != null && cursor.moveToFirst()) {
                   result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
               }
           } finally {
               cursor!!.close()
           }
       }
       if (result == null) {
           result = uri.path
           val cut = result!!.lastIndexOf('/')
           if (cut != -1) {
               result = result!!.substring(cut + 1)
           }
       }
       Log.e("result",""+result)
       return result
   }


}
