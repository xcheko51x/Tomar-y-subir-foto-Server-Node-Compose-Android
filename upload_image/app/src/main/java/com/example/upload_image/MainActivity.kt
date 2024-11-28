package com.example.upload_image

import android.Manifest
import android.content.Context
import android.media.Image
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.upload_image.ui.theme.Upload_imageTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.concurrent.Executor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Upload_imageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CamaraView()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CamaraView() {
    val permissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    )

    val context = LocalContext.current
    val camaraController = remember { LifecycleCameraController(context) }
    val lifecycle = LocalLifecycleOwner.current

    val directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absoluteFile

    LaunchedEffect(key1 = Unit) {
        permissions.launchMultiplePermissionRequest()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val executor = ContextCompat.getMainExecutor(context)
                    tomarFoto(camaraController, executor, directorio, context)
                }
            ) {
                Icon(
                    painterResource(id = R.drawable.icon_camara),
                    tint = Color.White,
                    contentDescription = ""
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {
        if (permissions.allPermissionsGranted) {
            CamaraComposable(
                camaraController,
                lifecycle,
                Modifier.padding(it)
            )
        } else {
            Text(
                text = "Permisos denegados",
                modifier = Modifier.padding(it)
            )
        }
    }
}

@Composable
fun CamaraComposable(
    camaraController: LifecycleCameraController,
    lifecycle: LifecycleOwner,
    modifier: Modifier = Modifier
) {
    camaraController.bindToLifecycle(lifecycle)

    AndroidView(
        modifier = modifier,
        factory = {
            val previaView = PreviewView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            previaView.controller = camaraController
            previaView
        }
    )
}

private fun tomarFoto(
    camaraController: LifecycleCameraController,
    executor: Executor,
    directorio: File,
    context: Context
) {
    val image = File.createTempFile("im_", ".jpg", directorio)

    val outputDirectory = ImageCapture.OutputFileOptions.Builder(image).build()

    camaraController.takePicture(
        outputDirectory,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                println(outputFileResults.savedUri)
                Log.d("IMG", "FOTO TOMADA: $image")

                uploadImage(image)
            }

            override fun onError(exception: ImageCaptureException) {
                println()
            }
        }
    )
}

fun uploadImage(
    image: File
) {
    val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), image)

    val body = MultipartBody.Part.createFormData("image", image.name, requestFile)

    RetrofitClient.apiService.uploadImage(body).enqueue(
        object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    Log.d("Upload", "Imagen subida exitosamente")
                } else {
                    Log.d("Upload", "Error al subir la imagen: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.d("Upload", "Error en la carga", t)
            }
        }
    )
}










