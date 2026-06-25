package com.example.gestordeevidencias.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.gestordeevidencias.data.local.entities.EvidenceEntity
import com.example.gestordeevidencias.data.local.entities.ReportEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewDialog(
    report: ReportEntity,
    evidences: List<EvidenceEntity>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onRotateImage: (EvidenceEntity) -> Unit,
    onScaleImage: (EvidenceEntity, Float) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Vista Previa PDF (2 por hoja)") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    },
                    actions = {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Generar")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.TopCenter
            ) {
                val pages = evidences.chunked(2)
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(pages.size) { pageIndex ->
                        val chunk = pages[pageIndex]
                        
                        // Mimicking an A4 page
                        Card(
                            modifier = Modifier
                                .width(380.dp) // Proportional A4 width
                                .height(537.dp), // Proportional A4 height
                            shape = RoundedCornerShape(0.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxSize()
                            ) {
                                if (pageIndex == 0) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "REPORTE ACADÉMICO DE EVIDENCIAS",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 14.sp
                                        )
                                        Spacer(Modifier.height(12.dp))
                                    }
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        PdfInfoRow("Materia:", report.subject)
                                        PdfInfoRow("Estudiante:", report.studentName)
                                        PdfInfoRow("Grado/Grup:", "${report.grade}° ${report.group}")
                                        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(report.createdAt)
                                        PdfInfoRow("Fecha:", date)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                }
                                
                                chunk.forEach { evidence ->
                                    val isVerticalPreview = (evidence.rotation / 90f).toInt() % 2 != 0
                                    
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Título: ${evidence.label}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Slider(
                                                    value = evidence.scale,
                                                    onValueChange = { onScaleImage(evidence, it) },
                                                    valueRange = 0.5f..1.5f,
                                                    modifier = Modifier.width(100.dp)
                                                )
                                                IconButton(
                                                    onClick = { onRotateImage(evidence) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_rotate),
                                                        contentDescription = "Girar",
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                        
                                        if (evidence.description.isNotBlank()) {
                                            Text(
                                                "Proceso: ${evidence.description}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.DarkGray,
                                                maxLines = 1,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(evidence.scale.coerceIn(0.1f, 1f))
                                                .aspectRatio(if (isVerticalPreview) 0.7f else 1.4f)
                                                .padding(vertical = 4.dp)
                                                .background(Color.LightGray.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = evidence.imagePath,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .graphicsLayer { rotationZ = evidence.rotation },
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                    if (chunk.indexOf(evidence) == 0 && chunk.size > 1) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                                    }
                                }
                                
                                Spacer(Modifier.weight(0.01f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Pág ${pageIndex + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        "Gestor de Evidencias - Mtro. Salvador Perez Zamoran",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfInfoRow(label: String, value: String) {
    Row {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black
        )
    }
}
