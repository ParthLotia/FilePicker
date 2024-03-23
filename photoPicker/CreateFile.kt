package com.android.parth.custom

import android.content.Context
import androidx.compose.runtime.Composable
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Context.createImageFile(): File {

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
    return image

}