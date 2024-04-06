package com.android.parth

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.parth.databinding.ActivityNewPhotoPickerBinding
import com.plandroid.photopicker.FileUriUtils
import com.plandroid.photopicker.FileUtil
import com.plandroid.photopicker.TakePictureWithUriReturnContract
import java.io.File


class NewPhotoPicker : AppCompatActivity() {

    var bindingNewPhotoPickerBinding: ActivityNewPhotoPickerBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_photo_picker)

        bindingNewPhotoPickerBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_new_photo_picker)

        bindingNewPhotoPickerBinding!!.btnUploadPick.setOnClickListener {
            val mimeType = "*/*"
            /*Single Document Picker*/
            // Image , Video , PDF , DOC , DOCX
            pickMedia.launch(
                arrayOf(mimeType)
            )
        }
        // For Camera uncomment below code
        bindingNewPhotoPickerBinding!!.btnUploadCamera.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            } else {
                takePicture()
            }
        }
    }


    private fun takePicture() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                pickCamera.launch(uri)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePicture()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.CAMERA
                )
            ) {
                shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)
            } else {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    private val pickCamera =
        registerForActivityResult(TakePictureWithUriReturnContract()) { (isSuccess, imageUri) ->
            if (isSuccess) {
                bindingNewPhotoPickerBinding!!.imgPick.setImageURI(imageUri)
                val file = FileUriUtils.getRealPath(this, imageUri)?.let { File(it) }
                Log.e("file", "" + file?.exists())
                Log.e("fileLength", "" + file?.length())
                Log.e("fileName", "" + file?.name)
                Log.e("filePath", "" + file?.path)
                Log.e("file.extension", "" + file?.extension)
            }
        }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            applicationContext,
            "${applicationContext.packageName}.fileProvider",
            tmpFile
        )
    }


    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {

                val file = FileUriUtils.getRealPath(this, uri)?.let { File(it) }
                if (FileUriUtils.checkExtensionFile(file)) {
                    bindingNewPhotoPickerBinding!!.imgPick.setImageURI(uri)
                } else {
                    bindingNewPhotoPickerBinding!!.imgPick.setImageBitmap(
                        FileUtil.getThumbnail(
                            file,
                            uri,
                            context = applicationContext
                        )
                    )
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
}
