package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerStats

data class Area(
    val name: String,
    val requiredLevel: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String,
    val lore: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapScreen(
    stats: PlayerStats?
) {
    val playerLevel = stats?.level ?: 1
    val scrollState = rememberScrollState()
    var selectedArea by remember { mutableStateOf<Area?>(null) }

    val areas = listOf(
        Area(
            name = "Village Core",
            requiredLevel = 1,
            icon = Icons.Default.Home,
            description = "The beginning of your awakening. Safe haven where normal daily rituals are forged.",
            lore = "\"A simple vanguard's village, yet under its quiet roofs, the seeds of a future warden are planted...\"",
            color = SystemNeonCyan
        ),
        Area(
            name = "Mystic Forest",
            requiredLevel = 3,
            icon = Icons.Default.Forest,
            description = "A dense jungle infested with D-Rank goblins and mana beasts.",
            lore = "\"The rustling leaves hide echoes that move on their own. Be alert, Vanguard!\"",
            color = SystemPurple
        ),
        Area(
            name = "Red Rift Dungeon",
            requiredLevel = 5,
            icon = Icons.Default.Warning,
            description = "A massive dimensional rift packed with aggressive frost elves.",
            lore = "\"A simple rift turning red is a signal of impending calamity. Enter with absolute readiness.\"",
            color = SystemRed
        ),
        Area(
            name = "High Orc Castle",
            requiredLevel = 10,
            icon = Icons.Default.Fort,
            description = "Heavy fortress ruled by the Orc Sovereign, Kargalgan.",
            lore = "\"Within these towering stone walls, hundreds of spell-weaving high orcs guard their majestic lord.\"",
            color = SystemGold
        ),
        Area(
            name = "Demon Tower",
            requiredLevel = 15,
            icon = Icons.Default.Cabin,
            description = "100 levels of ascending demonic trial leading to Baran, Demon Sovereign.",
            lore = "\"Each floor burns with a higher tier of demonic energy. Slay Baran to extract the ultimate lightning core!\"",
            color = SystemPurple
        ),
        Area(
            name = "Echo Realm",
            requiredLevel = 20,
            icon = Icons.Default.BrightnessLow,
            description = "The eternal dark space where extracted echoes are bound and marshaled.",
            lore = "\"A silent landscape of endless black dirt. Here, millions of guardians stand ready to respond to your decree.\"",
            color = SystemPurple
        ),
        Area(
            name = "Ascendant Citadel",
            requiredLevel = 30,
            icon = Icons.Default.EmojiEvents,
            description = "The absolute throne of the Eternal Warden. Overwhelming aura.",
            lore = "\"The final seat of power. Only a true warden who has conquered life and death is permitted to ascend these golden steps.\"",
            color = SystemGold
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemDarkBlue)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SYSTEM REGIONAL MAP",
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 2.sp
        )
        Text(
            text = "Your level dynamically unlocks active dimensions. Scroll horizontally to explore the dimensional rifts.",
            color = TextMutedGrey,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        // Parallax scrollable World Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .clip(RoundedCornerShape(12.dp))
                .background(SystemCardDark)
                .border(1.dp, DarkGreyBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                areas.forEachIndexed { index, area ->
                    val isUnlocked = playerLevel >= area.requiredLevel
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(180.dp)
                            .testTag("map_area_${area.name.replace(" ", "_")}")
                    ) {
                        // Node Visual Connection line indicator (drawn to previous)
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(2.dp)
                                    .background(if (isUnlocked) area.color else DarkGreyBorder)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Circular Map Node
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(if (isUnlocked) SystemDarkBlue else SystemCardDark)
                                .border(
                                    BorderStroke(
                                        2.dp,
                                        if (isUnlocked) area.color else DarkGreyBorder
                                    ),
                                    CircleShape
                                )
                                .clickable { if (isUnlocked) selectedArea = area }
                        ) {
                            Icon(
                                imageVector = if (isUnlocked) area.icon else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isUnlocked) area.color else TextMutedGrey,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Label
                        Text(
                            text = area.name,
                            color = if (isUnlocked) Color.White else TextMutedGrey,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Level Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isUnlocked) area.color.copy(alpha = 0.2f) else DarkGreyBorder,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isUnlocked) "UNLOCKED" else "LV. ${area.requiredLevel}",
                                color = if (isUnlocked) area.color else TextMutedGrey,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Details popup dialog
        selectedArea?.let { area ->
            AlertDialog(
                onDismissRequest = { selectedArea = null },
                containerColor = SystemCardDark,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(area.icon, contentDescription = null, tint = area.color)
                        Text(
                            text = area.name.uppercase(),
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = area.description,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .background(SystemDarkBlue, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = area.lore,
                                color = area.color,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { selectedArea = null }
                    ) {
                        Text(
                            text = "CLOSE INDEX",
                            color = SystemNeonCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                modifier = Modifier.border(1.5.dp, area.color, RoundedCornerShape(28.dp))
            )
        }
    }
}
