package com.example.gestordeevidencias.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.gestordeevidencias.data.local.entities.EvidenceEntity
import com.example.gestordeevidencias.data.local.entities.ReportEntity
import com.example.gestordeevidencias.ui.components.EvidenceListItem
import com.example.gestordeevidencias.ui.components.PdfPreviewDialog
import com.example.gestordeevidencias.ui.viewmodel.ReportViewModel
import com.example.gestordeevidencias.util.PdfExportHelper
import com.example.gestordeevidencias.util.ShareHelper
import com.example.gestordeevidencias.util.WordExportHelper
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
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
    
    // Internal state for reordering
    var listForReorder by remember(evidencesList) { mutableStateOf(evidencesList) }
    
    var selectedEvidence by remember { mutableStateOf<EvidenceEntity?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }
    var showPdfPreview by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { viewModel.importFromGallery(context, report.id, it) }
        }
    )

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            listForReorder = listForReorder.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            updateOrderInDb(viewModel, listForReorder)
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(report.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(report.studentName, style = MaterialTheme.typography.labelSmall)
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
                                    val file = WordExportHelper.exportToWord(context, report, evidencesList)
                                    if (file != null) ShareHelper.shareFile(context, file, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Exportar a PDF (.pdf)") },
                                onClick = {
                                    showExportMenu = false
                                    showPdfPreview = true
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
                        Icon(Icons.Default.Image, "Galería")
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            showAddMenu = false
                            onAddEvidence(report.id)
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, "Cámara")
                    }
                }
                FloatingActionButton(
                    onClick = { showAddMenu = !showAddMenu },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(if (showAddMenu) Icons.Default.Close else Icons.Default.Add, contentDescription = "Añadir")
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
            InfoSectionPremium(report)
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            
            if (evidencesList.isEmpty()) {
                EmptyEvidenceStatePremium()
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(listForReorder, key = { _, item -> item.id }) { index, evidence ->
                        ReorderableItem(reorderState, key = evidence.id) { isDragging ->
                            EvidenceListItem(
                                evidence = evidence,
                                index = index,
                                isDragging = isDragging,
                                onClick = {
                                    selectedEvidence = evidence
                                    showEditDialog = true
                                },
                                dragHandle = {
                                    IconButton(
                                        modifier = Modifier.draggableHandle(),
                                        onClick = {}
                                    ) {
                                        Icon(
                                            Icons.Outlined.DragHandle,
                                            contentDescription = "Reordenar",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPdfPreview) {
        PdfPreviewDialog(
            report = report,
            evidences = evidencesList,
            onDismiss = { showPdfPreview = false },
            onConfirm = {
                val file = PdfExportHelper.exportToPdf(context, report, evidencesList)
                if (file != null) ShareHelper.shareFile(context, file, "application/pdf")
                showPdfPreview = false
            },
            onRotateImage = { evidence ->
                viewModel.updateEvidence(evidence.copy(rotation = (evidence.rotation + 90f) % 360f))
            },
            onScaleImage = { evidence, newScale ->
                viewModel.updateEvidence(evidence.copy(scale = newScale))
            }
        )
    }

    if (showEditDialog && selectedEvidence != null) {
        EditEvidenceDialogPremium(
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
fun InfoSectionPremium(report: ReportEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = { },
            label = { Text("Grado: ${report.grade}°") },
            leadingIcon = { Icon(Icons.Default.School, null, Modifier.size(16.dp)) }
        )
        AssistChip(
            onClick = { },
            label = { Text("Grupo: ${report.group}") },
            leadingIcon = { Icon(Icons.Default.Group, null, Modifier.size(16.dp)) }
        )
    }
}

@Composable
fun EmptyEvidenceStatePremium() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(16.dp))
            Text("Aún no hay evidencias", style = MaterialTheme.typography.titleMedium)
            Text("Usa el botón + para empezar", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun EditEvidenceDialogPremium(
    evidence: EvidenceEntity,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onAnnotate: () -> Unit
) {
    var newLabel by remember { mutableStateOf(evidence.label) }
    var newDescription by remember { mutableStateOf(evidence.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles de Evidencia", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newLabel,
                    onValueChange = { newLabel = it },
                    label = { Text("Título") },
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Dibujar sobre la imagen")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(newLabel, newDescription) }) {
                Text("Guardar")
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
