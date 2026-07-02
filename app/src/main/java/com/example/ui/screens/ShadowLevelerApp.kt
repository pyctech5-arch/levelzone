package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import com.example.R
import com.example.data.Habit
import com.example.data.LevelUpResult
import com.example.data.PlayerStats
import com.example.data.ShopItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShadowLevelerApp(
    viewModel: QuestViewModel = viewModel()
) {
    val habits by viewModel.habits.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val playerStats by viewModel.playerStats.collectAsState()
    val shopItems by viewModel.shopItems.collectAsState()

    // New RPG state flow collections
    val shadows by viewModel.shadows.collectAsState()
    val equipment by viewModel.equipment.collectAsState()
    val dungeons by viewModel.dungeons.collectAsState()
    val loginState by viewModel.loginState.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    val showQuestSystemNotice by viewModel.showQuestSystemNotice.collectAsState()
    val activeLevelUp by viewModel.activeLevelUpResult.collectAsState()
    val activeShadowBinding by viewModel.activeShadowBinding.collectAsState()
    val sysMessage by viewModel.sysMessage.collectAsState()

    var activeTab by remember { mutableStateOf("QUESTS") } // QUESTS, STATS, SHOP
    var showCreateQuestDialog by remember { mutableStateOf(false) }
    var showCreateRewardDialog by remember { mutableStateOf(false) }

    // Coroutine scope for animations
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = SystemDarkBlue,
        bottomBar = {
            SystemNavigationBar(
                activeTab = activeTab,
                onTabSelect = { activeTab = it }
            )
        },
        floatingActionButton = {
            if (activeTab == "QUESTS") {
                FloatingActionButton(
                    onClick = { showCreateQuestDialog = true },
                    containerColor = SystemNeonCyan,
                    contentColor = SystemDarkBlue,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Quest",
                        modifier = Modifier.size(28.dp)
                    )
                }
            } else if (activeTab == "SHOP") {
                FloatingActionButton(
                    onClick = { showCreateRewardDialog = true },
                    containerColor = SystemGold,
                    contentColor = SystemDarkBlue,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCard,
                        contentDescription = "Forge Reward",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main content layout depending on Tab
            when (activeTab) {
                "QUESTS" -> QuestsTabScreen(
                    habits = habits,
                    stats = playerStats,
                    onComplete = { viewModel.completeQuest(it) },
                    onDelete = { viewModel.deleteQuest(it) },
                    onTriggerReset = { viewModel.forceMidnightReset() },
                    onLoadDefaultCsQuests = { viewModel.loadDefaultCsQuests() },
                    onEdit = { viewModel.startEditingQuest(it) }
                )
                "STATS" -> PlayerStatsTabScreen(
                    stats = playerStats,
                    logs = logs,
                    habits = habits
                )
                "SKILLS" -> SkillTreeTabScreen(
                    stats = playerStats,
                    onAllocate = { viewModel.allocateStatPoint(it) }
                )
                "SHOP" -> RestShopTabScreen(
                    shopItems = shopItems,
                    stats = playerStats,
                    onBuy = { viewModel.purchaseReward(it) },
                    onDelete = { viewModel.deleteReward(it) }
                )
                "HUB" -> SystemHubScreen(
                    viewModel = viewModel,
                    stats = playerStats,
                    shadows = shadows,
                    equipment = equipment,
                    dungeons = dungeons,
                    loginState = loginState,
                    achievements = achievements
                )
            }

            // --- SYSTEM TOAST-LIKE BANNER OVERLAY ---
            SystemBannerMessage(
                message = sysMessage,
                onDismiss = { viewModel.clearSystemAlert() }
            )

            // --- INTRO / DAILY QUEST ASSIGNED SYSTEM ALARM MODAL ---
            if (showQuestSystemNotice) {
                DailyQuestNoticeModal(
                    onAccept = { viewModel.closeQuestNotice() }
                )
            }

            // --- LEVEL UP OVERLAY DIALOG ---
            activeLevelUp?.let { result ->
                LevelUpDialog(
                    result = result,
                    onDismiss = { viewModel.clearLevelUp() }
                )
            }

            // --- SHADOW BINDING OVERLAY ---
            activeShadowBinding?.let { shadow ->
                ShadowBindingOverlay(
                    shadow = shadow,
                    onDismiss = { viewModel.clearShadowBinding() }
                )
            }

            // --- CREATE HABIT (QUEST) DIALOG ---
            if (showCreateQuestDialog) {
                CreateQuestDialog(
                    viewModel = viewModel,
                    onDismiss = { showCreateQuestDialog = false },
                    onConfirm = {
                        viewModel.addQuest()
                        showCreateQuestDialog = false
                    }
                )
            }

            // --- EDIT HABIT (QUEST) DIALOG ---
            val editingHabit by viewModel.editingHabit.collectAsState()
            if (editingHabit != null) {
                EditQuestDialog(
                    viewModel = viewModel,
                    onDismiss = { viewModel.cancelEditingQuest() },
                    onConfirm = { viewModel.saveEditedQuest() }
                )
            }

            // --- CREATE SHOP REWARD DIALOG ---
            if (showCreateRewardDialog) {
                CreateRewardDialog(
                    viewModel = viewModel,
                    onDismiss = { showCreateRewardDialog = false },
                    onConfirm = { title, desc, cost, icon ->
                        viewModel.createCustomReward(title, desc, cost, icon)
                        showCreateRewardDialog = false
                    }
                )
            }
        }
    }
}

