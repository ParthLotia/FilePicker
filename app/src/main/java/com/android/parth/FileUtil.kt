package com.android.parth

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


object FileUtil {


    fun getImageFile(context: Context, dir: File? = null, extension: String? = null): File? {
        try {

            val ext = extension ?: ".jpg"
            val imageFileName = "IMG_${getTimestamp()}$ext"


            val storageDir = dir ?: getCameraDirectory(context)


            if (!storageDir.exists()) storageDir.mkdirs()


            val file = File(storageDir, imageFileName)


            file.createNewFile()

            return file
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }

    fun getImageUri(context: Context, dir: File? = null, extension: String? = null): Uri? {
        try {

            val ext = extension ?: ".jpg"
            val imageFileName = "IMG_${getTimestamp()}$ext"


            val storageDir = dir ?: getCameraDirectory(context)


            if (!storageDir.exists()) storageDir.mkdirs()


            val file = File(storageDir, imageFileName)


            file.createNewFile()

            val authority =
                context.packageName + context.getString(R.string.image_picker_provider_authority_suffix)
            val uriForFile = FileProvider.getUriForFile(
                context,
                authority,
                file
            )
            return uriForFile
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }


    private fun getCameraDirectory(context: Context): File {
        val dir =
            context.getExternalFilesDir(Environment.DIRECTORY_DCIM) // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        return File(dir, "Camera")
    }


    private fun getTimestamp(): String {
        val timeFormat = "yyyyMMdd_HHmmssSSS"
        return SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date())
    }


    fun getFreeSpace(file: File): Long {
        val stat = StatFs(file.path)
        val availBlocks = stat.availableBlocksLong
        val blockSize = stat.blockSizeLong
        return availBlocks * blockSize
    }


    fun getImageResolution(context: Context, uri: Uri): Pair<Int, Int> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val stream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(stream, null, options)
        return Pair(options.outWidth, options.outHeight)
    }


    fun getImageResolution(file: File): Pair<Int, Int> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, options)
        return Pair(options.outWidth, options.outHeight)
    }


    fun getImageSize(context: Context, uri: Uri): Long {
        return getDocumentFile(context, uri)?.length() ?: 0
    }


    fun getTempFile(context: Context, uri: Uri): File? {
        try {
            val destination = File(context.cacheDir, "image_picker.png")

            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            val fileDescriptor = parcelFileDescriptor?.fileDescriptor ?: return null

            val src = FileInputStream(fileDescriptor).channel
            val dst = FileOutputStream(destination).channel
            dst.transferFrom(src, 0, src.size())
            src.close()
            dst.close()

            return destination
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return null
    }


    fun getDocumentFile(context: Context, uri: Uri): DocumentFile? {
        var file: DocumentFile? = null
        if (isFileUri(uri)) {
            val path = FileUriUtils.getRealPath(context, uri)
            if (path != null) {
                file = DocumentFile.fromFile(File(path))
            }
        } else {
            file = DocumentFile.fromSingleUri(context, uri)
        }
        return file
    }


    fun getCompressFormat(extension: String): Bitmap.CompressFormat {
        return when {
            extension.contains("png", ignoreCase = true) -> Bitmap.CompressFormat.PNG
            extension.contains("webp", ignoreCase = true) -> Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.JPEG
        }
    }


    private fun isFileUri(uri: Uri): Boolean {
        return "file".equals(uri.scheme, ignoreCase = true)
    }
}
