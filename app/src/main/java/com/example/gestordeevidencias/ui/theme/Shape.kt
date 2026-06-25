package com.example.gestordeevidencias.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // Chips, badges
    small      = RoundedCornerShape(8.dp),   // Inputs, botones secundarios
    medium     = RoundedCornerShape(12.dp),  // Diálogos, bottom sheets
    large      = RoundedCornerShape(16.dp),  // Tarjetas de proyecto
    extraLarge = RoundedCornerShape(20.dp),  // Hojas modales grandes
)
