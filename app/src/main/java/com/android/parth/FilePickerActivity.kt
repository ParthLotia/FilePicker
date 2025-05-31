package com.android.parth

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.parth.ui.theme.PhotoPicker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.plandroid.photopicker.FileUriUtils
import com.plandroid.photopicker.FileUtil
import com.plandroid.photopicker.TakePictureWithUriReturnContract
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
class FilePickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhotoPicker {
                val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
                val cameraImageUri = remember {
                    mutableStateOf<Uri?>(null)
                }
                val pickCamera =
                    rememberLauncherForActivityResult(TakePictureWithUriReturnContract()) { (isSuccess, imageUri) ->
                        if (isSuccess) {
                            cameraImageUri.value = imageUri

                            val file = FileUriUtils.getRealPath(this, imageUri)?.let { File(it) }
                            Log.e("file", "" + file?.exists())
                            Log.e("fileLength", "" + file?.length())
                            Log.e("fileName", "" + file?.name)
                            Log.e("filePath", "" + file?.path)
                            Log.e("file.extension", "" + file?.extension)
                        }
                    }
                val requestPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        lifecycleScope.launchWhenStarted {
                            getTmpFileUri().let { uri ->
                                pickCamera.launch(uri)
                            }
                        }
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }

                // A surface container using the 'background' color from the theme
                val selectedSingleImageUri = remember {
                    mutableStateOf<Uri?>(null)
                }
                val selectedSingleImageUriThumbnail = remember {
                    mutableStateOf<Uri?>(null)
                }



                var file by remember {
                    mutableStateOf<File?>(null)
                }
                val singleFilePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument(),
                    onResult = { uri ->

                        if (uri != null) {
                            file = FileUriUtils.getRealPath(this, uri)?.let { File(it) }
                            if (FileUriUtils.checkExtensionFile(file)) {
                                selectedSingleImageUri.value = uri
                                selectedSingleImageUriThumbnail.value = null
                            } else {
                                selectedSingleImageUri.value = null
                                Log.e("uri", "123123" + uri)
                                selectedSingleImageUriThumbnail.value = uri

                            }
                        }
                    }
                )


                Column(
                    modifier = Modifier
                        .fillMaxHeight()

                ) {

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {
                            singleFilePickerLauncher.launch(arrayOf("*/*"))
                        }) {
                            Text(text = "Choose File")
                        }

                        Button(onClick = {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }) {
                            Text(text = "Camera")
                        }

                    }


                    selectedSingleImageUri.value.let {
                        if (it != null) {
                            AsyncImage(model = it, contentDescription = "")
                        }
                    }
                    selectedSingleImageUriThumbnail.value.let {
                        if (it != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(applicationContext)
                                    .data(FileUtil.getThumbnail(file, it, applicationContext))
                                    .build(), contentDescription = null
                            )
                        }

                    }
                    cameraImageUri.value.let {
                        AsyncImage(model = it, contentDescription = "")
                    }

                }


            }

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
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PhotoPicker {
        Greeting("Android")
    }
}