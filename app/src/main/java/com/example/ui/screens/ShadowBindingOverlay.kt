package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ShadowSoldier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ShadowBindingOverlay(
    shadow: ShadowSoldier,
    onDismiss: () -> Unit
) {
    var extractionStage by remember { mutableStateOf("READY") } // "READY", "EXTRACTING", "SUCCESS"
    var ritualMessage by remember { mutableStateOf("Spirit frequency captured from completed habit core.") }
    val coroutineScope = rememberCoroutineScope()

    // Screen Shake Offset Animation for the extraction ritual
    val shakeOffset = remember { Animatable(0f) }
    
    // Rotating vortex animation
    val infiniteTransition = rememberInfiniteTransition(label = "vortex")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    // Pulsing shadow aura scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    // Trigger auto message cycle during extraction phase
    LaunchedEffect(extractionStage) {
        if (extractionStage == "EXTRACTING") {
            val messages = listOf(
                "Command code 'Awaken' registered in Nexus core...",
                "Initiating echo resonance alignment filters...",
                "Converting habit completion potential to spectral armor...",
                "Solidifying astral echo core parameters...",
                "Extraction finalizing... binding to Vanguard soul..."
            )
            
            // Screen Shake Effect loop
            val shakeJob = launch {
                while (true) {
                    shakeOffset.animateTo(8f, tween(35, easing = LinearEasing))
                    shakeOffset.animateTo(-8f, tween(35, easing = LinearEasing))
                    delay(35)
                }
            }
            
            for (msg in messages) {
                ritualMessage = msg
                delay(600)
            }
            
            shakeJob.cancel()
            shakeOffset.animateTo(0f, tween(50))
            extractionStage = "SUCCESS"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .clickable(enabled = false) {} // Prevent click-throughs
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = shakeOffset.value.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SystemCardDark)
                .border(
                    BorderStroke(
                        2.dp,
                        Brush.linearGradient(
                            if (extractionStage == "SUCCESS") listOf(SystemGold, SystemPurple)
                            else listOf(SystemPurple, SystemNeonCyan)
                        )
                    ),
                    RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storm,
                    contentDescription = null,
                    tint = if (extractionStage == "SUCCESS") SystemGold else SystemPurple,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = if (extractionStage == "SUCCESS") "BINDING COMPLETED" else "ECHO AWAKENING EXTRACTION",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp
                )
            }

            Divider(color = DarkGreyBorder, thickness = 1.dp)

            when (extractionStage) {
                "READY" -> {
                    Text(
                        text = "A lingering habit spirit is waiting for your awakening decree.",
                        color = TextMutedGrey,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    // Large Pulsing Holographic Shadow Gate Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(SystemPurple.copy(alpha = 0.25f), Color.Transparent)
                                )
                            )
                            .border(1.5.dp, SystemPurple, CircleShape)
                            .clickable {
                                extractionStage = "EXTRACTING"
                            }
                            .testTag("ritual_arise_gate")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .rotate(rotationAngle)
                                .background(
                                    Brush.radialGradient(
                                        listOf(SystemPurple, SystemNeonCyan, Color.Transparent)
                                    ),
                                    shape = CircleShape
                                )
                                .border(1.dp, SystemNeonCyan.copy(alpha = 0.4f), CircleShape)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "AWAKEN",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SPIRIT IDENTITY:",
                            color = SystemNeonCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = shadow.name.substringAfter("of "),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Button(
                        onClick = { extractionStage = "EXTRACTING" },
                        colors = ButtonDefaults.buttonColors(containerColor = SystemPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("extract_arise_button")
                    ) {
                        Text(
                            text = "COMMAND EXTRACTION",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                "EXTRACTING" -> {
                    // Spinning Shadow Vortex Loader
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(150.dp)
                    ) {
                        CircularProgressIndicator(
                            color = SystemPurple,
                            strokeWidth = 4.dp,
                            modifier = Modifier
                                .size(130.dp)
                                .rotate(rotationAngle)
                        )
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = SystemNeonCyan,
                            modifier = Modifier
                                .size(50.dp)
                                .rotate(-rotationAngle * 1.5f)
                        )
                    }

                    Text(
                        text = ritualMessage,
                        color = SystemNeonCyan,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .height(60.dp)
                            .padding(horizontal = 10.dp)
                    )
                }

                "SUCCESS" -> {
                    val classIcon = when (shadow.type) {
                        "Warrior" -> Icons.Default.Bolt
                        "Mage" -> Icons.Default.AutoAwesome
                        "Assassin" -> Icons.Default.ContentCut
                        "Tank" -> Icons.Default.Shield
                        else -> Icons.Default.Support
                    }
                    val classColor = when (shadow.type) {
                        "Warrior" -> SystemNeonCyan
                        "Mage" -> SystemPurple
                        "Assassin" -> SystemRed
                        "Tank" -> SystemNeonCyan
                        else -> SystemGold
                    }

                    // Exploded Crown Emblem / Complete Badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(130.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(SystemGold.copy(alpha = 0.2f), Color.Transparent)
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(SystemDarkBlue)
                                .border(2.dp, SystemGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = classIcon,
                                contentDescription = null,
                                tint = classColor,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        // Small overlay Level badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(SystemGold, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Lv. ${shadow.level}",
                                color = SystemDarkBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = shadow.name.substringAfter("of "),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "[${shadow.name.substringBefore(" of")}]",
                            color = SystemGold,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "TYPE: ${shadow.type} | COMBAT FORCE: ${shadow.power}",
                            color = SystemNeonCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SystemDarkBlue),
                        border = BorderStroke(1.dp, DarkGreyBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val passiveBuff = when (shadow.type) {
                                "Warrior" -> "+5% Habit Strength multipliers"
                                "Mage" -> "+8% Focus Mode focus recovery"
                                "Assassin" -> "+10% Agility speed bonuses"
                                "Tank" -> "+12% Vitality core buffer"
                                else -> "+15% Daily gold yields"
                            }
                            Text(
                                text = "SOUL BOND PASSIVE BUFF ACTIVE:",
                                color = SystemPurple,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = passiveBuff,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SystemGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ritual_confirm_button")
                    ) {
                        Text(
                            text = "DISMISS STATUS WINDOW",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SystemDarkBlue
                        )
                    }
                }
            }
        }
    }
}
