package com.example.gestordeevidencias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestordeevidencias.data.local.entities.EvidenceEntity
import com.example.gestordeevidencias.data.local.entities.ReportEntity
import com.example.gestordeevidencias.ui.screens.AnnotationScreen
import com.example.gestordeevidencias.ui.screens.CameraScreen
import com.example.gestordeevidencias.ui.screens.CreateReportScreen
import com.example.gestordeevidencias.ui.screens.ReportDetailScreen
import com.example.gestordeevidencias.ui.screens.ReportListScreen
import com.example.gestordeevidencias.ui.theme.GestorDeEvidenciasTheme
import com.example.gestordeevidencias.ui.viewmodel.ReportViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestorDeEvidenciasTheme {
                val viewModel: ReportViewModel = viewModel()
                var currentScreen by rememberSaveable { mutableStateOf("list") }
                var selectedReport by rememberSaveable { mutableStateOf<ReportEntity?>(null) }
                var selectedEvidence by rememberSaveable { mutableStateOf<EvidenceEntity?>(null) }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        if (targetState == "camera" || targetState == "annotation") {
                            slideInVertically { it } + fadeIn() togetherWith
                                    slideOutVertically { -it } + fadeOut()
                        } else {
                            slideInHorizontally { if (targetState == "list") -it else it } + fadeIn() togetherWith
                                    slideOutHorizontally { if (targetState == "list") it else -it } + fadeOut()
                        }
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        "list" -> {
                            ReportListScreen(
                                viewModel = viewModel,
                                onCreateNewReport = { currentScreen = "create" },
                                onReportClick = { report ->
                                    selectedReport = report
                                    currentScreen = "detail"
                                }
                            )
                        }
                        "create" -> {
                            CreateReportScreen(
                                viewModel = viewModel,
                                onReportCreated = { currentScreen = "list" }
                            )
                        }
                        "detail" -> {
                            selectedReport?.let { report ->
                                ReportDetailScreen(
                                    report = report,
                                    viewModel = viewModel,
                                    onBack = { currentScreen = "list" },
                                    onAddEvidence = { currentScreen = "camera" },
                                    onAnnotate = { evidence ->
                                        selectedEvidence = evidence
                                        currentScreen = "annotation"
                                    }
                                )
                            }
                        }
                        "camera" -> {
                            selectedReport?.let { report ->
                                CameraScreen(
                                    onPhotoCaptured = { path ->
                                        viewModel.addEvidence(report.id, path, "Nueva Evidencia")
                                        currentScreen = "detail"
                                    },
                                    onBack = { currentScreen = "detail" }
                                )
                            }
                        }
                        "annotation" -> {
                            selectedEvidence?.let { evidence ->
                                AnnotationScreen(
                                    evidence = evidence,
                                    onSave = { newPath ->
                                        viewModel.updateEvidence(evidence.copy(imagePath = newPath))
                                        currentScreen = "detail"
                                    },
                                    onCancel = { currentScreen = "detail" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
