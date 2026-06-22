package com.example.gestordeevidencias.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.gestordeevidencias.data.local.entities.EvidenceEntity
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationScreen(
    evidence: EvidenceEntity,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    
    // Carga de la imagen original
    val bitmap = remember {
        val options = BitmapFactory.Options().apply { inMutable = true }
        val path = evidence.imagePath.removePrefix("file://")
        BitmapFactory.decodeFile(path, options) ?: Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    }
    
    // Bitmap mutable para dibujar
    val canvasBitmap = remember { bitmap.copy(Bitmap.Config.ARGB_8888, true) }
    val drawCanvas = remember { Canvas(canvasBitmap) }
    val paint = remember {
        Paint().apply {
            color = Color.RED
            strokeWidth = 15f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
    }

    // Estados de transformación (Zoom y Pan)
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isDrawMode by remember { mutableStateOf(true) } // Alternar entre dibujar y mover
    var triggerRedraw by remember { mutableStateOf(0) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        if (!isDrawMode) {
            scale *= zoomChange
            offset += offsetChange
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Anotar Evidencia") },
                navigationIcon = {
                    IconButton(onClick = onCancel) { Icon(Icons.Default.Close, null) }
                },
                actions = {
                    IconButton(onClick = {
                        val file = File(context.getExternalFilesDir(null), "Annotated_${System.currentTimeMillis()}.jpg")
                        val out = FileOutputStream(file)
                        canvasBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        out.close()
                        onSave("file://" + file.absolutePath)
                    }) {
                        Icon(Icons.Default.Check, null)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(
                        onClick = { isDrawMode = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isDrawMode) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.Edit, "Modo Dibujo")
                    }
                    IconButton(
                        onClick = { isDrawMode = false },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (!isDrawMode) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.Menu, "Modo Mover/Zoom")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { 
                        scale = 1f
                        offset = Offset.Zero
                    }) {
                        Text("Reset Vista")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black)
                .onSizeChanged { canvasSize = it }
                .transformable(state = transformState)
                .pointerInput(isDrawMode, scale, offset, canvasSize) {
                    if (isDrawMode) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            
                            // Cálculo de coordenadas relativas al bitmap
                            // 1. Ajustar por el offset y escala de la UI
                            // 2. Mapear del tamaño del Canvas de Compose al tamaño real del Bitmap
                            
                            val imgWidth = canvasBitmap.width.toFloat()
                            val imgHeight = canvasBitmap.height.toFloat()
                            
                            // Factor de escala intrínseco (cómo encaja el bitmap en el componente)
                            val factorX = imgWidth / canvasSize.width
                            val factorY = imgHeight / canvasSize.height
                            val fitScale = minOf(1/factorX, 1/factorY) * canvasSize.width / imgWidth
                            
                            fun screenToBitmap(pos: Offset): Offset {
                                val centeredOffset = Offset(
                                    (canvasSize.width - imgWidth * scale * (1/factorX)) / 2,
                                    (canvasSize.height - imgHeight * scale * (1/factorY)) / 2
                                )
                                // Simplificación: Usamos una transformación directa para el MVP
                                // Para precisión quirúrgica se requiere una matriz de transformación inversa
                                return Offset(
                                    (pos.x - offset.x) * (imgWidth / (canvasSize.width * scale)),
                                    (pos.y - offset.y) * (imgHeight / (canvasSize.height * scale))
                                )
                            }

                            val start = screenToBitmap(change.position - dragAmount)
                            val end = screenToBitmap(change.position)

                            drawCanvas.drawLine(start.x, start.y, end.x, end.y, paint)
                            triggerRedraw++
                        }
                    }
                }
        ) {
            key(triggerRedraw) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                ) {
                    drawImage(
                        image = canvasBitmap.asImageBitmap(),
                        dstSize = IntSize(size.width.toInt(), size.height.toInt())
                    )
                }
            }
            
            if (!isDrawMode) {
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Usa dos dedos para Zoom o arrastra para mover",
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