// ==================== NAVIGATION BAR ====================
@Composable
fun SystemNavigationBar(
    activeTab: String,
    onTabSelect: (String) -> Unit
) {
    NavigationBar(
        containerColor = SystemCardDark,
        tonalElevation = 8.dp,
        modifier = Modifier
            .border(1.dp, DarkGreyBorder)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = activeTab == "QUESTS",
            onClick = { onTabSelect("QUESTS") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SystemDarkBlue,
                selectedTextColor = SystemNeonCyan,
                indicatorColor = SystemNeonCyan,
                unselectedIconColor = TextMutedGrey,
                unselectedTextColor = TextMutedGrey
            ),
            icon = { Icon(Icons.Default.Task, contentDescription = "Active Quests") },
            label = { Text("Quests", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
        )
        NavigationBarItem(
            selected = activeTab == "STATS",
            onClick = { onTabSelect("STATS") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SystemDarkBlue,
                selectedTextColor = SystemNeonCyan,
                indicatorColor = SystemNeonCyan,
                unselectedIconColor = TextMutedGrey,
                unselectedTextColor = TextMutedGrey
            ),
            icon = { Icon(Icons.Default.Person, contentDescription = "Player Stats") },
            label = { Text("Vanguard Profile", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
        )
        NavigationBarItem(
            selected = activeTab == "SKILLS",
            onClick = { onTabSelect("SKILLS") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SystemDarkBlue,
                selectedTextColor = SystemPurple,
                indicatorColor = SystemPurple,
                unselectedIconColor = TextMutedGrey,
                unselectedTextColor = TextMutedGrey
            ),
            icon = { Icon(Icons.Default.AccountTree, contentDescription = "Skill Tree") },
            label = { Text("Skill Tree", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
        )
        NavigationBarItem(
            selected = activeTab == "SHOP",
            onClick = { onTabSelect("SHOP") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SystemDarkBlue,
                selectedTextColor = SystemGold,
                indicatorColor = SystemGold,
                unselectedIconColor = TextMutedGrey,
                unselectedTextColor = TextMutedGrey
            ),
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Rest Shop") },
            label = { Text("Rest Shop", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
        )
        NavigationBarItem(
            selected = activeTab == "HUB",
            onClick = { onTabSelect("HUB") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SystemDarkBlue,
                selectedTextColor = SystemPurple,
                indicatorColor = SystemPurple,
                unselectedIconColor = TextMutedGrey,
                unselectedTextColor = TextMutedGrey
            ),
            icon = { Icon(Icons.Default.Storm, contentDescription = "System Hub") },
            label = { Text("System Hub", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
        )
    }
}

// ==================== TAB 1: QUESTS LIST SCRREN ====================
@Composable
fun QuestsTabScreen(
    habits: List<Habit>,
    stats: PlayerStats?,
    onComplete: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
    onTriggerReset: () -> Unit,
    onLoadDefaultCsQuests: () -> Unit,
    onEdit: (Habit) -> Unit
) {
    val listState = rememberLazyListState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemDarkBlue)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // --- IMMERSIVE HUD PLAYER CARD ---
        stats?.let {
            HoloHUDHeader(
                stats = it,
                onTriggerReset = onTriggerReset
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "▶ SYSTEM ACTIVE QUESTS (${habits.size})",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SystemNeonCyan,
                letterSpacing = 1.sp
            )
            
            Text(
                text = "[ SYNC CS QUESTS ]",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = SystemNeonCyan,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onLoadDefaultCsQuests() }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, DarkGreyBorder, RoundedCornerShape(12.dp))
                    .background(SystemCardDark)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QueryStats,
                        contentDescription = "Empty",
                        tint = TextMutedGrey,
                        modifier = Modifier
                            .size(64.dp)
                            .alpha(0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NO ACTIVE QUESTS DETECTED",
                        fontFamily = FontFamily.Monospace,
                        color = TextCrispWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap the [+] button to summon your first Rank classification quest, analyze complexity, and begin extraction.",
                        fontFamily = FontFamily.SansSerif,
                        color = TextMutedGrey,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onLoadDefaultCsQuests,
                        colors = ButtonDefaults.buttonColors(containerColor = SystemNeonCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = SystemDarkBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SYNC CS DAILY QUESTS",
                            color = SystemDarkBlue,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            // --- SCROLL EFFECT LAZY COLUMN ---
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(habits, key = { _, h -> h.id }) { index, habit ->
                    // Calculate visual dynamic translate-and-scale effect based on scrolling
                    // Highly responsive parallax feel
                    val isFirstItem = index == 0
                    val isLastItem = index == habits.lastIndex
                    
                    // Simple, stable scroll offsets
                    val firstVisible = listState.firstVisibleItemIndex
                    val offset = listState.firstVisibleItemScrollOffset
                    
                    val scaleFactor = if (index < firstVisible) {
                        0.93f
                    } else if (index == firstVisible) {
                        // Blend down scale
                        1f - (offset.toFloat() / 5000f).coerceIn(0f, 0.08f)
                    } else {
                        1.0f
                    }

                    QuestCardItem(
                        habit = habit,
                        scaleFactor = scaleFactor,
                        onComplete = onComplete,
                        onDelete = onDelete,
                        onEdit = onEdit
                    )
                }
            }
        }
    }
}

@Composable
fun HoloHUDHeader(
    stats: PlayerStats,
    onTriggerReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(SystemNeonCyan, SystemPurple))),
                shape = RoundedCornerShape(12.dp)
            )
            .background(SystemCardDark)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "STATUS WINDOW",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = SystemPurple,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "HUNTER LEVEL ${stats.level}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = TextCrispWhite
                )
            }

            // Reset mechanism styled styled like system config
            IconButton(
                onClick = onTriggerReset,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SystemDarkBlue)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Simulate Reset",
                    tint = SystemNeonCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // XP Bar System Slider style
        Text(
            text = "SYSTEM EXP PROGRESS: ${stats.xp} / ${stats.xpNeeded}",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = SystemNeonCyan
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SystemDarkBlue)
                .border(0.5.dp, DarkGreyBorder, RoundedCornerShape(4.dp))
        ) {
            val progressWidth = (stats.xp.toFloat() / stats.xpNeeded.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressWidth)
                    .background(
                        Brush.horizontalGradient(
                            listOf(SystemNeonBlue, SystemNeonCyan)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Title",
                    tint = SystemGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stats.title,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = SystemGold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = "Gold Balance",
                    tint = SystemGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${stats.gold} Gold Tokens",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SystemGold
                )
            }
        }
    }
}

