package com.android.parth

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class NewPhotoPicker : AppCompatActivity() {

    lateinit var img_pick: ImageView
    lateinit var btn_upload_pick: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_photo_picker)

        img_pick = findViewById(R.id.img_pick)
        btn_upload_pick = findViewById(R.id.btn_upload_pick)


        btn_upload_pick.setOnClickListener {

            val mimeType = "image/*"

            /*Single Image Picker*/
            pickMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.SingleMimeType(
                        mimeType
                    )
                )
            )

            /*Multiple Image And Video*/
            //pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))

            /*Multiple Image*/
            //pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

            /*Multiple Video*/
            //pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))

        }


    }

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.e("PhotoPicker", "Selected URI: $uri")
            val file = File(FileUriUtils.getRealPath(this, uri))
            Log.e("file", "" + file.exists())
            Log.e("fileLength", "" + file.length())
            Log.e("fileName", "" + file.name)
            Log.e("filePath", "" + file.path)
            img_pick.setImageURI(uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }


    // This Multiple Selection working from Android 11 to 13
    // Below Android 11 we need to use old way clip data to achieve it

    val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (uris.isNotEmpty()) {
                Log.d("PhotoPicker", "Number of items selected: ${uris.size}")
                Log.d("PhotoPicker", "Number of items selected: ${uris}")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
}