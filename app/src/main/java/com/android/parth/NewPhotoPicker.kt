package com.android.parth

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
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

            val mimeType = "*/*"

            /*Single Document Picker*/
            // Image , Video , PDF , DOC , DOCX
            pickMedia.launch(
                arrayOf(mimeType)
            )

            /* Multiple Document Picker*/
            //pickMultipleDocument.launch(arrayOf(mimeType))

            /*Multiple Image And Video*/
            //pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))

            /*Multiple Image*/
            //pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

            /*Multiple Video*/
            //pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))


        }


    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            Log.e("PhotoPicker", "Selected URI: $uri")

            val file = FileUriUtils.getRealPath(this, uri)?.let { File(it) }
            Log.e("file", "" + file?.exists())
            Log.e("fileLength", "" + file?.length())
            Log.e("fileName", "" + file?.name)
            Log.e("filePath", "" + file?.path)
            Log.e("file.extension", "" + file?.extension)

            if (file != null) {

                if ((file.extension.equals("pdf", true)) ||
                    (file.extension.equals("doc", true)) ||
                    (file.extension.equals("docx", true)) ||
                    (file.extension.equals("mp4", true)) ||
                    (file.extension.equals("mp3", true)) ||
                    (file.extension.equals("eac3", true)) ||
                    (file.extension.equals("wav", true)) ||
                    (file.extension.equals("mov", true)) ||
                    (file.extension.equals("avi", true)) ||
                    (file.extension.equals("mkv", true)) ||
                    (file.extension.equals("webm", true))
                ) {

                    img_pick.setImageBitmap(
                        FileUtil.getThumbnail(
                            file,
                            uri,
                            context = applicationContext
                        )
                    )

                } else if ((file.extension.equals("jpg", true)) ||
                    (file.extension.equals("jpeg", true)) ||
                    (file.extension.equals("png", true))
                ) {

                    img_pick.setImageURI(uri)

                }
            } else {
                img_pick.setImageResource(R.drawable.img_not_supported)
                Toast.makeText(this, "This file format is not supported", Toast.LENGTH_SHORT).show()
            }


        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }


    // This Multiple Selection working from Android 11 to 13
    // Below Android 11 we need to use old way clip data to achieve it

    val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (uris.isNotEmpty()) {
                if (uris.size > 5) {
                    Toast.makeText(this, "Please Select Item Upto 5", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("PhotoPicker", "Number of items selected: ${uris.size}")
                    Log.d("PhotoPicker", "Number of items selected: ${uris}")
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    private val pickMultipleDocument =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {uris ->
            if (uris.isNotEmpty()){

                for (i in uris.indices){
                    val file = FileUriUtils.getRealPath(this, uris[i])?.let { File(it) }
                    Log.e("file", "" + file?.exists())
                    Log.e("fileLength", "" + file?.length())
                    Log.e("fileName", "" + file?.name)
                    Log.e("filePath", "" + file?.path)
                    Log.e("file.extension", "" + file?.extension)
                }

            }

        }
}