package com.android.parth

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.util.Log
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

    fun getPdfFile(context: Context, dir: File? = null, extension: String? = null): File? {
        try {

            val ext = extension ?: ".pdf"
            val imageFileName = "PDF_${getTimestamp()}$ext"

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

    fun getDocumentFile(context: Context, dir: File? = null, extension: String? = null): File? {
        try {

            val ext = extension ?: ".docx"
            val imageFileName = "DOC_${getTimestamp()}$ext"

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

    fun getAudioFile(context: Context, dir: File? = null, extension: String? = null): File? {
        try {

            val ext = extension ?: ".mp3"
            val imageFileName = "MP3_${getTimestamp()}$ext"

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

    fun getVideoFile(context: Context, dir: File? = null, extension: String? = null): File? {
        try {

            val ext = extension ?: ".mp4"
            val imageFileName = "MP4_${getTimestamp()}$ext"

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

    fun getThumbnail(file: File?,uri: Uri,context: Context): Bitmap? {

        with(context) {

            Log.e("file?.extension",""+file?.extension)
            Log.e("file.path",""+file?.path)
            when (file?.extension) {

                "pdf" -> return getThumbnailPDF(uri, context)
                "doc" -> return BitmapFactory.decodeResource(resources,R.drawable.doc)
                "docx" -> return BitmapFactory.decodeResource(resources,R.drawable.doc)
                "mp3" -> return BitmapFactory.decodeResource(resources,R.drawable.audio)
                "eac3" -> return BitmapFactory.decodeResource(resources,R.drawable.audio)
                "wav" -> return BitmapFactory.decodeResource(resources,R.drawable.audio)
                "mp4" -> return BitmapFactory.decodeResource(resources,R.drawable.video)
                "mov" -> return BitmapFactory.decodeResource(resources,R.drawable.video)
                "avi" -> return BitmapFactory.decodeResource(resources,R.drawable.video)
                "mkv" -> return BitmapFactory.decodeResource(resources,R.drawable.video)
                "webm" -> return BitmapFactory.decodeResource(resources,R.drawable.video)


                else -> {
                    return null
                }
            }


        }


    }

    private fun getThumbnailPDF(uri: Uri, context: Context): Bitmap {
        with(context) {
            var bitmap: Bitmap? = null
            contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
                val pdfRenderer = PdfRenderer(parcelFileDescriptor).openPage(0)
                bitmap = Bitmap.createBitmap(
                    pdfRenderer.width,
                    pdfRenderer.height,
                    Bitmap.Config.ARGB_8888
                )
                pdfRenderer.render(bitmap!!, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                pdfRenderer.close()

            }
            return bitmap!!
        }
    }

    fun getThumbnailImage(absolutePath: String, context: Context): Bitmap{
        return BitmapFactory.decodeFile(absolutePath)
    }

}
