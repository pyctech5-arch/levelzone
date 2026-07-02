package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyLoginState
import com.example.data.DungeonState
import com.example.data.EquipmentItem
import com.example.data.PlayerAchievement
import com.example.data.PlayerStats
import com.example.data.ShadowSoldier
import com.example.ui.viewmodel.QuestViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemHubScreen(
    viewModel: QuestViewModel,
    stats: PlayerStats?,
    shadows: List<ShadowSoldier>,
    equipment: List<EquipmentItem>,
    dungeons: List<DungeonState>,
    loginState: DailyLoginState?,
    achievements: List<PlayerAchievement>
) {
    var activeSubMode by remember { mutableStateOf<String?>(null) }
    
    // Simple state-driven routing for sub-modes
    if (activeSubMode != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (activeSubMode) {
                "AI_QUEST" -> AiQuestGeneratorScreen(viewModel = viewModel, stats = stats)
                "DUNGEON" -> DungeonsAndBossesScreen(viewModel = viewModel, dungeons = dungeons)
                "SHADOW" -> ShadowArmyScreen(shadows = shadows)
                "EQUIPMENT" -> EquipmentScreen(viewModel = viewModel, stats = stats, equipment = equipment)
                "MAP" -> WorldMapScreen(stats = stats)
                "FOCUS" -> FocusModeScreen(viewModel = viewModel)
            }
            
            // Shared Floating close button on sub-screens
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = { activeSubMode = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SystemNeonCyan),
                    elevation = ButtonDefaults.buttonElevation(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("close_submode_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SystemDarkBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CLOSE SYSTEM TAB", color = SystemDarkBlue, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        // --- PRIMARY SYSTEM HUB HOMEPAGE ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SystemDarkBlue)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Dynamic Weather & Title Card
            item {
                DynamicWeatherPanel()
            }

            // 2. AI Daily Coach & Continuous Story Mode Console
            item {
                SystemStoryConsole(stats)
            }

            // 3. Main RPG Features Navigation Grid
            item {
                Text(
                    text = "SYSTEM APPLICATIONS",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // App Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HubMenuCard("AI Quest Gen", Icons.Default.AutoAwesome, Modifier.weight(1f)) {
                            activeSubMode = "AI_QUEST"
                        }
                        HubMenuCard("Dungeon Gates", Icons.Default.Warning, Modifier.weight(1f)) {
                            activeSubMode = "DUNGEON"
                        }
                    }
                    // App Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HubMenuCard("Shadow Army", Icons.Default.Storm, Modifier.weight(1f)) {
                            activeSubMode = "SHADOW"
                        }
                        HubMenuCard("Armory Store", Icons.Default.Shield, Modifier.weight(1f)) {
                            activeSubMode = "EQUIPMENT"
                        }
                    }
                    // App Row 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HubMenuCard("Regional Map", Icons.Default.Map, Modifier.weight(1f)) {
                            activeSubMode = "MAP"
                        }
                        HubMenuCard("Dilation Focus", Icons.Default.HourglassTop, Modifier.weight(1f)) {
                            activeSubMode = "FOCUS"
                        }
                    }
                }
            }

            // 4. Daily Login Rewards Rowclaim
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAILY SYSTEM ALIGNMENT CLAIM",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                    
                    Text(
                        text = "Day ${loginState?.consecutiveDays ?: 0}/7",
                        color = SystemGold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.claimDailyLoginReward() }
                        .testTag("daily_login_claim_card"),
                    colors = CardDefaults.cardColors(containerColor = SystemCardDark),
                    border = BorderStroke(1.5.dp, SystemGold)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = SystemGold, modifier = Modifier.size(32.dp))
                            Column {
                                Text(
                                    text = "ALIGN CORES WITH SYSTEM",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Tap to claim your daily dimensional chest.",
                                    color = TextMutedGrey,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SystemGold)
                    }
                }
            }

            // 5. Achievement Gallery List
            item {
                Text(
                    text = "ACHIEVEMENT HALL OF HUNTERS",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
            
            items(achievements) { ach ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("achievement_card_${ach.id}"),
                    colors = CardDefaults.cardColors(containerColor = SystemCardDark),
                    border = BorderStroke(1.dp, if (ach.isUnlocked) SystemGold else DarkGreyBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (ach.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (ach.isUnlocked) SystemGold else TextMutedGrey,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(
                                text = ach.title,
                                color = if (ach.isUnlocked) SystemGold else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                            Text(
                                text = ach.description,
                                color = TextMutedGrey,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = ach.progress.toFloat() / ach.maxProgress,
                                color = if (ach.isUnlocked) SystemGold else SystemPurple,
                                trackColor = DarkGreyBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Progress: ${ach.progress} / ${ach.maxProgress}",
                                color = if (ach.isUnlocked) SystemGold else SystemPurple,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 6. Prestige Reawakening Card at level 15+
            if ((stats?.level ?: 1) >= 15) {
                item {
                    Text(
                        text = "SYSTEM PRESTIGE GATEWAYS",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.reawakenPrestige() }
                            .testTag("prestige_reawakening_card"),
                        colors = CardDefaults.cardColors(containerColor = SystemCardDark),
                        border = BorderStroke(2.dp, SystemPurple)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BrightnessLow, contentDescription = null, tint = SystemPurple, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "NEXUS REAWAKENING RIFT",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "You have surpassed Vanguard Level 15! Tap to enter the Prestige rift. Reset your level to 1, but permanently boost baseline stat points and unlock multiplier buffers.",
                                color = TextMutedGrey,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HubMenuCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(74.dp)
            .clickable { onClick() }
            .testTag("hub_app_${label.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = SystemCardDark),
        border = BorderStroke(1.dp, DarkGreyBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SystemDarkBlue)
                    .border(1.dp, SystemNeonCyan, CircleShape)
            ) {
                Icon(icon, contentDescription = null, tint = SystemNeonCyan, modifier = Modifier.size(18.dp))
            }
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}

// 1. Dynamic Weather Indicator
@Composable
fun DynamicWeatherPanel() {
    var manualWeatherOverride by remember { mutableStateOf<String?>(null) }
    
    // Auto detect local hour
    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    
    val baseWeather = when (hour) {
        in 5..11 -> "Morning Brightness"
        in 12..17 -> "Symmetrical Solar Apex"
        in 18..20 -> "Sunset Glow"
        else -> "Midnight Abyssal Aura"
    }

    val activeWeather = manualWeatherOverride ?: baseWeather
    
    val gradientColors = when (activeWeather) {
        "Morning Brightness" -> listOf(Color(0xFF0F172A), Color(0xFF0369A1))
        "Symmetrical Solar Apex" -> listOf(Color(0xFF020617), Color(0xFF0F766E))
        "Sunset Glow" -> listOf(Color(0xFF020617), Color(0xFF7C2D12))
        "Purple Storm Aura" -> listOf(Color(0xFF020617), Color(0xFF581C87))
        "Eclipse Calamity" -> listOf(Color(0xFF030712), Color(0xFF020617))
        else -> listOf(Color(0xFF030712), Color(0xFF0F172A))
    }

    val icon = when (activeWeather) {
        "Morning Brightness" -> Icons.Default.LightMode
        "Symmetrical Solar Apex" -> Icons.Default.WbSunny
        "Sunset Glow" -> Icons.Default.BrightnessMedium
        "Purple Storm Aura" -> Icons.Default.Thunderstorm
        "Eclipse Calamity" -> Icons.Default.DarkMode
        else -> Icons.Default.NightsStay
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(gradientColors))
            .border(1.dp, DarkGreyBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "THE SYSTEM LABS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Weather: $activeWeather",
                        color = SystemNeonCyan,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SystemNeonCyan,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            // Manual Simulation toggle bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SIMULATE:",
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                
                listOf("Purple Storm Aura", "Eclipse Calamity").forEach { override ->
                    val isOverrideActive = manualWeatherOverride == override
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isOverrideActive) SystemNeonCyan else DarkGreyBorder)
                            .clickable {
                                manualWeatherOverride = if (isOverrideActive) null else override
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (override.contains("Storm")) "STORM" else "ECLIPSE",
                            color = if (isOverrideActive) SystemDarkBlue else Color.White,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// 2. Story Progress Console
@Composable
fun SystemStoryConsole(stats: PlayerStats?) {
    val level = stats?.level ?: 1
    
    val storyLine = when {
        level >= 30 -> "ALERT: [THE WARDEN ASCENSION] Master consciousness achieved. All dimensional rifts stand open and absolute. Proceed to reign."
        level >= 20 -> "ALERT: [THE SPECTRAL REALM COMPILING] The echo Warden space has fully unified with your mortal timeline. Every habit is bound eternally."
        level >= 15 -> "ALERT: [DEMON CITADEL RIFTS AWAKENED] The Baran thunder core is vibrating in your vicinity. Prepare your stats for extreme ascension."
        level >= 10 -> "ALERT: [HIGH KEEPER CASTLE DETECTED] Heavy-energy spellcasters are monitoring your productivity metrics. Strengthen your defense index."
        level >= 5 -> "ALERT: [RED RIFT ACCELERATION] The Nexus has unlocked abyssal red dungeons. Complete daily quest calibrations to deal heavy damage."
        level >= 3 -> "ALERT: [MYSTIC FOREST UNLOCKED] Minor mana beast signals captured. The Nexus has authorized echo extraction upon completed habits."
        else -> "ALERT: [INITIAL NEXUS CONTACT ESTABLISHED] \"Welcome, Prashant. The Nexus has recognized your potential. Your awakening sequence is complete.\""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SystemCardDark),
        border = StrokeBorder(1.dp, SystemPurple)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(8.dp).background(SystemPurple, CircleShape))
                Text(
                    text = "THE COLD CALIBRATOR LOGS",
                    color = SystemPurple,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = storyLine,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}

// Border Stroke Helper for Card Design
@Composable
fun StrokeBorder(width: androidx.compose.ui.unit.Dp, color: Color) = BorderStroke(width, color)
