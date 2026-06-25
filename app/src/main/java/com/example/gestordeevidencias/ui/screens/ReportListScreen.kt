package com.example.gestordeevidencias.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestordeevidencias.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.gestordeevidencias.data.local.entities.ReportEntity
import com.example.gestordeevidencias.ui.components.PdfPreviewDialog
import com.example.gestordeevidencias.ui.components.ReportCard
import com.example.gestordeevidencias.ui.theme.GestorDeEvidenciasTheme
import com.example.gestordeevidencias.ui.viewmodel.ReportViewModel
import com.example.gestordeevidencias.util.PdfExportHelper
import com.example.gestordeevidencias.util.ShareHelper
import com.example.gestordeevidencias.util.WordExportHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    viewModel: ReportViewModel,
    onCreateNewReport: () -> Unit,
    onReportClick: (ReportEntity) -> Unit
) {
    val reports by viewModel.reports.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var reportForPdfPreview by remember { mutableStateOf<ReportEntity?>(null) }

    if (reportForPdfPreview != null) {
        val evidences by viewModel.getEvidences(reportForPdfPreview!!.id).collectAsState(initial = emptyList())
        PdfPreviewDialog(
            report = reportForPdfPreview!!,
            evidences = evidences,
            onDismiss = { reportForPdfPreview = null },
            onConfirm = {
                val file = PdfExportHelper.exportToPdf(context, reportForPdfPreview!!, evidences)
                if (file != null) ShareHelper.shareFile(context, file, "application/pdf")
                reportForPdfPreview = null
            },
            onRotateImage = { evidence ->
                viewModel.updateEvidence(evidence.copy(rotation = (evidence.rotation + 90f) % 360f))
            },
            onScaleImage = { evidence, newScale ->
                viewModel.updateEvidence(evidence.copy(scale = newScale))
            }
        )
    }

    ReportListContent(
        reports = reports,
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onCreateNewReport = onCreateNewReport,
        onReportClick = onReportClick,
        onExportPdf = { report ->
            reportForPdfPreview = report
        },
        onExportWord = { report ->
            scope.launch {
                val evidences = viewModel.getEvidences(report.id).first()
                val file = WordExportHelper.exportToWord(context, report, evidences)
                if (file != null) ShareHelper.shareFile(context, file, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListContent(
    reports: List<ReportEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCreateNewReport: () -> Unit,
    onReportClick: (ReportEntity) -> Unit,
    onExportPdf: (ReportEntity) -> Unit,
    onExportWord: (ReportEntity) -> Unit
) {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
                LargeTopAppBar(
                    title = { 
                        Text(
                            "Mis Reportes",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ) 
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
                
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar alumno o materia...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateNewReport,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, "Nuevo") },
                text = { Text("Crear Reporte") }
            )
        }
    ) { paddingValues ->
        if (reports.isEmpty() && searchQuery.isEmpty()) {
            EmptyState(modifier = Modifier.padding(paddingValues))
        } else if (reports.isEmpty() && searchQuery.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No se encontraron resultados para '$searchQuery'", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = reports, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        evidenceCount = 0,
                        onClick = { onReportClick(report) },
                        onExportPdf = { onExportPdf(report) },
                        onExportWord = { onExportWord(report) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "No hay reportes creados",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Presiona el botón para comenzar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportListScreenPreview() {
    val sampleReports = listOf(
        ReportEntity(id = 1, subject = "Matemáticas", grade = "1", group = "A", studentName = "Juan Pérez"),
        ReportEntity(id = 2, subject = "Español", grade = "2", group = "B", studentName = "María García"),
        ReportEntity(id = 3, subject = "Ciencias", grade = "3", group = "C", studentName = "Carlos López")
    )
    
    GestorDeEvidenciasTheme {
        ReportListContent(
            reports = sampleReports,
            searchQuery = "",
            onSearchQueryChange = {},
            onCreateNewReport = {},
            onReportClick = {},
            onExportPdf = {},
            onExportWord = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReportListScreenEmptyPreview() {
    GestorDeEvidenciasTheme {
        ReportListContent(
            reports = emptyList(),
            searchQuery = "",
            onSearchQueryChange = {},
            onCreateNewReport = {},
            onReportClick = {},
            onExportPdf = {},
            onExportWord = {}
        )
    }
}
