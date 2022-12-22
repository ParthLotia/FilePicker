package com.android.parth

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.android.parth.ui.theme.PhotoPicker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhotoPicker {
                var selectedSingleImageUri by remember {
                    mutableStateOf<Uri?>(null)
                }
                var selectedMultipleImageUris by remember {
                    mutableStateOf<List<Uri>>(emptyList())
                }
                val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = { uri -> selectedSingleImageUri = uri }
                )
                val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickMultipleVisualMedia(),
                    onResult = { uris -> selectedMultipleImageUris = uris }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()

                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Button(onClick = {
                                singlePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }) {
                                Text(text = "Choose One Photo")
                            }
                            Button(onClick = {
                                multiplePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }) {
                                Text(text = "Choose Multiple Photo")
                            }
                        }
                    }

                    item {
                        AsyncImage(
                            model = selectedSingleImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    items(selectedMultipleImageUris) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}