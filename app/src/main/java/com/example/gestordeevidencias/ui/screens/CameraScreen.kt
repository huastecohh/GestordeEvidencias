package com.example.gestordeevidencias.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    onPhotoCaptured: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Forzar orientación horizontal
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraView(onPhotoCaptured, onBack)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Se necesita permiso de cámara para continuar")
        }
    }
}

@Composable
private fun CameraView(
    onPhotoCaptured: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    val preview = Preview.Builder().build()
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                preview.setSurfaceProvider(previewView.surfaceProvider)
            } catch (e: Exception) {
                Log.e("CameraScreen", "Binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Botón de captura a la derecha (estilo cámara tradicional en landscape)
        IconButton(
            onClick = {
                takePhoto(context, imageCapture, cameraExecutor) { uri ->
                    onPhotoCaptured(uri.toString())
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 48.dp)
                .size(72.dp)
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = Color.White.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(4.dp, Color.White),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(56.dp)
                    ) {}
                }
            }
        }
        
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(32.dp))
        }
        
        // Indicador visual de modo horizontal
        Text(
            "Modo Horizontal Activado",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: ExecutorService,
    onPhotoCaptured: (Uri) -> Unit
) {
    val outputDirectory = getOutputDirectory(context)
    val photoFile = File(
        outputDirectory,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onPhotoCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "Photo capture failed: ${exception.message}", exception)
            }
        }
    )
}

private fun getOutputDirectory(context: Context): File {
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
        File(it, "GestorEvidencias").apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
}
