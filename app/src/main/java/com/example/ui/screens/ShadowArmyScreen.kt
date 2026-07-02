package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun ShadowArmyScreen(
    shadows: List<ShadowSoldier>
) {
    var selectedShadow by remember { mutableStateOf<ShadowSoldier?>(null) }

    // Total physical power of shadow army
    val totalPower = shadows.sumOf { it.power }

    // Breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "shadow_glow")
    val glowColorAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemDarkBlue)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shadow Overlord Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SystemCardDark)
                .border(
                    BorderStroke(1.5.dp, Brush.linearGradient(listOf(SystemPurple, SystemNeonCyan))),
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storm,
                        contentDescription = null,
                        tint = SystemPurple,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "ECHO GUARDIAN VAULT",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Total Guardian Combat Force: $totalPower | Bound count: ${shadows.size}",
                    color = SystemNeonCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Every habit checked off automatically awakens or levels up an Echo Guardian. Complete your daily commissions to grow your bound forces.",
                    color = TextMutedGrey,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
                )
            }
        }

        if (shadows.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SystemCardDark),
                    border = BorderStroke(1.dp, DarkGreyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AcUnit,
                            contentDescription = "Frozen Core",
                            tint = SystemPurple,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Echo Guardians are Dormant",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You do not currently possess any bound spirits. Complete habits in the Quests Tab to perform your first binding extraction!\n\n\"Awaken...\"",
                            color = TextMutedGrey,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            // Shadows Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1.0f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(shadows) { shadow ->
                    // Set color grades depending on power/evolution
                    val evolutionName = when (shadow.evolutionStage) {
                        4 -> "Eternal Marshal"
                        3 -> "Apex Commander"
                        2 -> "Elite Guardian"
                        else -> "Echo Recruit"
                    }
                    val glowColor = when (shadow.evolutionStage) {
                        4 -> SystemGold
                        3 -> SystemRed
                        2 -> SystemNeonCyan
                        else -> SystemPurple
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedShadow = shadow }
                            .testTag("shadow_card_${shadow.id}"),
                        colors = CardDefaults.cardColors(containerColor = SystemCardDark),
                        border = BorderStroke(1.5.dp, glowColor.copy(alpha = glowColorAlpha))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Icon representing class
                            val (classIcon, classColor) = when (shadow.type) {
                                "Warrior" -> Pair(Icons.Default.Bolt, SystemNeonCyan)
                                "Mage" -> Pair(Icons.Default.AutoAwesome, SystemPurple)
                                "Assassin" -> Pair(Icons.Default.ContentCut, SystemRed)
                                "Tank" -> Pair(Icons.Default.Shield, SystemNeonCyan)
                                else -> Pair(Icons.Default.Support, SystemGold)
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .background(SystemDarkBlue)
                                    .border(1.dp, glowColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = classIcon,
                                    contentDescription = null,
                                    tint = classColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Name
                            Text(
                                text = shadow.name.substringAfter("of "),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = shadow.name.substringBefore(" of"),
                                color = glowColor,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Power Stats
                            Text(
                                text = "Combat Force: ${shadow.power}",
                                color = SystemNeonCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Lv. ${shadow.level} | $evolutionName",
                                color = TextMutedGrey,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = DarkGreyBorder, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(4.dp))

                            // Passive Buff Description
                            val passiveBuff = when (shadow.type) {
                                "Warrior" -> "+5% Habit Strength multipliers"
                                "Mage" -> "+8% Focus Mode focus recovery"
                                "Assassin" -> "+10% Agility speed bonuses"
                                "Tank" -> "+12% Vitality core buffer"
                                else -> "+15% Daily gold yields"
                            }
                            Text(
                                text = "BUFF: $passiveBuff",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }

        selectedShadow?.let { selected ->
            ShadowDetailDialog(
                shadow = selected,
                onDismiss = { selectedShadow = null }
            )
        }
    }
}

@Composable
fun ShadowDetailDialog(
    shadow: ShadowSoldier,
    onDismiss: () -> Unit
) {
    var isResonating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    val infiniteTransition = rememberInfiniteTransition(label = "resonance")
    val resonanceScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCirc),
            repeatMode = RepeatMode.Reverse
        ), label = "res_scale"
    )

    val evolutionName = when (shadow.evolutionStage) {
        4 -> "Eternal Marshal"
        3 -> "Apex Commander"
        2 -> "Elite Guardian"
        else -> "Echo Recruit"
    }

    val baseGlowColor = when (shadow.evolutionStage) {
        4 -> SystemGold
        3 -> SystemRed
        2 -> SystemNeonCyan
        else -> SystemPurple
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header details
                Text(
                    text = "ECHO GUARDIAN INDEX",
                    color = SystemPurple,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                )

                Text(
                    text = shadow.name.substringAfter("of "),
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "[${shadow.name.substringBefore(" of")}]",
                    color = baseGlowColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Divider(color = DarkGreyBorder, thickness = 1.dp)

                // Large class orb
                val (classIcon, classColor) = when (shadow.type) {
                    "Warrior" -> Pair(Icons.Default.Bolt, SystemNeonCyan)
                    "Mage" -> Pair(Icons.Default.AutoAwesome, SystemPurple)
                    "Assassin" -> Pair(Icons.Default.ContentCut, SystemRed)
                    "Tank" -> Pair(Icons.Default.Shield, SystemNeonCyan)
                    else -> Pair(Icons.Default.Support, SystemGold)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .scale(if (isResonating) resonanceScale else 1.0f)
                        .clip(CircleShape)
                        .background(SystemDarkBlue)
                        .border(2.dp, baseGlowColor, CircleShape)
                ) {
                    Icon(
                        imageVector = classIcon,
                        contentDescription = null,
                        tint = classColor,
                        modifier = Modifier.size(45.dp)
                    )
                }

                // Resonance message
                AnimatedVisibility(visible = isResonating) {
                    Text(
                        text = "⚡ Soul Resonance Stable: 100% Strength",
                        color = SystemNeonCyan,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Stats Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Class Type:", color = TextMutedGrey, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        Text(shadow.type, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Army Rank:", color = TextMutedGrey, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        Text(evolutionName, color = baseGlowColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Combat Force:", color = TextMutedGrey, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        Text("${shadow.power}", color = SystemNeonCyan, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Soldier Level:", color = TextMutedGrey, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        Text("Lv. ${shadow.level}", color = SystemGold, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = DarkGreyBorder, thickness = 1.dp)

                // Evolution gauge to next stage
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val nextRankName = when (shadow.evolutionStage) {
                        1 -> "Elite Guardian (Lv. 3)"
                        2 -> "Apex Commander (Lv. 8)"
                        3 -> "Eternal Marshal (Lv. 15)"
                        else -> "Max Evolution"
                    }
                    val progressRatio = when (shadow.evolutionStage) {
                        1 -> shadow.level.toFloat() / 3f
                        2 -> shadow.level.toFloat() / 8f
                        3 -> shadow.level.toFloat() / 15f
                        else -> 1f
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Evo Progress:", color = TextMutedGrey, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        Text(nextRankName, color = SystemGold, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = progressRatio.coerceIn(0f, 1f),
                        color = baseGlowColor,
                        trackColor = DarkGreyBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }

                // Passive Buff
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SystemDarkBlue),
                    border = BorderStroke(1.dp, DarkGreyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
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
                            text = "ACTIVE PASSIVE BUFF:",
                            color = SystemPurple,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = passiveBuff,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isResonating) {
                        isResonating = true
                        coroutineScope.launch {
                            delay(2000)
                            isResonating = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SystemPurple),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("resonate_button")
            ) {
                Text(
                    text = "RE-SUMMON AURA",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, DarkGreyBorder),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("dismiss_inspection_button")
            ) {
                Text(
                    text = "CLOSE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        },
        containerColor = SystemCardDark,
        shape = RoundedCornerShape(12.dp)
    )
}
