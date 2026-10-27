package com.example.ejemploglide

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.example.ejemploglide.ui.theme.EjemploGlideTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val CLOUDINARY_IMAGE_URL =
    "https://res.cloudinary.com/dtwtm70cj/image/upload/v1730290135/laravel/rgjz8vfxj8ynpvihtlfk.jpg"


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EjemploGlideTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DogImageScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DogImageScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var cloudinaryBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                cloudinaryBitmap = null
                errorMessage = null
                scope.launch {
                    try {
                        cloudinaryBitmap = fetchCloudinaryBitmap()
                    } catch (exception: Exception) {
                        errorMessage = exception.localizedMessage ?: "No se pudo cargar la imagen de Cloudinary"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Cargar imagen de Cloudinary")
        }

        ImageBox(
            cloudinaryBitmap = cloudinaryBitmap,
            errorMessage = errorMessage,
        )
    }
}

@Composable
private fun ImageBox(
    cloudinaryBitmap: Bitmap?,
    errorMessage: String?,
) {
    Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
        when {
            errorMessage != null -> Text(text = errorMessage)
            cloudinaryBitmap == null -> Text(text = "Pulsa el botón para cargar la imagen")
            cloudinaryBitmap != null -> CloudinaryImage(bitmap = cloudinaryBitmap)
        }
    }
}


@Composable
private fun CloudinaryImage(bitmap: Bitmap, modifier: Modifier = Modifier) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Imagen descargada desde Cloudinary",
        modifier = modifier.fillMaxSize()
    )
}

private suspend fun fetchCloudinaryBitmap(): Bitmap = withContext(Dispatchers.IO) {
    val connection = (URL(CLOUDINARY_IMAGE_URL).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 10_000
        readTimeout = 10_000
    }
    try {
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException("El servidor respondió con HTTP ${connection.responseCode}")
        }
        val bitmap = connection.inputStream.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
        bitmap ?: throw IllegalStateException("La respuesta no contiene una imagen válida")
    } finally {
        connection.disconnect()
    }
}



//@Preview(showBackground = true)
//@Composable
//fun DogImageScreenPreview() {
//    EjemploGlideTheme {
//        DogImageScreen()
//    }
//}
