package com.android.parth

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
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {


            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
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

    private fun getFilePath(context: Context, uri: Uri): String? {

        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
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
            }else{
                var pathString = getRealPathFromURI(context,uri)
                extensions = pathString?.substring(pathString.lastIndexOf(".") + 1)
                extensions = ".$extensions"
                Log.e("pathString", "$pathString")

            }


            Log.e("extensions", "$extensions")


            if (extensions.equals(".pdf", true)) {

                inputStream = context.contentResolver.openInputStream(uri)
                file = FileUtil.getPdfFile(context, context.cacheDir, extensions)
                if (file == null) return null
                outputStream = FileOutputStream(file)
                if (inputStream != null) {
                    inputStream.copyTo(outputStream, bufferSize = 4 * 1024)
                    success = true
                }


            } else if ((extensions.equals(".doc", true)) || (extensions.equals(".docx", true))) {

                inputStream = context.contentResolver.openInputStream(uri)
                file = FileUtil.getDocumentFile(context, context.cacheDir, extensions)
                if (file == null) return null
                outputStream = FileOutputStream(file)
                if (inputStream != null) {
                    inputStream.copyTo(outputStream, bufferSize = 4 * 1024)
                    success = true
                }


            } else if ((extensions.equals(".jpg", true)) || (extensions.equals(
                    ".jpeg",
                    true
                )) || (extensions.equals(".png", true))
            ) {
                val extension = getImageExtension(uri)
                inputStream = context.contentResolver.openInputStream(uri)
                file = FileUtil.getImageFile(context, context.cacheDir, extension)
                if (file == null) return null
                outputStream = FileOutputStream(file)
                if (inputStream != null) {
                    inputStream.copyTo(outputStream, bufferSize = 4 * 1024)
                    success = true
                }
            }else if (extensions.equals(".mp3")){

                inputStream = context.contentResolver.openInputStream(uri)
                file = FileUtil.getAudioFile(context, context.cacheDir, extensions)
                if (file == null) return null
                outputStream = FileOutputStream(file)
                if (inputStream != null) {
                    inputStream.copyTo(outputStream, bufferSize = 4 * 1024)
                    success = true
                }
            }else if (extensions.equals(".mp4")){
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

    fun getImageExtension(uriImage: Uri): String {
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


    fun getImageExtension(file: File): String {
        return getImageExtension(Uri.fromFile(file))
    }

    fun getImageExtensionFormat(uri: Uri): Bitmap.CompressFormat {
        val extension = getImageExtension(uri)
        return if (extension == ".png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
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




//        val out: OutputStream
        val file = File(getFileName(context,contentUri)!!)
        if (file != null){
            Log.e("filePath","123:->>"+file.path)
            return file.path
        }
       /* try {
            if (file.createNewFile()) {
                val iStream = if (context != null) context.contentResolver.openInputStream(
                    contentUri!!
                ) else context?.contentResolver?.openInputStream(contentUri!!)
                val inputData = getBytes(iStream)
                out = FileOutputStream(file)
                out.write(inputData)
                out.close()
                return file.absolutePath
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }*/
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

   /* private fun getFilename(context: Context?, splitUri: String): String? {
        val mediaStorageDir = File(context!!.getExternalFilesDir(""), "NewPhotoPickerAndroid13")
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
        }
        Log.e("mediaStorageDir",""+mediaStorageDir.path)
        var mImageName : String = ""
        if (splitUri.contains("image")){

            mImageName = "IMG_" + System.currentTimeMillis().toString() + ".png"
        }else if (splitUri.contains("video")){
            mImageName = "VIDEO_" + System.currentTimeMillis().toString() + ".mp4"
        }else if (splitUri.contains("audio")){
            mImageName = "AUD_" + System.currentTimeMillis().toString() + ".mp3"
        }else if (splitUri.contains("document")){
            mImageName = "AUD_" + System.currentTimeMillis().toString() + ".mp3"
        }
        return mediaStorageDir.absolutePath + "/" + mImageName
    }*/
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