@Composable
fun QuestCardItem(
    habit: Habit,
    scaleFactor: Float,
    onComplete: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
    onEdit: (Habit) -> Unit
) {
    val rankColor = when (habit.rank) {
        "S" -> ColorRankS
        "A" -> ColorRankA
        "B" -> ColorRankB
        "C" -> ColorRankC
        "D" -> ColorRankD
        else -> ColorRankE
    }

    val categoryIcon = when (habit.associatedStat) {
        "STRENGTH" -> Icons.Default.FitnessCenter
        "AGILITY" -> Icons.Default.DirectionsRun
        "SENSE" -> Icons.Default.SelfImprovement
        "VITALITY" -> Icons.Default.Favorite
        "INTELLIGENCE" -> Icons.Default.Psychology
        else -> Icons.Default.SportsMartialArts
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scaleFactor)
            .border(
                1.dp,
                if (habit.isCompletedToday) SystemNeonCyan.copy(alpha = 0.5f) else DarkGreyBorder,
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday) SystemCardDark.copy(alpha = 0.62f) else SystemCardDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(rankColor.copy(alpha = 0.15f))
                    .border(1.5.dp, rankColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = habit.rank,
                    color = rankColor,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = habit.title,
                        fontWeight = FontWeight.Bold,
                        color = if (habit.isCompletedToday) TextCrispWhite.copy(alpha = 0.5f) else TextCrispWhite,
                        fontSize = 15.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = habit.associatedStat,
                        tint = SystemNeonCyan,
                        modifier = Modifier
                            .size(14.dp)
                            .alpha(0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                val displayDesc = remember(habit.description, habit.createdAt) {
                    if (habit.title.contains("pushup", ignoreCase = true)) {
                        val weeks = ((System.currentTimeMillis() - habit.createdAt) / (7 * 24 * 60 * 60 * 1000L)).toInt()
                        val target = 10 + (weeks * 10)
                        "Perform your daily set of pushups. Target: $target pushups today! (+10 per week progressive overload)"
                    } else {
                        habit.description.ifEmpty { "Dynamic Rank Quest assigned by system." }
                    }
                }

                Text(
                    text = displayDesc,
                    color = TextMutedGrey,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SystemPurple.copy(alpha = 0.15f))
                            .border(0.5.dp, SystemPurple.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = habit.category.uppercase(),
                            color = SystemPurple,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (habit.streak > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SystemDarkBlue)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "Streak",
                                tint = SystemGold,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${habit.streak} DAY STREAK",
                                color = SystemGold,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    onClick = { onComplete(habit) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (habit.isCompletedToday) SystemNeonCyan else SystemDarkBlue)
                        .border(
                            1.dp,
                            if (habit.isCompletedToday) SystemNeonCyan else SystemNeonCyan.copy(alpha = 0.5f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (habit.isCompletedToday) Icons.Default.Check else Icons.Default.Bolt,
                        contentDescription = "Select Quest",
                        tint = if (habit.isCompletedToday) SystemDarkBlue else SystemNeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit Option
                    Text(
                        text = "EDIT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = SystemNeonCyan.copy(alpha = 0.8f),
                        modifier = Modifier
                            .clickable { onEdit(habit) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Delete Option
                    Text(
                        text = "DELETE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = Color.Red.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clickable { onDelete(habit) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}


// ==================== TAB 2: HUNTER PROFILE / STATS SCREEN ====================
@Composable
fun PlayerStatsTabScreen(
    stats: PlayerStats?,
    logs: List<com.example.data.QuestLog>,
    habits: List<Habit>
) {
    var activeInfoStat by remember { mutableStateOf<String?>(null) }
    val detailsScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemDarkBlue)
            .verticalScroll(detailsScrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "▶ VANGUARD NEXUS STATUS PROFILE",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = SystemNeonCyan,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (stats == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SystemNeonCyan)
            }
        } else {
            // Profile Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SystemPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .background(SystemCardDark)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Generated Dynamic Avatar Image!
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, SystemNeonCyan, RoundedCornerShape(8.dp))
                        .background(SystemDarkBlue)
                ) {
                    // Load the generated dynamic avatar image from drawable resource
                    Image(
                        painter = painterResource(id = R.drawable.img_shadow_avatar_1781669347207),
                        contentDescription = "Warden Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Tiny Rank overlay tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(SystemNeonCyan)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "S-RANK",
                            color = SystemDarkBlue,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "PRASHANT (PLAYER)",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = TextCrispWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = stats.title,
                        fontFamily = FontFamily.Monospace,
                        color = SystemGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current Class Rank is automatically indexed by completing daily assigned gates.",
                        color = TextMutedGrey,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            RadarChartContainer(
                stats = stats,
                habits = habits,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // STATS BREAKDOWN GRID
            Text(
                text = "▶ PLAYER NUMERICAL ATTRIBUTES",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = SystemPurple,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stat list
            StatBarIndicator("STRENGTH [STR]", stats.strength, SystemNeonCyan) {
                activeInfoStat = "STRENGTH: Gained through Physical Quests (Workout/Gym). Controls task completion intensity."
            }
            StatBarIndicator("AGILITY [AGI]", stats.agility, SystemNeonBlue) {
                activeInfoStat = "AGILITY: Gained through Cardio Quests (Running/Exercise). Enhances scheduling flexibility."
            }
            StatBarIndicator("SENSE [SEN]", stats.sense, SystemPurple) {
                activeInfoStat = "SENSE: Gained through Mindfulness Quests (Meditation/Hydration). Elevates stress resistance."
            }
            StatBarIndicator("VITALITY [VIT]", stats.vitality, ColorRankD) {
                activeInfoStat = "VITALITY: Gained through Wellness Quests (Sleep/Hygiene). Enhances recovery speeds."
            }
            StatBarIndicator("INTELLIGENCE [INT]", stats.intelligence, SystemGold) {
                activeInfoStat = "INTELLIGENCE: Gained through Cognitive Quests (Study/Coding). Multiplies reward tokens earned."
            }

            // Stat info description popups
            activeInfoStat?.let { desc ->
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SystemPurple.copy(alpha = 0.1f))
                        .border(1.dp, SystemPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = SystemPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = desc,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextCrispWhite,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { activeInfoStat = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextCrispWhite,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // LOGS / COMPLETED HISTORY LOG
            Text(
                text = "▶ SYSTEM ACTIVITY FEED (CONQUERED GATES)",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = SystemNeonCyan,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkGreyBorder, RoundedCornerShape(8.dp))
                        .background(SystemCardDark)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO CONQUERED GATES LOGGED IN CORE DATABASE.",
                        fontFamily = FontFamily.Monospace,
                        color = TextMutedGrey,
                        fontSize = 11.sp
                    )
                }
            } else {
                logs.take(15).forEach { log ->
                    ActivityFeedRow(log = log)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
fun StatBarIndicator(
    statName: String,
    statValue: Int,
    fillColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = statName,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextCrispWhite
            )
            Text(
                text = "$statValue",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = fillColor
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SystemCardDark)
                .border(0.5.dp, DarkGreyBorder, RoundedCornerShape(4.dp))
        ) {
            // Animate width progression safely
            val percentage = (statValue.toFloat() / 100f).coerceIn(0.1f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage)
                    .background(
                        Brush.horizontalGradient(
                            listOf(fillColor.copy(alpha = 0.5f), fillColor)
                        )
                    )
            )
        }
    }
}

@Composable
fun ActivityFeedRow(log: com.example.data.QuestLog) {
    val rankColor = when (log.rank) {
        "S" -> ColorRankS
        "A" -> ColorRankA
        "B" -> ColorRankB
        "C" -> ColorRankC
        "D" -> ColorRankD
        else -> ColorRankE
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, DarkGreyBorder, RoundedCornerShape(8.dp))
            .background(SystemCardDark)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(rankColor.copy(alpha = 0.15f))
                .border(1.dp, rankColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = log.rank,
                color = rankColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Gate Conquered: ${log.habitTitle}",
                color = TextCrispWhite,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "COMPLETED SECURELY IN LOCAL GRID TIME",
                fontSize = 9.sp,
                color = TextMutedGrey,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "+${log.xpEarned} EXP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SystemNeonCyan,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "+${log.goldEarned} GOLD",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SystemGold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}


// ==================== TAB 3: REST SHOP (THE REWARD WINDOW) ====================
@Composable
fun RestShopTabScreen(
    shopItems: List<ShopItem>,
    stats: PlayerStats?,
    onBuy: (ShopItem) -> Unit,
    onDelete: (ShopItem) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemDarkBlue)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "▶ REST SYSTEM RECOVERY SHOP",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = SystemGold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Balance Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SystemGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .background(SystemCardDark)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "AVAILABLE GOLD TOKENS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextMutedGrey,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = "Gold Tokens",
                        tint = SystemGold,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${stats?.gold ?: 0}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = SystemGold
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "Rest Scrolls replenish fatigue to prevent severe system failure penalties.",
                    color = TextMutedGrey,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "▶ REST SCROLLS CATALOGUE",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = SystemNeonCyan,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (shopItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO ITEMS IN STORE DICTIONARY.",
                    fontFamily = FontFamily.Monospace,
                    color = TextMutedGrey,
                    fontSize = 11.sp
                )
            }
        } else {
            shopItems.forEach { item ->
                ShopItemRow(
                    item = item,
                    canAfford = (stats?.gold ?: 0) >= item.cost,
                    onBuy = { onBuy(item) },
                    onDelete = { onDelete(item) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun ShopItemRow(
    item: ShopItem,
    canAfford: Boolean,
    onBuy: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = when (item.iconName) {
        "local_cafe" -> Icons.Default.LocalCafe
        "hotel" -> Icons.Default.Weekend
        "sports_esports" -> Icons.Default.SportsEsports
        "weekend" -> Icons.Default.Weekend
        "restaurant" -> Icons.Default.Restaurant
        "movie" -> Icons.Default.Movie
        else -> Icons.Default.CardGiftcard
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (canAfford) SystemGold.copy(alpha = 0.5f) else DarkGreyBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SystemCardDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SystemDarkBlue)
                    .border(1.dp, SystemGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.title,
                    tint = SystemGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    color = TextCrispWhite,
                    fontSize = 14.sp
                )
                Text(
                    text = item.description,
                    color = TextMutedGrey,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
                
                if (item.purchasedCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ACQUIRED: ${item.purchasedCount} TIMES",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = SystemNeonCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Button(
                    onClick = onBuy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) SystemGold else SystemDarkBlue,
                        contentColor = if (canAfford) SystemDarkBlue else TextMutedGrey
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "${item.cost} G",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }

                if (!item.isBuiltIn) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "FORGED ITEM [DEL]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = Color.Red.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable { onDelete() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}


// ==================== SYSTEM POPUPS AND CUSTOM OVERLAYS ====================

@Composable
fun SystemBannerMessage(
    message: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        message?.let { text ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF030C16))
                    .border(1.5.dp, SystemNeonCyan, RoundedCornerShape(8.dp))
                    .clickable { onDismiss() }
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SystemNeonCyan)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = text,
                            color = TextCrispWhite,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SystemNeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Automatical system banner close
            LaunchedEffect(key1 = text) {
                delay(3500)
                onDismiss()
            }
        }
    }
}

@Composable
fun DailyQuestNoticeModal(
    onAccept: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, SystemNeonCyan, RoundedCornerShape(16.dp))
                .background(Color(0xFF02070E))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Neon flashing Alert Icon
                val infiniteTransition = rememberInfiniteTransition(label = "Flash")
                val alphaVal by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "flashingAlpha"
                )

                Text(
                    text = "⚠ SYSTEM ALARM ⚠",
                    color = Color.Red,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.alpha(alphaVal)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Divider(color = SystemNeonCyan, thickness = 1.dp)

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "DAILY QUEST HAS ARRIVED",
                    color = SystemNeonCyan,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "The Daily Quest Log has been successfully prepared for Player Sung Jin-Woo.\n\nCompleting quests increases attributes and grants break-time reward currency.\n\n※ WARNING: Neglecting assigned tasks may result in severe system penalty quests.",
                    color = TextCrispWhite,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SystemNeonCyan,
                        contentColor = SystemDarkBlue
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "ACCEPT QUESTS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// Level Up Anime modal!
@Composable
fun LevelUpDialog(
    result: LevelUpResult,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LevelUpEffects")
    
    // Rotating halo simulation (CSS transform: rotate)
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HaloRotation"
    )

    // Pulsing halo scale (CSS transform: scale / keyframe glow)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Cosmic glow intensity
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    // Floating particles (CSS floating keys)
    val particleOffset1 by infiniteTransition.animateFloat(
        initialValue = 150f,
        targetValue = -150f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Particle1"
    )
    val particleOffset2 by infiniteTransition.animateFloat(
        initialValue = 180f,
        targetValue = -180f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Particle2"
    )

    // Entry anim states
    var textTriggered by remember { mutableStateOf(false) }
    var statsTriggered by remember { mutableStateOf(false) }
    var buttonTriggered by remember { mutableStateOf(false) }

    // Sequential cascade entry
    LaunchedEffect(Unit) {
        delay(150)
        textTriggered = true
        delay(400)
        statsTriggered = true
        delay(500)
        buttonTriggered = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(pulseScale)
                .border(
                    border = BorderStroke(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(SystemGold, SystemPurple, SystemNeonCyan)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(Color(0xFF04060C))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background Animation Overlay: Cybernetic Rotating Halo & Rising Particles (CSS Canvas feeling)
            Canvas(modifier = Modifier.matchParentSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw rotating cyber-halo behind content
                drawContext.canvas.save()
                drawContext.transform.rotate(rotationAngle, pivot = center)
                
                // Outer circle dash lines
                drawCircle(
                    color = SystemNeonCyan.copy(alpha = glowAlpha * 0.25f),
                    radius = center.x * 0.8f,
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f), 0f)
                    )
                )
                
                // Inner circle lines
                drawCircle(
                    color = SystemPurple.copy(alpha = glowAlpha * 0.2f),
                    radius = center.x * 0.65f,
                    style = Stroke(
                        width = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                    )
                )

                drawContext.canvas.restore()

                // Rising CSS-like sparks/mana-crystals
                // Particle A
                drawCircle(
                    color = SystemGold.copy(alpha = 0.5f),
                    radius = 8f,
                    center = androidx.compose.ui.geometry.Offset(canvasWidth * 0.15f, canvasHeight / 2 + particleOffset1)
                )
                // Particle B
                drawCircle(
                    color = SystemNeonCyan.copy(alpha = 0.6f),
                    radius = 6f,
                    center = androidx.compose.ui.geometry.Offset(canvasWidth * 0.85f, canvasHeight / 2 + particleOffset2)
                )
                // Particle C (middle)
                drawCircle(
                    color = SystemPurple.copy(alpha = 0.5f),
                    radius = 10f,
                    center = androidx.compose.ui.geometry.Offset(canvasWidth * 0.5f, canvasHeight / 2 + particleOffset1 * 1.2f)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Glow star / crown indicator with rotate anim
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SystemGold.copy(alpha = 0.12f))
                        .border(1.5.dp, SystemGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Celebration",
                        tint = SystemGold,
                        modifier = Modifier
                            .size(38.dp)
                            .graphicsLayer(rotationZ = rotationAngle)
                    )
                }
                
                Spacer(modifier = Modifier.height(14.dp))

                // Fading, scale-in Title (CSS zoomIn)
                AnimatedVisibility(
                    visible = textTriggered,
                    enter = scaleIn(initialScale = 0.5f) + fadeIn(),
                    exit = shrinkOut()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "L E V E L   U P !",
                            color = SystemGold,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 28.sp,
                            letterSpacing = 3.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer(alpha = 0.95f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "▶ SYSTEM CLASSIFICATION ELEVATED ◀",
                            color = SystemNeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = DarkGreyBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Progression text
                Text(
                    text = "SHADOW HUNTER EVOLUTION",
                    color = TextMutedGrey,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "LV. ${result.prevLevel}",
                        color = TextMutedGrey,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    // Shockwave/Glow arrow
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "To",
                        tint = SystemNeonCyan,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .size(20.dp)
                            .scale(pulseScale)
                    )
                    
                    Text(
                        text = "LV. ${result.newLevel}",
                        color = SystemNeonCyan,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.scale(pulseScale)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Growth details box (CSS SlideUp & FadeIn stagger)
                AnimatedVisibility(
                    visible = statsTriggered,
                    enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SystemCardDark)
                            .border(
                                border = BorderStroke(
                                    width = 1.dp,
                                    brush = Brush.horizontalGradient(listOf(DarkGreyBorder, SystemPurple.copy(alpha = 0.3f)))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "▶ REORDER BONUS LOGS",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = SystemPurple,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Stats row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SystemNeonCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "All Attribute parameters scaled up (+2)",
                                    color = TextCrispWhite,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // Gold row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SystemGold)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "System reward token bonus: +300 Gold",
                                    color = SystemGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // Gate completed class row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SystemPurple)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Conquered Gate: ${result.rank}-Class (+${result.xpGained} XP)",
                                    color = TextCrispWhite,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // Attribute upgrade
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SystemNeonBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Affiliated Stat growth: ${result.statGained} ➔ ${result.statValueAfter}",
                                    color = SystemNeonCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Animated Button
                AnimatedVisibility(
                    visible = buttonTriggered,
                    enter = scaleIn() + fadeIn()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SystemGold,
                            contentColor = SystemDarkBlue
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .border(1.dp, SystemGold, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "CONQUER FURTHER",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}


// Create Quest and complexity analysis
@Composable
fun CreateQuestDialog(
    viewModel: QuestViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val title by viewModel.createTitle.collectAsState()
    val desc by viewModel.createDesc.collectAsState()
    val category by viewModel.createCategory.collectAsState()
    val liveRank by viewModel.liveRankPreview.collectAsState()

    val rankColor = when (liveRank) {
        "S" -> ColorRankS
        "A" -> ColorRankA
        "B" -> ColorRankB
        "C" -> ColorRankC
        "D" -> ColorRankD
        else -> ColorRankE
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, SystemNeonCyan, RoundedCornerShape(16.dp))
                .background(SystemDarkBlue)
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "▶ SUMMON GATE QUEST",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = SystemNeonCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Title Input
                Text(
                    text = "QUEST MAIN OBJECTIVE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMutedGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.createTitle.value = it },
                    placeholder = { Text("e.g. Finish Android repository rewrite", fontSize = 12.sp, color = TextMutedGrey) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextCrispWhite,
                        unfocusedTextColor = TextCrispWhite,
                        focusedBorderColor = SystemNeonCyan,
                        unfocusedBorderColor = DarkGreyBorder,
                        focusedContainerColor = SystemCardDark,
                        unfocusedContainerColor = SystemCardDark
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description Input
                Text(
                    text = "QUEST DESCRIPTION / COMPILING METADATA",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMutedGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { viewModel.createDesc.value = it },
                    placeholder = { Text("Describe details. More items and long details scale complex rank output!", fontSize = 11.sp, color = TextMutedGrey) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextCrispWhite,
                        unfocusedTextColor = TextCrispWhite,
                        focusedBorderColor = SystemNeonCyan,
                        unfocusedBorderColor = DarkGreyBorder,
                        focusedContainerColor = SystemCardDark,
                        unfocusedContainerColor = SystemCardDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Associated Category / Stat Selector
                Text(
                    text = "ASSOCIATED ATTRIBUTE INDEX",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMutedGrey
                )
                Spacer(modifier = Modifier.height(4.dp))

                val categoriesList = listOf("Physical Training", "Mental Growth", "Mindfulness Focus", "Active Wellness", "Core Discipline")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoriesList.forEach { cat ->
                        val isSelected = category == cat
                        val catColor = if (isSelected) SystemNeonCyan else DarkGreyBorder
                        val contentColor = if (isSelected) SystemDarkBlue else TextCrispWhite
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) SystemNeonCyan else SystemCardDark)
                                .border(1.dp, catColor, RoundedCornerShape(6.dp))
                                .clickable { viewModel.createCategory.value = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat.uppercase(),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- THE LIVE COMPLEXITY ANALYZER CONTAINER ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(rankColor.copy(alpha = 0.08f))
                        .border(1.dp, rankColor, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Intelligence analyzer",
                                tint = rankColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SYSTEM DYNAMIC CLASSIFICATION",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = rankColor
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "QUEST COMPLEXITY: $liveRank-RANK",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = rankColor
                        )
                        
                        Text(
                            text = "Rewards calculated: +${
                                when(liveRank){
                                    "S" -> 500
                                    "A" -> 250
                                    "B" -> 120
                                    "C" -> 60
                                    "D" -> 30
                                    else -> 15
                                }
                            } EXP, +${
                                when(liveRank){
                                    "S" -> 600
                                    "A" -> 300
                                    "B" -> 150
                                    "C" -> 80
                                    "D" -> 40
                                    else -> 20
                                }
                            } Gold",
                            color = TextCrispWhite,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, DarkGreyBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextCrispWhite)
                    ) {
                        Text("ABANDON", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SystemNeonCyan,
                            contentColor = SystemDarkBlue
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SUMMON", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}


@Composable
fun EditQuestDialog(
    viewModel: QuestViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val title by viewModel.editTitle.collectAsState()
    val desc by viewModel.editDesc.collectAsState()
    val category by viewModel.editCategory.collectAsState()
    val liveRank by viewModel.liveEditRankPreview.collectAsState()

    val rankColor = when (liveRank) {
        "S" -> ColorRankS
        "A" -> ColorRankA
        "B" -> ColorRankB
        "C" -> ColorRankC
        "D" -> ColorRankD
        else -> ColorRankE
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, SystemNeonCyan, RoundedCornerShape(16.dp))
                .background(SystemDarkBlue)
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "▶ ALTER GATE QUEST",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = SystemNeonCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Title Input
                Text(
                    text = "QUEST MAIN OBJECTIVE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMutedGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.editTitle.value = it },
                    placeholder = { Text("e.g. Finish Android repository rewrite", fontSize = 12.sp, color = TextMutedGrey) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextCrispWhite,
                        unfocusedTextColor = TextCrispWhite,
                        focusedBorderColor = SystemNeonCyan,
                        unfocusedBorderColor = DarkGreyBorder,
                        focusedContainerColor = SystemCardDark,
                        unfocusedContainerColor = SystemCardDark
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description Input
                Text(
                    text = "QUEST DESCRIPTION / COMPILING METADATA",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMutedGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { viewModel.editDesc.value = it },
                    placeholder = { Text("Describe details.", fontSize = 11.sp, color = TextMutedGrey) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextCrispWhite,
                        unfocusedTextColor = TextCrispWhite,
                        focusedBorderColor = SystemNeonCyan,
                        unfocusedBorderColor = DarkGreyBorder,
                        focusedContainerColor = SystemCardDark,
                        unfocusedContainerColor = SystemCardDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Associated Category / Stat Selector
                Text(
                    text = "ASSOCIATED ATTRIBUTE INDEX",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMutedGrey
                )
                Spacer(modifier = Modifier.height(4.dp))

                val categoriesList = listOf("Physical Training", "Mental Growth", "Mindfulness Focus", "Active Wellness", "Core Discipline")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoriesList.forEach { cat ->
                        val isSelected = category == cat
                        val catColor = if (isSelected) SystemNeonCyan else DarkGreyBorder
                        val contentColor = if (isSelected) SystemDarkBlue else TextCrispWhite
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) SystemNeonCyan else SystemCardDark)
                                .border(1.dp, catColor, RoundedCornerShape(6.dp))
                                .clickable { viewModel.editCategory.value = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat.uppercase(),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- THE LIVE COMPLEXITY ANALYZER CONTAINER ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(rankColor.copy(alpha = 0.08f))
                        .border(1.dp, rankColor, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Intelligence analyzer",
                                tint = rankColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SYSTEM DYNAMIC CLASSIFICATION",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = rankColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "QUEST COMPLEXITY: $liveRank-RANK",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = rankColor
                        )

                        Text(
                            text = "Rewards calculated: +${
                                when(liveRank){
                                    "S" -> 500
                                    "A" -> 250
                                    "B" -> 120
                                    "C" -> 60
                                    "D" -> 30
                                    else -> 15
                                }
                            } EXP, +${
                                when(liveRank){
                                    "S" -> 600
                                    "A" -> 300
                                    "B" -> 150
                                    "C" -> 80
                                    "D" -> 40
                                    else -> 20
                                }
                            } Gold",
                            color = TextCrispWhite,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, DarkGreyBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextCrispWhite)
                    ) {
                        Text("CANCEL", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SystemNeonCyan,
                            contentColor = SystemDarkBlue
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SAVE CHANGES", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}


// Create Reward custom popup
@Composable
fun CreateRewardDialog(
    viewModel: QuestViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var costStr by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("sports_esports") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, SystemGold, RoundedCornerShape(16.dp))
                .background(SystemDarkBlue)
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "▶ FORGE FORBIDDEN BREAK SCROLL",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = SystemGold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Title scroll
                Text(
                    text = "RECOVERY SCROLL NAME",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMutedGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. 1 Hour TikTok binging", fontSize = 12.sp, color = TextMutedGrey) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextCrispWhite,
                        unfocusedTextColor = TextCrispWhite,
                        focusedBorderColor = SystemGold,
                        unfocusedBorderColor = DarkGreyBorder,
                        focusedContainerColor = SystemCardDark,
                        unfocusedContainerColor = SystemCardDark
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                Text(
                    text = "RECOVERY OUTCOME DETAILS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMutedGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    placeholder = { Text("Details of reward / break granted", fontSize = 12.sp, color = TextMutedGrey) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextCrispWhite,
                        unfocusedTextColor = TextCrispWhite,
                        focusedBorderColor = SystemGold,
                        unfocusedBorderColor = DarkGreyBorder,
                        focusedContainerColor = SystemCardDark,
                        unfocusedContainerColor = SystemCardDark
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Token cost
                Text(
                    text = "REQUIRED TOKEN SYSTEM COST (G)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMutedGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = costStr,
                    onValueChange = { costStr = it },
                    placeholder = { Text("e.g. 150", fontSize = 12.sp, color = TextMutedGrey) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextCrispWhite,
                        unfocusedTextColor = TextCrispWhite,
                        focusedBorderColor = SystemGold,
                        unfocusedBorderColor = DarkGreyBorder,
                        focusedContainerColor = SystemCardDark,
                        unfocusedContainerColor = SystemCardDark
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Icon chooser
                Text(
                    text = "CHOOSE ICON VISUALIZER",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMutedGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val iconsList = listOf(
                    "sports_esports" to Icons.Default.SportsEsports,
                    "local_cafe" to Icons.Default.LocalCafe,
                    "weekend" to Icons.Default.Weekend,
                    "restaurant" to Icons.Default.Restaurant,
                    "movie" to Icons.Default.Movie
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconsList.forEach { (name, iconVector) ->
                        val isSelected = selectedIcon == name
                        IconButton(
                            onClick = { selectedIcon = name },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SystemGold else SystemCardDark)
                                .border(1.dp, if (isSelected) SystemGold else DarkGreyBorder, RoundedCornerShape(8.dp))
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = name,
                                tint = if (isSelected) SystemDarkBlue else SystemGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, DarkGreyBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextCrispWhite)
                    ) {
                        Text("SCRAP", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val costInt = costStr.toIntOrNull() ?: 100
                            onConfirm(title, desc, costInt, selectedIcon)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SystemGold,
                            contentColor = SystemDarkBlue
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("FORGE", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// ==================== TAB 2.5: SYSTEM VISUAL SKILL TREE SCREEN ====================
@Composable
fun SkillTreeTabScreen(
    stats: PlayerStats?,
    onAllocate: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemDarkBlue)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Screen Header
        Text(
            text = "▶ SYSTEM STATUS SKILL CONDUIT",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = SystemNeonCyan,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (stats == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SystemNeonCyan)
            }
        } else {
            // Visual Banner showing status points available
            StatusPointsHeader(statPoints = stats.statPoints)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Interactive Skill Tree Map (Visual canvas with connecting paths)
            VisualSkillTreeLayout(stats = stats, onAllocate = onAllocate)
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun StatusPointsHeader(statPoints: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "PointsPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PointsScale"
    )
    
    val isPointsAvailable = statPoints > 0
    val borderBrush = if (isPointsAvailable) {
        Brush.linearGradient(listOf(SystemGold, SystemPurple))
    } else {
        Brush.linearGradient(listOf(DarkGreyBorder, DarkGreyBorder))
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isPointsAvailable) pulseScale else 1f)
            .clip(RoundedCornerShape(12.dp))
            .background(SystemCardDark)
            .border(2.dp, borderBrush, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "UNALLOCATED STATUS POINTS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isPointsAvailable) SystemGold else TextMutedGrey,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isPointsAvailable) {
                        "THE SYSTEM OFFERS STABILITY. CHOOSE WISELY."
                    } else {
                        "CLEAR QUESTS AND GATES TO ACCUMULATE POINTS."
                    },
                    fontSize = 9.sp,
                    color = TextCrispWhite.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )
            }
            
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isPointsAvailable) SystemGold.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.1f))
                    .border(1.5.dp, if (isPointsAvailable) SystemGold else TextMutedGrey, CircleShape)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$statPoints",
                    color = if (isPointsAvailable) SystemGold else TextCrispWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun VisualSkillTreeLayout(
    stats: PlayerStats,
    onAllocate: (String) -> Unit
) {
    val lineGlowAlpha = rememberInfiniteTransition(label = "LineGlow").animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow"
    ).value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            
            val yCore = h * 0.08f
            val yMid = h * 0.45f
            val yBottom = h * 0.82f
            
            val xCore = w / 2f
            val xLeft = w * 0.18f
            val xCenter = w / 2f
            val xRight = w * 0.82f
            
            val xBottomLeft = w * 0.28f
            val xBottomRight = w * 0.72f
            
            // Core -> STRENGTH
            drawLine(
                color = SystemNeonCyan.copy(alpha = lineGlowAlpha),
                start = androidx.compose.ui.geometry.Offset(xCore, yCore),
                end = androidx.compose.ui.geometry.Offset(xLeft, yMid),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )
            // Core -> AGILITY
            drawLine(
                color = SystemNeonCyan.copy(alpha = lineGlowAlpha),
                start = androidx.compose.ui.geometry.Offset(xCore, yCore),
                end = androidx.compose.ui.geometry.Offset(xCenter, yMid),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )
            // Core -> SENSE
            drawLine(
                color = SystemNeonCyan.copy(alpha = lineGlowAlpha),
                start = androidx.compose.ui.geometry.Offset(xCore, yCore),
                end = androidx.compose.ui.geometry.Offset(xRight, yMid),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )
            // STRENGTH -> VITALITY
            drawLine(
                color = SystemPurple.copy(alpha = lineGlowAlpha),
                start = androidx.compose.ui.geometry.Offset(xLeft, yMid),
                end = androidx.compose.ui.geometry.Offset(xBottomLeft, yBottom),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )
            // SENSE -> INTELLIGENCE
            drawLine(
                color = SystemPurple.copy(alpha = lineGlowAlpha),
                start = androidx.compose.ui.geometry.Offset(xRight, yMid),
                end = androidx.compose.ui.geometry.Offset(xBottomRight, yBottom),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // TOP LEVEL: System Core Hub
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(SystemDarkBlue)
                    .border(
                        border = BorderStroke(
                            width = 2.dp,
                            brush = Brush.radialGradient(listOf(SystemNeonCyan, SystemPurple))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Warden Core",
                        tint = SystemNeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "CORE HUB",
                        fontSize = 9.sp,
                        color = TextCrispWhite,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // MID LEVEL NODES: Strength, Agility, Sense
            Text(
                text = "▶ INITIATOR SKILLS ◀",
                color = TextMutedGrey,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            SkillTreeNodeCard(
                title = "STRENGTH",
                subtitle = "Iron Muscle",
                value = stats.strength,
                description = "Enhances brute physical torque, lifting power, and overall impact forces.",
                icon = Icons.Default.FitnessCenter,
                accentColor = SystemGold,
                hasPoints = stats.statPoints > 0,
                onUpgrade = { onAllocate("STRENGTH") }
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            SkillTreeNodeCard(
                title = "AGILITY",
                subtitle = "Echo Evasion",
                value = stats.agility,
                description = "Amplifies reaction rates, tactical coordination, and movement reflex speed.",
                icon = Icons.Default.DirectionsRun,
                accentColor = SystemNeonCyan,
                hasPoints = stats.statPoints > 0,
                onUpgrade = { onAllocate("AGILITY") }
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            SkillTreeNodeCard(
                title = "SENSE",
                subtitle = "Warden Intuition",
                value = stats.sense,
                description = "Sharpens spatial awareness, danger foresight, and perceptual clarity.",
                icon = Icons.Default.Visibility,
                accentColor = SystemPurple,
                hasPoints = stats.statPoints > 0,
                onUpgrade = { onAllocate("SENSE") }
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // BOTTOM LEVEL NODES: Vitality, Intelligence
            Text(
                text = "▶ MONARCH CHRONICLE SKILLS ◀",
                color = TextMutedGrey,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            SkillTreeNodeCard(
                title = "VITALITY",
                subtitle = "Immortal Aegis",
                value = stats.vitality,
                description = "Dramatically broadens health reservoirs and cellular energy stamina.",
                icon = Icons.Default.Favorite,
                accentColor = Color(0xFFFF3366),
                hasPoints = stats.statPoints > 0,
                onUpgrade = { onAllocate("VITALITY") }
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            SkillTreeNodeCard(
                title = "INTELLIGENCE",
                subtitle = "Infinite Mind",
                value = stats.intelligence,
                description = "Heightens deep cognitive flow and boosts magical mana capacities.",
                icon = Icons.Default.Psychology,
                accentColor = Color(0xFF0099FF),
                hasPoints = stats.statPoints > 0,
                onUpgrade = { onAllocate("INTELLIGENCE") }
            )
        }
    }
}

@Composable
fun SkillTreeNodeCard(
    title: String,
    subtitle: String,
    value: Int,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    hasPoints: Boolean,
    onUpgrade: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NodeGlow")
    val buttonPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BtnPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SystemCardDark)
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(DarkGreyBorder, accentColor.copy(alpha = 0.4f))
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f))
                    .border(1.5.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = title,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = accentColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = subtitle,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = TextMutedGrey
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(0.5.dp, accentColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LV. $value",
                            color = TextCrispWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = TextCrispWhite.copy(alpha = 0.85f),
                    lineHeight = 14.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    val progressFraction = (value % 10) / 10f
                    val filledProgress = if (progressFraction == 0f) 0.1f else progressFraction
                    
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(filledProgress)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }
            
            IconButton(
                onClick = onUpgrade,
                modifier = Modifier
                    .size(36.dp)
                    .scale(if (hasPoints) buttonPulseScale else 1f)
                    .clip(CircleShape)
                    .background(if (hasPoints) accentColor else Color.Gray.copy(alpha = 0.1f))
                    .border(
                        width = 1.dp,
                        color = if (hasPoints) accentColor else Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Allocate Point",
                    tint = if (hasPoints) SystemDarkBlue else TextMutedGrey,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
