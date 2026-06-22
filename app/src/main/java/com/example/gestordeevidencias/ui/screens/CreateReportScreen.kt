package com.example.gestordeevidencias.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestordeevidencias.ui.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    viewModel: ReportViewModel,
    onReportCreated: () -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }
    var studentName by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nuevo Proyecto", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Ingresa los detalles del reporte académico",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            StyledTextField(
                value = subject,
                onValueChange = { subject = it },
                label = "Materia",
                icon = Icons.Default.List
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StyledTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = "Grado",
                    icon = Icons.Default.Info,
                    modifier = Modifier.weight(1f)
                )
                StyledTextField(
                    value = group,
                    onValueChange = { group = it },
                    label = "Grupo",
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
            }

            StyledTextField(
                value = studentName,
                onValueChange = { studentName = it },
                label = "Nombre del Estudiante",
                icon = Icons.Default.AccountBox
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (subject.isNotBlank() && studentName.isNotBlank()) {
                        viewModel.createReport(subject, grade, group, studentName)
                        onReportCreated()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = subject.isNotBlank() && studentName.isNotBlank()
            ) {
                Text("Continuar a Evidencias", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}
