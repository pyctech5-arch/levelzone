package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EquipmentItem
import com.example.data.PlayerStats
import com.example.ui.viewmodel.QuestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EquipmentScreen(
    viewModel: QuestViewModel,
    stats: PlayerStats?,
    equipment: List<EquipmentItem>
) {
    var activeTab by remember { mutableStateOf("INVENTORY") } // INVENTORY, STORE, GACHA
    val coroutineScope = rememberCoroutineScope()
    
    // Gacha animations
    var isOpeningChest by remember { mutableStateOf(false) }
    var chestRollResult by remember { mutableStateOf<String?>(null) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "gacha_bounce")
    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bounce"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemDarkBlue)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gold and Stat overview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SystemCardDark)
                .border(1.dp, DarkGreyBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.MonetizationOn, contentDescription = "Gold", tint = SystemGold, modifier = Modifier.size(24.dp))
                Text(
                    text = "GOLD: ${stats?.gold ?: 0}",
                    color = SystemGold,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Text(
                text = "Vanguard Lv. ${stats?.level ?: 1}",
                color = SystemNeonCyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        // Subtabs: INVENTORY | BUY EQUIPMENT | GACHA CHEST
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("INVENTORY", "STORE", "GACHA").forEach { tab ->
                val isSelected = activeTab == tab
                val bgBrush = if (isSelected) {
                    Brush.linearGradient(listOf(SystemNeonCyan, SystemPurple))
                } else {
                    Brush.linearGradient(listOf(SystemCardDark, SystemCardDark))
                }
                
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgBrush)
                        .clickable { activeTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (tab == "GACHA") "GACHA BOX" else tab,
                        color = if (isSelected) SystemDarkBlue else Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Main Display depending on subtab
        when (activeTab) {
            "INVENTORY" -> {
                val ownedItems = equipment.filter { it.isPurchased }
                if (ownedItems.isEmpty()) {
                    Box(modifier = Modifier.weight(1.0f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Your armory is empty. Head over to the Store or Gacha tabs to equip your Vanguard with legendary armaments.",
                            color = TextMutedGrey,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1.0f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ownedItems) { item ->
                            val borderCol = getRarityColor(item.rarity)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleEquipItem(item.id) }
                                    .testTag("inventory_item_${item.id}"),
                                colors = CardDefaults.cardColors(containerColor = SystemCardDark),
                                border = BorderStroke(if (item.isEquipped) 2.dp else 1.dp, if (item.isEquipped) SystemNeonCyan else borderCol)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(45.dp)
                                            .clip(CircleShape)
                                            .background(SystemDarkBlue)
                                            .border(1.dp, borderCol, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = getEquipmentIcon(item.category),
                                            contentDescription = null,
                                            tint = borderCol,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "[${item.rarity}]",
                                        color = borderCol,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+${item.statBonusValue} ${item.statBonusType}",
                                        color = SystemNeonCyan,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (item.isEquipped) SystemNeonCyan else DarkGreyBorder)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (item.isEquipped) "EQUIPPED" else "TAP TO EQUIP",
                                            color = if (item.isEquipped) SystemDarkBlue else TextMutedGrey,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "STORE" -> {
                val storeItems = equipment.filter { !it.isPurchased }
                if (storeItems.isEmpty()) {
                    Box(modifier = Modifier.weight(1.0f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "All available equipment has been purchased. You have reached peak armament limit!",
                            color = TextMutedGrey,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1.0f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(storeItems) { item ->
                            val borderCol = getRarityColor(item.rarity)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("store_item_${item.id}"),
                                colors = CardDefaults.cardColors(containerColor = SystemCardDark),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(SystemDarkBlue)
                                            .border(1.dp, borderCol, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = getEquipmentIcon(item.category),
                                            contentDescription = null,
                                            tint = borderCol,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(
                                        modifier = Modifier.weight(1.0f)
                                    ) {
                                        Text(
                                            text = item.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "${item.rarity} ${item.category} | Granting +${item.statBonusValue} ${item.statBonusType}",
                                            color = TextMutedGrey,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Button(
                                        onClick = { viewModel.purchaseEquipment(item.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = borderCol),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${item.goldCost}G",
                                            fontSize = 11.sp,
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
            }
            "GACHA" -> {
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "THE MONARCH'S MYSTERY VAULT",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Spend 80 Gold to open the high-tier Mystery Chest. High chance of attributes boosters, gold caches, or rare weapons!",
                        color = TextMutedGrey,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(30.dp))

                    // Animated bounce chest icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(140.dp)
                            .scale(if (isOpeningChest) bounceScale else 1.0f)
                            .clickable {
                                if (!isOpeningChest) {
                                    coroutineScope.launch {
                                        isOpeningChest = true
                                        chestRollResult = "Opening high-grade dimensional lock..."
                                        delay(1500)
                                        viewModel.drawGachaChest()
                                        isOpeningChest = false
                                    }
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(listOf(SystemPurple.copy(alpha = 0.5f), Color.Transparent)),
                                    shape = CircleShape
                                )
                        )
                        Icon(
                            imageVector = if (isOpeningChest) Icons.Default.Casino else Icons.Default.CardGiftcard,
                            contentDescription = "Gacha Box",
                            tint = if (isOpeningChest) SystemGold else SystemPurple,
                            modifier = Modifier.size(90.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isOpeningChest = true
                                chestRollResult = "Connecting to the Nexus matrix..."
                                delay(1200)
                                viewModel.drawGachaChest()
                                isOpeningChest = false
                            }
                        },
                        enabled = !isOpeningChest,
                        colors = ButtonDefaults.buttonColors(containerColor = SystemPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("gacha_roll_button")
                    ) {
                        Text(
                            text = "BUY CHEST (COST: 80 GOLD)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

fun getRarityColor(rarity: String): Color {
    return when (rarity.uppercase()) {
        "COMMON" -> Color(0xFF9CA3AF)
        "RARE" -> SystemNeonCyan
        "EPIC" -> SystemPurple
        "LEGENDARY" -> SystemGold
        "MYTHIC" -> SystemRed
        else -> Color.White
    }
}

fun getEquipmentIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category.uppercase()) {
        "WEAPON" -> Icons.Default.Colorize
        "ARMOR" -> Icons.Default.Shield
        "ACCESSORY" -> Icons.Default.BrightnessLow
        "ARTIFACT" -> Icons.Default.Key
        "BOOK" -> Icons.Default.MenuBook
        "POTION" -> Icons.Default.LocalCafe
        else -> Icons.Default.FitnessCenter
    }
}
