package com.example.gestordeevidencias.ui.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// 5.1 Animación de entrada para nuevas evidencias (slideIn + fadeIn)
fun evidenceEnterTransition(): EnterTransition =
    slideInVertically(
        initialOffsetY = { it / 3 },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
    ) + fadeIn(animationSpec = tween(250))

fun evidenceExitTransition(): ExitTransition =
    slideOutVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(150))

// 5.2 Botón de exportar con feedback visual
@Composable
fun ExportButtonPremium(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    var exported by remember { mutableStateOf(false) }
    val containerColor by animateColorAsState(
        targetValue = if (exported) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "exportColor"
    )

    Button(
        onClick = {
            onClick()
            exported = true
        },
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.small
    ) {
        AnimatedContent(
            targetState = exported,
            transitionSpec = { (fadeIn(tween(200)) togetherWith fadeOut(tween(150))).using(SizeTransform(clip = false)) },
            label = "exportIcon"
        ) { done ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (done) Icons.Default.Check else icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(if (done) "¡Exportado!" else label)
            }
        }
    }
}
