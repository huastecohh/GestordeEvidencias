package com.example.gestordeevidencias.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.gestordeevidencias.data.local.entities.EvidenceEntity
import java.io.File
import java.io.FileOutputStream

enum class AnnotationTool { PENCIL, MOVE, ZOOM, UNDO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationScreen(
    evidence: EvidenceEntity,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var activeTool by remember { mutableStateOf(AnnotationTool.PENCIL) }
    
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
    var triggerRedraw by remember { mutableStateOf(0) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        if (activeTool == AnnotationTool.MOVE || activeTool == AnnotationTool.ZOOM) {
            if (activeTool == AnnotationTool.ZOOM) scale *= zoomChange
            if (activeTool == AnnotationTool.MOVE) offset += offsetChange
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color(0xFF12151F),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Anotar evidencia",
                        style = MaterialTheme.typography.titleMedium,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, null, tint = androidx.compose.ui.graphics.Color.White.copy(0.6f))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val file = File(context.getExternalFilesDir(null), "Annotated_${System.currentTimeMillis()}.jpg")
                        val out = FileOutputStream(file)
                        canvasBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        out.close()
                        onSave("file://" + file.absolutePath)
                    }) {
                        Icon(Icons.Default.Check, null, tint = androidx.compose.ui.graphics.Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF1A1D2B)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(androidx.compose.ui.graphics.Color.Black)
                .onSizeChanged { canvasSize = it }
                .transformable(state = transformState)
                .pointerInput(activeTool, scale, offset, canvasSize) {
                    if (activeTool == AnnotationTool.PENCIL) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            
                            val imgWidth = canvasBitmap.width.toFloat()
                            val imgHeight = canvasBitmap.height.toFloat()
                            
                            fun screenToBitmap(pos: Offset): Offset {
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

            // Barra flotante LATERAL DERECHA
            FloatingAnnotationToolbarPremium(
                activeTool = activeTool,
                onToolSelected = { 
                    if (it == AnnotationTool.UNDO) {
                        // Implement undo logic if needed
                    } else {
                        activeTool = it
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            )
        }
    }
}

@Composable
fun FloatingAnnotationToolbarPremium(
    activeTool: AnnotationTool,
    onToolSelected: (AnnotationTool) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = androidx.compose.ui.graphics.Color(0xFF0F1120).copy(alpha = 0.88f),
        border = BorderStroke(0.5.dp, androidx.compose.ui.graphics.Color.White.copy(0.1f)),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ToolButtonPremium(icon = Icons.Outlined.Edit, tool = AnnotationTool.PENCIL, active = activeTool, onSelect = onToolSelected)
            
            HorizontalDivider(modifier = Modifier.width(24.dp).padding(vertical = 2.dp), color = androidx.compose.ui.graphics.Color.White.copy(0.1f))

            ToolButtonPremium(icon = Icons.Outlined.PanTool, tool = AnnotationTool.MOVE, active = activeTool, onSelect = onToolSelected)
            ToolButtonPremium(icon = Icons.Outlined.ZoomIn, tool = AnnotationTool.ZOOM, active = activeTool, onSelect = onToolSelected)

            HorizontalDivider(modifier = Modifier.width(24.dp).padding(vertical = 2.dp), color = androidx.compose.ui.graphics.Color.White.copy(0.1f))

            ToolButtonPremium(icon = Icons.AutoMirrored.Outlined.Undo, tool = AnnotationTool.UNDO, active = activeTool, onSelect = onToolSelected, tint = androidx.compose.ui.graphics.Color(0xFFFF6B6B))
        }
    }
}

@Composable
private fun ToolButtonPremium(
    icon: ImageVector,
    tool: AnnotationTool,
    active: AnnotationTool,
    onSelect: (AnnotationTool) -> Unit,
    tint: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White
) {
    val isActive = tool == active
    val bgColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
        animationSpec = tween(150),
        label = "toolBg"
    )
    Surface(
        onClick = { onSelect(tool) },
        modifier = Modifier.size(36.dp),
        shape = MaterialTheme.shapes.small,
        color = bgColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = tool.name,
                modifier = Modifier.size(18.dp),
                tint = if (isActive) androidx.compose.ui.graphics.Color.White else tint.copy(alpha = 0.6f)
            )
        }
    }
}
