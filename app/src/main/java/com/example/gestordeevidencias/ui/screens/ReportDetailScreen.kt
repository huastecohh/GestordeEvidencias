package com.example.gestordeevidencias.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.gestordeevidencias.R
import com.example.gestordeevidencias.data.local.entities.EvidenceEntity
import com.example.gestordeevidencias.data.local.entities.ReportEntity
import com.example.gestordeevidencias.ui.viewmodel.ReportViewModel
import com.example.gestordeevidencias.util.WordExportHelper
import com.example.gestordeevidencias.util.PdfExportHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    report: ReportEntity,
    viewModel: ReportViewModel,
    onBack: () -> Unit,
    onAddEvidence: (Long) -> Unit,
    onAnnotate: (EvidenceEntity) -> Unit
) {
    val context = LocalContext.current
    val evidencesList by viewModel.getEvidences(report.id).collectAsState(initial = emptyList())
    
    var evidences by remember(evidencesList) { mutableStateOf(evidencesList) }
    var selectedEvidence by remember { mutableStateOf<EvidenceEntity?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { viewModel.importFromGallery(context, report.id, it) }
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(report.subject, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(report.studentName, style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Exportar")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Exportar a Word (.docx)") },
                                onClick = {
                                    showExportMenu = false
                                    val file = WordExportHelper.exportToWord(context, report, evidences)
                                    if (file != null) shareFile(context, file, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Exportar a PDF (.pdf)") },
                                onClick = {
                                    showExportMenu = false
                                    val file = PdfExportHelper.exportToPdf(context, report, evidences)
                                    if (file != null) shareFile(context, file, "application/pdf")
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (showAddMenu) {
                    SmallFloatingActionButton(
                        onClick = {
                            showAddMenu = false
                            galleryLauncher.launch("image/*")
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.List, "Galería")
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            showAddMenu = false
                            onAddEvidence(report.id)
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.Create, "Cámara")
                    }
                }
                FloatingActionButton(
                    onClick = { showAddMenu = !showAddMenu },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            InfoSection(report)
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            
            if (evidences.isEmpty()) {
                EmptyEvidenceState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(evidences) { index, evidence ->
                        StyledEvidenceItem(
                            evidence = evidence,
                            onEdit = {
                                selectedEvidence = it
                                showEditDialog = true
                            },
                            onMoveUp = if (index > 0) {
                                {
                                    val newList = evidences.toMutableList()
                                    val item = newList.removeAt(index)
                                    newList.add(index - 1, item)
                                    evidences = newList
                                    updateOrderInDb(viewModel, newList)
                                }
                            } else null,
                            onMoveDown = if (index < evidences.size - 1) {
                                {
                                    val newList = evidences.toMutableList()
                                    val item = newList.removeAt(index)
                                    newList.add(index + 1, item)
                                    evidences = newList
                                    updateOrderInDb(viewModel, newList)
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog && selectedEvidence != null) {
        EditEvidenceDialog(
            selectedEvidence!!,
            onDismiss = { showEditDialog = false },
            onSave = { label, desc ->
                viewModel.updateEvidence(selectedEvidence!!.copy(label = label, description = desc))
                showEditDialog = false
            },
            onAnnotate = {
                showEditDialog = false
                onAnnotate(selectedEvidence!!)
            }
        )
    }
}

@Composable
fun InfoSection(report: ReportEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SuggestionChip(
            onClick = { },
            label = { Text("Grado: ${report.grade}°") },
            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        )
        SuggestionChip(
            onClick = { },
            label = { Text("Grupo: ${report.group}") },
            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        )
        SuggestionChip(
            onClick = { },
            label = { Text("ID: #${report.id}") }
        )
    }
}

@Composable
fun StyledEvidenceItem(
    evidence: EvidenceEntity,
    onEdit: (EvidenceEntity) -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onEdit(evidence) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = evidence.imagePath,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                
                // Overlay para ordenamiento
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    if (onMoveUp != null) {
                        IconButton(onClick = onMoveUp) {
                            Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White)
                        }
                    }
                    if (onMoveDown != null) {
                        IconButton(onClick = onMoveDown) {
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White)
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = evidence.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (evidence.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = evidence.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyEvidenceState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = null,
                modifier = Modifier.size(80.dp).padding(bottom = 16.dp),
                tint = Color.Unspecified
            )
            Text("Aún no hay fotos", style = MaterialTheme.typography.bodyLarge)
            Text("Pulsa + para capturar una evidencia", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun EditEvidenceDialog(
    evidence: EvidenceEntity,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onAnnotate: () -> Unit
) {
    var newLabel by remember { mutableStateOf(evidence.label) }
    var newDescription by remember { mutableStateOf(evidence.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles de Evidencia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newLabel,
                    onValueChange = { newLabel = it },
                    label = { Text("Título de la foto") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newDescription,
                    onValueChange = { newDescription = it },
                    label = { Text("Descripción del proceso") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Button(
                    onClick = onAnnotate,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("✏️ Dibujar sobre la imagen")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(newLabel, newDescription) }) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun updateOrderInDb(viewModel: ReportViewModel, list: List<EvidenceEntity>) {
    list.forEachIndexed { index, evidence ->
        viewModel.updateEvidence(evidence.copy(orderIndex = index))
    }
}

private fun shareFile(context: android.content.Context, file: File, mimeType: String) {
    val uri = FileProvider.getUriForFile(
        context,
        "com.example.gestordeevidencias.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        setPackage("com.whatsapp")
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Compartir reporte"))
    } catch (e: Exception) {
        intent.setPackage(null)
        context.startActivity(Intent.createChooser(intent, "Compartir reporte"))
    }
}
