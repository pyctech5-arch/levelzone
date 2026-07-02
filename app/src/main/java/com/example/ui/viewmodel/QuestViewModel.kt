package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuestViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = QuestRepository(
        db.habitDao(),
        db.questLogDao(),
        db.playerStatsDao(),
        db.shopDao(),
        db.shadowSoldierDao(),
        db.equipmentItemDao(),
        db.dungeonDao(),
        db.dailyLoginDao(),
        db.achievementDao()
    )

    // Flows for UI observation
    val habits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<QuestLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playerStats: StateFlow<PlayerStats?> = repository.playerStatsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val shopItems: StateFlow<List<ShopItem>> = repository.shopItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // New RPG module Flows
    val shadows: StateFlow<List<ShadowSoldier>> = repository.allShadowsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val equipment: StateFlow<List<EquipmentItem>> = repository.allEquipmentFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val equippedItems: StateFlow<List<EquipmentItem>> = repository.equippedItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dungeons: StateFlow<List<DungeonState>> = repository.allDungeonsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loginState: StateFlow<DailyLoginState?> = repository.loginStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val achievements: StateFlow<List<PlayerAchievement>> = repository.allAchievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Transient States
    val activeLevelUpResult = MutableStateFlow<LevelUpResult?>(null)
    val activeShadowBinding = MutableStateFlow<ShadowSoldier?>(null)
    val sysMessage = MutableStateFlow<String?>(null)
    val showQuestSystemNotice = MutableStateFlow(true) // For Shadow Ascension "Quest Arrived Notification" modal
    
    // Live creation parameters to offer real-time preview of ranks
    val createTitle = MutableStateFlow("")
    val createDesc = MutableStateFlow("")
    val createCategory = MutableStateFlow("Physical Training") // Physical Training, Mental Growth, Mindfulness Focus, Active Wellness, Core Discipline

    val liveRankPreview: StateFlow<String> = combine(createTitle, createDesc) { t, d ->
        repository.analyzeTaskComplexity(t, d)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "E")

    // Live editing parameters to pre-populate and preview edited quests
    val editTitle = MutableStateFlow("")
    val editDesc = MutableStateFlow("")
    val editCategory = MutableStateFlow("Physical Training")
    val editingHabit = MutableStateFlow<Habit?>(null)

    val liveEditRankPreview: StateFlow<String> = combine(editTitle, editDesc) { t, d ->
        repository.analyzeTaskComplexity(t, d)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "E")

    init {
        // Clear old purchase item statuses on boot or pre-populate if empty
        viewModelScope.launch {
            val sharedPrefs = application.getSharedPreferences("shadow_leveler_prefs", android.content.Context.MODE_PRIVATE)
            val lastAssignedDate = sharedPrefs.getString("last_assigned_date", "")
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

            // Check if player has any data, insert default if not loaded
            val direct = db.playerStatsDao().getPlayerStatsDirect()
            if (direct == null) {
                db.playerStatsDao().insertOrUpdateStats(
                    PlayerStats(
                        id = 1, level = 1, xp = 0, gold = 100,
                        strength = 10, agility = 10, sense = 10, vitality = 10, intelligence = 10,
                        title = "E-Rank Vanguard", statPoints = 5
                    )
                )
            }

            // Auto-seed CS Student Quests if there are no habits in database
            val existingHabits = db.habitDao().getAllHabitsDirect()
            if (existingHabits.isEmpty()) {
                repository.seedDefaultCsQuests()
                sharedPrefs.edit().putString("last_assigned_date", todayDate).apply()
            } else if (lastAssignedDate != todayDate) {
                // System detects a new day! Automatically assign and reset all daily quests
                repository.resetAllDailyCompletions()
                repository.seedDefaultCsQuests()
                sharedPrefs.edit().putString("last_assigned_date", todayDate).apply()
                triggerSystemAlert("DAILY QUESTS ASSIGNED: A new day has dawned! All daily quests have been automatically assigned and reset by the System.")
            }
        }
    }

    fun loadDefaultCsQuests() {
        viewModelScope.launch {
            repository.seedDefaultCsQuests()
            triggerSystemAlert("SYSTEM ARCHIVE SYNCED: CS Student Daily Quests synchronized and loaded successfully!")
        }
    }

    // ACTIONS

    fun closeQuestNotice() {
        showQuestSystemNotice.value = false
        triggerSystemAlert("Daily Quest accepted! Establish your boundaries, Vanguard.")
    }

    fun triggerSystemAlert(text: String) {
        sysMessage.value = text
    }

    fun clearSystemAlert() {
        sysMessage.value = null
    }

    fun clearLevelUp() {
        activeLevelUpResult.value = null
    }

    fun clearShadowBinding() {
        activeShadowBinding.value = null
    }

    fun addQuest() {
        val title = createTitle.value.trim()
        val desc = createDesc.value.trim()
        val category = createCategory.value

        if (title.isEmpty()) {
            triggerSystemAlert("SYSTEM ERROR: Quest title cannot be blank!")
            return
        }

        viewModelScope.launch {
            repository.createHabit(title, desc, category)
            // Reset field states
            createTitle.value = ""
            createDesc.value = ""
            createCategory.value = "Physical Training"
            triggerSystemAlert("NEW QUEST REGISTERED: Dynamic Class assigned successfully.")
        }
    }

    fun startEditingQuest(habit: Habit) {
        editingHabit.value = habit
        editTitle.value = habit.title
        editDesc.value = habit.description
        editCategory.value = habit.category
    }

    fun cancelEditingQuest() {
        editingHabit.value = null
    }

    fun saveEditedQuest() {
        val habit = editingHabit.value ?: return
        val title = editTitle.value.trim()
        val desc = editDesc.value.trim()
        val cat = editCategory.value

        if (title.isEmpty()) {
            triggerSystemAlert("SYSTEM ERROR: Quest title cannot be blank!")
            return
        }

        viewModelScope.launch {
            val associatedStat = when (cat) {
                "Physical Training" -> "STRENGTH"
                "Mental Growth" -> "INTELLIGENCE"
                "Mindfulness Focus" -> "SENSE"
                "Active Wellness" -> "VITALITY"
                else -> "AGILITY"
            }
            val rank = repository.analyzeTaskComplexity(title, desc)

            val updated = habit.copy(
                title = title,
                description = desc,
                category = cat,
                rank = rank,
                associatedStat = associatedStat
            )
            repository.updateHabitDirect(updated)
            editingHabit.value = null
            triggerSystemAlert("QUEST UPDATED: Changes saved and synchronized.")
        }
    }

    fun deleteQuest(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            triggerSystemAlert("QUEST REMOVED. The system index has been re-ordered.")
        }
    }

    fun completeQuest(habit: Habit) {
        viewModelScope.launch {
            if (habit.isCompletedToday) {
                // If already completed, let them untoggle it
                val untoggled = habit.copy(
                    isCompletedToday = false,
                    streak = maxOf(0, habit.streak - 1)
                )
                repository.updateHabitDirect(untoggled)
                triggerSystemAlert("QUEST SUSPENDED: Progression log modified.")
                return@launch
            }

            // 1. Complete task and obtain basic rewards
            val result = repository.completeHabit(habit)
            
            // 2. Summon or upgrade a Shadow Soldier!
            val shadow = repository.triggerShadowSoldierSummon(habit.title, habit.associatedStat, habit.id)
            activeShadowBinding.value = shadow
            val shadowNotice = if (shadow.level == 1) {
                "\n✨ SHADOW SUMMONED: \"Arise...\" [${shadow.name}] has joined your Shadow Army!"
            } else {
                "\n⚡ SHADOW LEVELED UP: [${shadow.name}] is now Level ${shadow.level} (Power: ${shadow.power})!"
            }

            // 3. Deal damage to matching themed Dungeon Boss
            val targetDungeonId = when (habit.associatedStat.uppercase()) {
                "STRENGTH", "VITALITY" -> 3 // Fitness Arena (Gravity Overlord)
                "AGILITY" -> 1             // Morning Gate (Alarm Golem)
                "INTELLIGENCE" -> 2        // Study Vault (Procrastination Fiend)
                "SENSE" -> 4               // Work Citadel (Stress Drake)
                else -> 1
            }
            
            val damageDealt = (25..45).random()
            val (bossDefeated, dungeonState) = repository.dealDamageToDungeonBoss(targetDungeonId, damageDealt)
            val dungeonNotice = if (dungeonState != null) {
                if (bossDefeated) {
                    "\n🏆 DUNGEON CLEARED! Defeated ${dungeonState.bossName}! Gained +${dungeonState.xpReward} XP, +${dungeonState.goldReward} Gold!"
                } else {
                    "\n⚔️ DEAL ${damageDealt} DMG to ${dungeonState.bossName}! Boss HP: ${dungeonState.bossHp}/${dungeonState.bossMaxHp}."
                }
            } else ""

            // 4. Update achievements progress
            updateAllAchievementsProgress(1)

            // 5. Notify player
            if (result.isLevelUp) {
                activeLevelUpResult.value = result
                triggerSystemAlert("⚡ CINEMATIC LEVEL UP TRIGGERED! Level ${result.newLevel} Reached! $shadowNotice $dungeonNotice")
            } else {
                triggerSystemAlert("QUEST COMPLETED! Received +${result.xpGained} XP, +${result.goldGained} Gold!$shadowNotice$dungeonNotice")
            }
        }
    }

    private suspend fun updateAllAchievementsProgress(amount: Int) {
        val achievementsList = listOf("bronze_vanguard", "silver_vanguard", "nexus_warden")
        for (id in achievementsList) {
            val ach = db.achievementDao().getAchievementById(id)
            if (ach != null && !ach.isUnlocked) {
                val newProgress = minOf(ach.maxProgress, ach.progress + amount)
                val unlockedNow = newProgress >= ach.maxProgress
                db.achievementDao().insertOrUpdateAchievement(
                    ach.copy(
                        progress = newProgress,
                        isUnlocked = unlockedNow
                    )
                )
                if (unlockedNow) {
                    triggerSystemAlert("🏆 ACHIEVEMENT UNLOCKED: \"${ach.title}\" rank acquired!")
                }
            }
        }
    }

    // ==================== NEW COMPREHENSIVE RPG SYSTEM ACTIONS ====================

    fun purchaseEquipment(itemId: Int) {
        viewModelScope.launch {
            val success = repository.purchaseEquipment(itemId)
            if (success) {
                triggerSystemAlert("EQUIPMENT PURCHASED: Gold spent successfully. Open your Inventory!")
            } else {
                triggerSystemAlert("SYSTEM ERROR: Insufficient Gold Coins. Keep fighting monsters!")
            }
        }
    }

    fun toggleEquipItem(itemId: Int) {
        viewModelScope.launch {
            val success = repository.toggleEquipItem(itemId)
            if (success) {
                triggerSystemAlert("INVENTORY SYNCED: Equipment state updated!")
            }
        }
    }

    fun dealDamageToDungeonBossDirect(dungeonId: Int, damage: Int) {
        viewModelScope.launch {
            val (bossDefeated, dungeonState) = repository.dealDamageToDungeonBoss(dungeonId, damage)
            if (dungeonState != null) {
                if (bossDefeated) {
                    triggerSystemAlert("💥 BOSS SLAIN! Gate Closed! Received +${dungeonState.goldReward} Gold & +${dungeonState.xpReward} XP!")
                } else {
                    triggerSystemAlert("⚔️ Boss damaged: -${damage} HP! Boss HP is now ${dungeonState.bossHp}/${dungeonState.bossMaxHp}.")
                }
            }
        }
    }

    fun claimDailyLoginReward() {
        viewModelScope.launch {
            val (day, description) = repository.claimDailyLoginReward()
            if (day == -1) {
                triggerSystemAlert("⚠️ SYSTEM: $description")
            } else {
                triggerSystemAlert("🎁 LOGIN REWARD CLAIMED (Day $day): $description")
            }
        }
    }

    fun drawGachaChest() {
        viewModelScope.launch {
            val result = repository.drawGachaChest()
            triggerSystemAlert(result)
        }
    }

    fun reawakenPrestige() {
        viewModelScope.launch {
            val success = repository.reawakenPrestige()
            if (success) {
                triggerSystemAlert("🌀 REAWAKENING COMPLETE: You have shed your mortal limits and emerged as an Eternal Warden! Base Stats Increased!")
            } else {
                triggerSystemAlert("⚠️ REAWAKENING BLOCKED: The Nexus requires Vanguard Level 15 before permitting prestige reawakening.")
            }
        }
    }

    fun purchaseReward(item: ShopItem) {
        viewModelScope.launch {
            val success = repository.purchaseShopItem(item)
            if (success) {
                triggerSystemAlert("ITEM ACQUIRED: Spend your ${item.title} Break-Time wisely!")
            } else {
                triggerSystemAlert("SYSTEM WARNING: Insufficient Gold Tokens. Clear more Quests!")
            }
        }
    }

    fun createCustomReward(title: String, description: String, cost: Int, icon: String) {
        if (title.trim().isEmpty() || cost <= 0) {
            triggerSystemAlert("SYSTEM ERROR: Invalid custom reward format!")
            return
        }
        viewModelScope.launch {
            repository.createShopItem(title, description, cost, icon)
            triggerSystemAlert("REWARD REGISTERED: Rest Shop catalog updated.")
        }
    }

    fun deleteReward(item: ShopItem) {
        viewModelScope.launch {
            repository.deleteShopItem(item)
            triggerSystemAlert("REWARD REMOVED FROM STORE.")
        }
    }

    fun forceMidnightReset() {
        viewModelScope.launch {
            repository.resetAllDailyCompletions()
            repository.seedDefaultCsQuests()

            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val sharedPrefs = getApplication<Application>().getSharedPreferences("shadow_leveler_prefs", android.content.Context.MODE_PRIVATE)
            sharedPrefs.edit().putString("last_assigned_date", todayDate).apply()

            triggerSystemAlert("DAILY UPDATE: Midnight reset simulated. Daily quests have been automatically assigned and reset!")
        }
    }

    fun allocateStatPoint(statName: String) {
        viewModelScope.launch {
            val success = repository.allocateStatPoint(statName)
            if (success) {
                triggerSystemAlert("SYSTEM: Stat point successfully allocated to ${statName.uppercase()}!")
            } else {
                triggerSystemAlert("SYSTEM ERROR: Insufficient Stat Points available! Complete more Quests to level up.")
            }
        }
    }

    fun addQuestFromSystem(title: String, description: String, rank: String, associatedStat: String) {
        viewModelScope.launch {
            val category = when (associatedStat.uppercase()) {
                "STRENGTH" -> "Physical Training"
                "INTELLIGENCE" -> "Mental Growth"
                "SENSE" -> "Mindfulness Focus"
                "VITALITY" -> "Active Wellness"
                else -> "Core Discipline"
            }
            db.habitDao().insertHabit(
                Habit(
                    title = title,
                    description = description,
                    rank = rank,
                    associatedStat = associatedStat,
                    category = category,
                    isCompletedToday = false
                )
            )
        }
    }

    fun awardFocusRewards(xp: Int, gold: Int) {
        viewModelScope.launch {
            val stats = db.playerStatsDao().getPlayerStatsDirect()
            if (stats != null) {
                db.playerStatsDao().insertOrUpdateStats(
                    stats.copy(
                        gold = stats.gold + gold,
                        xp = stats.xp + xp
                    )
                )
            }
        }
    }
}
