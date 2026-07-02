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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.QuestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FocusModeScreen(
    viewModel: QuestViewModel
) {
    var timerDuration by remember { mutableStateOf(25 * 60) } // Default 25 min
    var timeLeft by remember { mutableStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var showCompletionReward by remember { mutableStateOf(false) }
    var activeSoundtrack by remember { mutableStateOf("Warden's Sanctuary") }

    val coroutineScope = rememberCoroutineScope()

    // Timer countdown loop
    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000)
            timeLeft--
            if (timeLeft == 0) {
                isRunning = false
                showCompletionReward = true
                
                // Add actual rewards to player stats using safe ViewModel helper
                viewModel.awardFocusRewards(80, 40)
                viewModel.triggerSystemAlert("🔥 FOCUS CLEARED: Focused state solidified! Obtained +80 XP & +40 Gold!")
            }
        }
    }

    // Interactive circular animation progress
    val progress = if (timerDuration > 0) timeLeft.toFloat() / timerDuration else 0f

    // Animated breathing aura for active focus
    val infiniteTransition = rememberInfiniteTransition(label = "focus_aura")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "aura_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemDarkBlue)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "SYSTEM FOCUS MODE",
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 2.sp
        )
        Text(
            text = "Minimize external psychic interference. Lock your consciousness into the active time calibration loop.",
            color = TextMutedGrey,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Holographic Countdown Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .padding(16.dp)
        ) {
            // Animated breathing aura
            if (isRunning) {
                Box(
                    modifier = Modifier
                        .size(200.dp * auraScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(SystemNeonCyan.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                )
            }

            CircularProgressIndicator(
                progress = progress,
                color = SystemNeonCyan,
                trackColor = DarkGreyBorder,
                strokeWidth = 8.dp,
                modifier = Modifier.size(200.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val minutes = timeLeft / 60
                val seconds = timeLeft % 60
                val timeString = String.format("%02d:%02d", minutes, seconds)
                
                Text(
                    text = timeString,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "CALIBRATING",
                    color = SystemNeonCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Timer presets (Only visible when not running)
        AnimatedVisibility(
            visible = !isRunning,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(10, 25, 50).forEach { mins ->
                    val isSelected = timerDuration == mins * 60
                    Button(
                        onClick = {
                            timerDuration = mins * 60
                            timeLeft = mins * 60
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) SystemNeonCyan else SystemCardDark,
                            contentColor = if (isSelected) SystemDarkBlue else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.0f)
                    ) {
                        Text("${mins}m", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Soundtrack selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SystemCardDark),
            border = BorderStroke(1.dp, DarkGreyBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ACTIVE MANA SOUNDTRACK:",
                    color = SystemPurple,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                listOf("Warden's Sanctuary", "Midnight Dungeon Ambience", "Quiet Village Morning").forEach { sound ->
                    val isSelected = activeSoundtrack == sound
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) SystemPurple.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { activeSoundtrack = sound }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = null,
                            tint = if (isSelected) SystemPurple else TextMutedGrey,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sound,
                            color = if (isSelected) Color.White else TextMutedGrey,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Control Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isRunning) {
                Button(
                    onClick = {
                        isRunning = false
                        timeLeft = timerDuration
                        viewModel.triggerSystemAlert("⚠️ FOCUS ABORTED: Time dilation stream disrupted.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SystemRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.0f)
                        .height(48.dp)
                        .testTag("abort_focus_button")
                ) {
                    Text("ABORT MISSION", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { isRunning = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SystemNeonCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.0f)
                        .height(48.dp)
                        .testTag("start_focus_button")
                ) {
                    Text("INITIATE DILATION", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = SystemDarkBlue)
                }
            }
        }

        // Completion chest overlay
        if (showCompletionReward) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SystemCardDark)
                    .border(BorderStroke(1.5.dp, SystemGold), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = SystemGold, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "FOCUS SEQUENCE ACCOMPLISHED!",
                        color = SystemGold,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { showCompletionReward = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SystemGold),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("CLAIM REWARDS", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = SystemDarkBlue)
                    }
                }
            }
        }
    }
}
