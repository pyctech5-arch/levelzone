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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DungeonState
import com.example.ui.viewmodel.QuestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DungeonsAndBossesScreen(
    viewModel: QuestViewModel,
    dungeons: List<DungeonState>
) {
    var selectedDungeon by remember { mutableStateOf<DungeonState?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    // Portal rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "portal_spin")
    val portalRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "portal_rotation"
    )

    if (selectedDungeon == null) {
        // --- PORTAL GATEWAY LIST SCREEN ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SystemDarkBlue)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "DIMENSIONAL GATEWAYS",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Active C-Rank to S-Rank Red Gates detected. Complete daily habits to strike down these dungeon overlords.",
                    color = TextMutedGrey,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            items(dungeons) { dungeon ->
                val colorGradient = when (dungeon.theme) {
                    "Morning" -> listOf(SystemNeonCyan, SystemPurple)
                    "Study" -> listOf(SystemPurple, SystemNeonCyan)
                    "Fitness" -> listOf(SystemGold, SystemPurple)
                    else -> listOf(SystemRed, SystemPurple)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedDungeon = dungeon }
                        .testTag("dungeon_card_${dungeon.id}"),
                    colors = CardDefaults.cardColors(containerColor = SystemCardDark),
                    border = BorderStroke(1.5.dp, Brush.linearGradient(colorGradient))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Portal Visual Animation
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(SystemDarkBlue)
                                .border(1.dp, SystemNeonCyan, CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .rotate(portalRotation)
                                    .background(
                                        Brush.radialGradient(
                                            colors = if (dungeon.isCleared) listOf(SystemGold, Color.Transparent)
                                            else listOf(SystemPurple, SystemNeonCyan, Color.Transparent)
                                        ),
                                        shape = CircleShape
                                    )
                            )
                            Icon(
                                imageVector = if (dungeon.isCleared) Icons.Default.LockOpen else Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (dungeon.isCleared) SystemGold else SystemNeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1.0f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${dungeon.theme.uppercase()} GATE",
                                    color = if (dungeon.isCleared) SystemGold else SystemNeonCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                if (dungeon.isCleared) {
                                    Text(
                                        text = "CLEARED",
                                        color = SystemGold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Text(
                                text = dungeon.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Progress bar
                            LinearProgressIndicator(
                                progress = (dungeon.bossMaxHp - dungeon.bossHp).toFloat() / dungeon.bossMaxHp,
                                color = if (dungeon.isCleared) SystemGold else SystemNeonCyan,
                                trackColor = DarkGreyBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Boss: ${dungeon.bossName} | HP: ${dungeon.bossHp}/${dungeon.bossMaxHp}",
                                color = TextMutedGrey,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    } else {
        // --- ACTIVE DUNGEON BATTLE VIEW ---
        val dungeon = dungeons.find { it.id == selectedDungeon?.id } ?: selectedDungeon!!
        var dmgPopupActive by remember { mutableStateOf(false) }
        var dmgValuePopup by remember { mutableStateOf(0) }
        var isCritPopup by remember { mutableStateOf(false) }
        var isShaking by remember { mutableStateOf(false) }
        var showChestReward by remember { mutableStateOf(false) }

        val shakeOffset = remember { Animatable(0f) }

        // Objectives checklist based on theme
        val objectives = when (dungeon.theme) {
            "Morning" -> listOf("Complete Wake Up Ritual", "Perform Stretching", "Hydrate + Drink Water", "Morning Journal")
            "Study" -> listOf("Study Core Subject (45m)", "Review Memory Cards", "Draft Notes")
            "Fitness" -> listOf("Warm-up Stretching", "Target Gym/Home Workout", "Post-Workout Hydration")
            else -> listOf("Clear Daily Priority Tasks", "Review Weekly Goals", "Zero Inbox Calibration")
        }

        // Handle hit boss animation
        val hitBoss: (Boolean) -> Unit = { isCrit ->
            coroutineScope.launch {
                val dmg = if (isCrit) (45..70).random() else (20..35).random()
                dmgValuePopup = dmg
                isCritPopup = isCrit
                dmgPopupActive = true
                isShaking = true
                
                // Camera shake effect
                launch {
                    repeat(6) {
                        shakeOffset.animateTo(12f, tween(30, easing = LinearEasing))
                        shakeOffset.animateTo(-12f, tween(30, easing = LinearEasing))
                    }
                    shakeOffset.animateTo(0f, tween(30))
                    isShaking = false
                }
                
                viewModel.dealDamageToDungeonBossDirect(dungeon.id, dmg)
                
                // If boss defeated, trigger clear chest reward screen
                if (dungeon.bossHp - dmg <= 0) {
                    showChestReward = true
                }
                
                delay(1200)
                dmgPopupActive = false
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SystemDarkBlue)
                .offset(x = shakeOffset.value.dp)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { selectedDungeon = null }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SystemNeonCyan)
                }
                Text(
                    text = "ABYSSAL GATE: ENTERED",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            // Animated Boss Figure Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SystemCardDark)
                    .border(
                        BorderStroke(2.dp, if (dungeon.isBossDefeated) SystemGold else SystemRed),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (dungeon.isBossDefeated) Icons.Default.EmojiEvents else Icons.Default.Warning,
                        contentDescription = "Boss Aura",
                        tint = if (dungeon.isBossDefeated) SystemGold else SystemRed,
                        modifier = Modifier
                            .size(70.dp)
                            .scale(if (isShaking) 1.3f else 1.0f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = dungeon.bossName.uppercase(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Boss HP bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = dungeon.bossHp.toFloat() / dungeon.bossMaxHp,
                            color = SystemRed,
                            trackColor = DarkGreyBorder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "BOSS HP: ${dungeon.bossHp} / ${dungeon.bossMaxHp}",
                            color = SystemRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Floating Damage Numbers (simple direct condition)
                if (dmgPopupActive) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isCritPopup) "🔥 CRITICAL HIT! -${dmgValuePopup} HP" else "⚔️ DAMAGE -${dmgValuePopup} HP",
                            color = if (isCritPopup) SystemGold else SystemRed,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Dungeon Objectives Checklist
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SystemCardDark),
                border = BorderStroke(1.dp, DarkGreyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DUNGEON OBJECTIVES (CLEAR TO SEAL GATE):",
                        color = SystemNeonCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    objectives.forEach { objective ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (dungeon.isCleared) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (dungeon.isCleared) SystemGold else SystemPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = objective,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Active Battle Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "BATTLE ACTION LOG:",
                    color = TextMutedGrey,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { hitBoss(false) },
                        enabled = !dungeon.isBossDefeated,
                        colors = ButtonDefaults.buttonColors(containerColor = SystemPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.0f)
                            .height(48.dp)
                            .testTag("basic_strike_button")
                    ) {
                        Text("BASIC STRIKE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { hitBoss(true) },
                        enabled = !dungeon.isBossDefeated,
                        colors = ButtonDefaults.buttonColors(containerColor = SystemRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.0f)
                            .height(48.dp)
                            .testTag("critical_strike_button")
                    ) {
                        Text("CRIT STRIKE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Reward chest animation block
            if (dungeon.isBossDefeated) {
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
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Chest",
                            tint = SystemGold,
                            modifier = Modifier
                                .size(50.dp)
                                .scale(1.1f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DUNGEON CLEAR CHEST UNLOCKED!",
                            color = SystemGold,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Claimed: +${dungeon.goldReward} Gold & +${dungeon.xpReward} XP. Active buffs are permanently bound to your soul core.",
                            color = TextMutedGrey,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
