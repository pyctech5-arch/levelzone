package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.data.PlayerStats
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarChartContainer(
    stats: PlayerStats,
    habits: List<Habit>,
    modifier: Modifier = Modifier
) {
    // 1. Calculate values for each category
    // Base stat + bonus for each active habit in this category completed today
    val physicalCompleted = habits.count { it.category == "Physical Training" && it.isCompletedToday }
    val mentalCompleted = habits.count { it.category == "Mental Growth" && it.isCompletedToday }
    val mindfulnessCompleted = habits.count { it.category == "Mindfulness Focus" && it.isCompletedToday }
    val wellnessCompleted = habits.count { it.category == "Active Wellness" && it.isCompletedToday }
    val disciplineCompleted = habits.count { it.category == "Core Discipline" && it.isCompletedToday }

    val strValue = stats.strength.toFloat() + (physicalCompleted * 3f)
    val intValue = stats.intelligence.toFloat() + (mentalCompleted * 3f)
    val senValue = stats.sense.toFloat() + (mindfulnessCompleted * 3f)
    val vitValue = stats.vitality.toFloat() + (wellnessCompleted * 3f)
    val agiValue = stats.agility.toFloat() + (disciplineCompleted * 3f)

    // Find max value to auto-scale the radar (with a minimum max of 50 to look balanced)
    val maxValue = maxOf(50f, strValue, intValue, senValue, vitValue, agiValue) * 1.15f

    // 2. Setup animated values for smooth sci-fi opening
    val animatedStr by animateFloatAsState(targetValue = strValue, animationSpec = tween(1200))
    val animatedInt by animateFloatAsState(targetValue = intValue, animationSpec = tween(1200))
    val animatedSen by animateFloatAsState(targetValue = senValue, animationSpec = tween(1200))
    val animatedVit by animateFloatAsState(targetValue = vitValue, animationSpec = tween(1200))
    val animatedAgi by animateFloatAsState(targetValue = agiValue, animationSpec = tween(1200))

    val radarData = listOf(
        RadarAxisData("Physical Training", "STR", animatedStr, SystemNeonCyan),
        RadarAxisData("Mental Growth", "INT", animatedInt, SystemGold),
        RadarAxisData("Mindfulness Focus", "SEN", animatedSen, SystemPurple),
        RadarAxisData("Active Wellness", "VIT", animatedVit, ColorRankD),
        RadarAxisData("Core Discipline", "AGI", animatedAgi, SystemNeonBlue)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SystemCardDark)
            .border(1.dp, DarkGreyBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "▶ SOUL ATTRIBUTE RESONANCE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = SystemNeonCyan,
                letterSpacing = 1.sp
            )
            Text(
                text = "ACTIVE SYNC",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = ColorRankD,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(ColorRankD.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Beautiful Interactive Radar View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            RadarChart(
                data = radarData,
                maxValue = maxValue
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Brief Explanatory Note
        Text(
            text = "Holographic chart monitors category stats directly. Complete Quests matching each domain to expand its vertex boundary.",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = TextMutedGrey,
            lineHeight = 14.sp
        )
    }
}

data class RadarAxisData(
    val label: String,
    val statAbbr: String,
    val value: Float,
    val color: Color
)

@Composable
fun RadarChart(
    data: List<RadarAxisData>,
    maxValue: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2
        val radius = minOf(width, height) / 2 * 0.75f // Leave room for labels

        val numAxes = data.size
        val angleStep = (2 * Math.PI / numAxes).toFloat()

        // 1. Draw Background Concentric Pentagons (Web Grid)
        val gridLevels = 5
        for (level in 1..gridLevels) {
            val ratio = level.toFloat() / gridLevels
            val levelRadius = radius * ratio
            val gridPath = Path()

            for (i in 0 until numAxes) {
                val angle = (i * angleStep) - (Math.PI / 2).toFloat()
                val x = centerX + levelRadius * cos(angle)
                val y = centerY + levelRadius * sin(angle)

                if (i == 0) {
                    gridPath.moveTo(x, y)
                } else {
                    gridPath.lineTo(x, y)
                }
            }
            gridPath.close()

            // Outer ring has slightly higher opacity
            val alpha = if (level == gridLevels) 0.35f else 0.12f
            drawPath(
                path = gridPath,
                color = SystemNeonCyan.copy(alpha = alpha),
                style = Stroke(width = if (level == gridLevels) 1.5.dp.toPx() else 1.dp.toPx())
            )
        }

        // 2. Draw Axes radiating from the center to vertices
        for (i in 0 until numAxes) {
            val angle = (i * angleStep) - (Math.PI / 2).toFloat()
            val outerX = centerX + radius * cos(angle)
            val outerY = centerY + radius * sin(angle)

            drawLine(
                color = SystemNeonCyan.copy(alpha = 0.15f),
                start = Offset(centerX, centerY),
                end = Offset(outerX, outerY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 3. Draw Player Resonance filled area
        val fillPath = Path()
        for (i in 0 until numAxes) {
            val angle = (i * angleStep) - (Math.PI / 2).toFloat()
            val axisValue = data[i].value
            val valueRatio = (axisValue / maxValue).coerceIn(0.1f, 1.0f)
            val pointRadius = radius * valueRatio
            val x = centerX + pointRadius * cos(angle)
            val y = centerY + pointRadius * sin(angle)

            if (i == 0) {
                fillPath.moveTo(x, y)
            } else {
                fillPath.lineTo(x, y)
            }
        }
        fillPath.close()

        // Fill resonance area with dynamic purple / cyan hologram gradient
        drawPath(
            path = fillPath,
            brush = Brush.radialGradient(
                colors = listOf(SystemPurple.copy(alpha = 0.45f), SystemNeonCyan.copy(alpha = 0.15f)),
                center = Offset(centerX, centerY),
                radius = radius
            )
        )

        // Draw resonance outline with clean stroke
        drawPath(
            path = fillPath,
            color = SystemPurple,
            style = Stroke(width = 2.dp.toPx())
        )

        // 4. Draw individual vertex points
        for (i in 0 until numAxes) {
            val angle = (i * angleStep) - (Math.PI / 2).toFloat()
            val axisValue = data[i].value
            val valueRatio = (axisValue / maxValue).coerceIn(0.1f, 1.0f)
            val pointRadius = radius * valueRatio
            val x = centerX + pointRadius * cos(angle)
            val y = centerY + pointRadius * sin(angle)

            // Draw outer glowing indicator
            drawCircle(
                color = data[i].color,
                radius = 5.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = Offset(x, y)
            )
        }

        // 5. Draw Labels
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 24f
            typeface = android.graphics.Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val supportPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(180, 0, 229, 255) // Neon Cyan
            textSize = 20f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        for (i in 0 until numAxes) {
            val angle = (i * angleStep) - (Math.PI / 2).toFloat()
            
            // Push labels slightly further than the radar radius
            val labelRadius = radius + 22.dp.toPx()
            val x = centerX + labelRadius * cos(angle)
            val y = centerY + labelRadius * sin(angle)

            val axis = data[i]
            val valueText = "${axis.value.toInt()}"

            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas

                // Adjust label height offset slightly based on their vertical positioning
                var yOffset = y
                if (angle > -Math.PI / 6 && angle < 7 * Math.PI / 6) {
                    // bottom half labels
                    yOffset += 12f
                } else {
                    // top half labels
                    yOffset -= 8f
                }

                // Split label into abbreviation + name for readability
                nativeCanvas.drawText(
                    "${axis.statAbbr} ($valueText)",
                    x,
                    yOffset,
                    paint
                )
                nativeCanvas.drawText(
                    axis.label,
                    x,
                    yOffset + 24f,
                    supportPaint
                )
            }
        }
    }
}
