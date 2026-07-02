package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

data class LevelUpResult(
    val prevLevel: Int,
    val newLevel: Int,
    val isLevelUp: Boolean,
    val xpGained: Int,
    val goldGained: Int,
    val rank: String,
    val statGained: String,
    val statValueAfter: Int
)

class QuestRepository(
    private val habitDao: HabitDao,
    private val questLogDao: QuestLogDao,
    private val playerStatsDao: PlayerStatsDao,
    private val shopDao: ShopDao,
    private val shadowSoldierDao: ShadowSoldierDao,
    private val equipmentItemDao: EquipmentItemDao,
    private val dungeonDao: DungeonDao,
    private val dailyLoginDao: DailyLoginDao,
    private val achievementDao: AchievementDao
) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allLogs: Flow<List<QuestLog>> = questLogDao.getAllLogs()
    val playerStatsFlow: Flow<PlayerStats?> = playerStatsDao.getPlayerStatsFlow()
    val shopItems: Flow<List<ShopItem>> = shopDao.getAllShopItems()

    // New RPG module Flows
    val allShadowsFlow: Flow<List<ShadowSoldier>> = shadowSoldierDao.getAllShadowsFlow()
    val allEquipmentFlow: Flow<List<EquipmentItem>> = equipmentItemDao.getAllEquipmentFlow()
    val equippedItemsFlow: Flow<List<EquipmentItem>> = equipmentItemDao.getEquippedItemsFlow()
    val allDungeonsFlow: Flow<List<DungeonState>> = dungeonDao.getAllDungeonsFlow()
    val loginStateFlow: Flow<DailyLoginState?> = dailyLoginDao.getLoginStateFlow()
    val allAchievementsFlow: Flow<List<PlayerAchievement>> = achievementDao.getAllAchievementsFlow()

    suspend fun createHabit(title: String, description: String, category: String) {
        val calculatedRank = analyzeTaskComplexity(title, description)
        val associatedStat = when (category.uppercase()) {
            "PHYSICAL TRAINING", "PHYSICAL" -> "STRENGTH"
            "MENTAL GROWTH", "COGNITIVE" -> "INTELLIGENCE"
            "MINDFULNESS FOCUS", "MINDFULNESS" -> "SENSE"
            "ACTIVE WELLNESS", "WELLNESS" -> "VITALITY"
            "CORE DISCIPLINE", "CARDIO" -> "AGILITY"
            else -> "VITALITY"
        }
        val finalCategory = when (category.uppercase()) {
            "PHYSICAL", "PHYSICAL TRAINING" -> "Physical Training"
            "COGNITIVE", "MENTAL GROWTH" -> "Mental Growth"
            "MINDFULNESS", "MINDFULNESS FOCUS" -> "Mindfulness Focus"
            "WELLNESS", "ACTIVE WELLNESS" -> "Active Wellness"
            "CARDIO", "CORE DISCIPLINE" -> "Core Discipline"
            else -> category
        }
        val newHabit = Habit(
            title = title,
            description = description,
            rank = calculatedRank,
            associatedStat = associatedStat,
            category = finalCategory
        )
        habitDao.insertHabit(newHabit)
    }

    suspend fun seedDefaultCsQuests() {
        val defaultQuests = listOf(
            Habit(
                title = "Daily Pushups Challenge",
                description = "Perform daily pushups to build strength. Target: +10 per week (Automatically scales based on week of registration).",
                rank = "B",
                associatedStat = "STRENGTH",
                category = "Physical Training"
            ),
            Habit(
                title = "Study CS Core Concepts",
                description = "Spend 2 hours reviewing algorithms, operating systems, networking, system architecture, or your coursework.",
                rank = "A",
                associatedStat = "INTELLIGENCE",
                category = "Mental Growth"
            ),
            Habit(
                title = "Daily Mindset Calibration",
                description = "Dedicate 30 minutes to quiet meditation, mindfulness, or mindful breathing. Lowers stress and sharpens cognitive acuity.",
                rank = "C",
                associatedStat = "SENSE",
                category = "Mindfulness Focus"
            ),
            Habit(
                title = "Algorithm Gate Extraction",
                description = "Solve 1-2 coding problems on LeetCode/HackerRank or optimize part of your software development projects.",
                rank = "B",
                associatedStat = "INTELLIGENCE",
                category = "Mental Growth"
            ),
            Habit(
                title = "Vanguard Hydration Protocol",
                description = "Drink 3 liters of water and take active physical/eye breaks every 60 minutes of keyboard runtime to prevent strain.",
                rank = "D",
                associatedStat = "VITALITY",
                category = "Active Wellness"
            )
        )
        val existing = habitDao.getAllHabitsDirect()
        for (quest in defaultQuests) {
            if (existing.none { it.title.equals(quest.title, ignoreCase = true) }) {
                habitDao.insertHabit(quest)
            }
        }
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    suspend fun updateHabitDirect(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun resetAllDailyCompletions() {
        habitDao.resetDailyCompletion()
    }

    // Complete a task: calculate rewards, update player level, update stats, record a history log
    suspend fun completeHabit(habit: Habit): LevelUpResult {
        // 1. Calculate Rewards based on rank
        val xpGained = when (habit.rank) {
            "E" -> 15
            "D" -> 30
            "C" -> 60
            "B" -> 120
            "A" -> 250
            "S" -> 500
            else -> 15
        }
        val goldGained = when (habit.rank) {
            "E" -> 20
            "D" -> 40
            "C" -> 80
            "B" -> 150
            "A" -> 300
            "S" -> 600
            else -> 20
        }

        // 2. Fetch Player Stats
        val currentStats = playerStatsDao.getPlayerStatsDirect() ?: PlayerStats(
            id = 1, level = 1, xp = 0, gold = 100,
            strength = 10, agility = 10, sense = 10, vitality = 10, intelligence = 10,
            title = "E-Rank Vanguard", statPoints = 5
        )

        // 3. Update the associated physical/cognitive stat +1
        var uStrength = currentStats.strength
        var uAgility = currentStats.agility
        var uSense = currentStats.sense
        var uVitality = currentStats.vitality
        var uIntelligence = currentStats.intelligence
        var uStatPoints = currentStats.statPoints

        when (habit.associatedStat) {
            "STRENGTH" -> uStrength++
            "AGILITY" -> uAgility++
            "SENSE" -> uSense++
            "VITALITY" -> uVitality++
            "INTELLIGENCE" -> uIntelligence++
        }

        val gainedStatValue = when (habit.associatedStat) {
            "STRENGTH" -> uStrength
            "AGILITY" -> uAgility
            "SENSE" -> uSense
            "VITALITY" -> uVitality
            "INTELLIGENCE" -> uIntelligence
            else -> uVitality
        }

        // 4. Update XP and Gold
        var tempXP = currentStats.xp + xpGained
        var tempGold = currentStats.gold + goldGained
        var tempLevel = currentStats.level
        var isLevelUpOccurred = false

        // Loop to allow multiple levelups in case of huge S-rank boost on low levels
        while (tempXP >= getXPNeededForLevel(tempLevel)) {
            tempXP -= getXPNeededForLevel(tempLevel)
            tempLevel++
            isLevelUpOccurred = true
            // Level up bonus: add +2 to all stats, +300 gold, and +5 stat points
            uStrength += 2
            uAgility += 2
            uSense += 2
            uVitality += 2
            uIntelligence += 2
            tempGold += 300
            uStatPoints += 5
        }

        // 5. Update Vanguard Title based on level
        val updatedTitle = when {
            tempLevel >= 50 -> "S-Rank Warden"
            tempLevel >= 35 -> "A-Rank Vanguard Guildmaster"
            tempLevel >= 22 -> "B-Rank Nexus Sentinel"
            tempLevel >= 12 -> "C-Rank Rift Stalker"
            tempLevel >= 5 -> "D-Rank Elite Vanguard"
            else -> "E-Rank Vanguard"
        }

        val updatedStats = PlayerStats(
            id = 1,
            level = tempLevel,
            xp = tempXP,
            gold = tempGold,
            strength = uStrength,
            agility = uAgility,
            sense = uSense,
            vitality = uVitality,
            intelligence = uIntelligence,
            title = updatedTitle,
            statPoints = uStatPoints
        )

        playerStatsDao.insertOrUpdateStats(updatedStats)

        // 6. Complete Habit today in DB
        val completedHabit = habit.copy(
            isCompletedToday = true,
            streak = habit.streak + 1,
            lastCompletedTimestamp = System.currentTimeMillis()
        )
        habitDao.updateHabit(completedHabit)

        // 7. Write completion log History
        val log = QuestLog(
            habitId = habit.id,
            habitTitle = habit.title,
            rank = habit.rank,
            xpEarned = xpGained,
            goldEarned = goldGained
        )
        questLogDao.insertLog(log)

        return LevelUpResult(
            prevLevel = currentStats.level,
            newLevel = tempLevel,
            isLevelUp = isLevelUpOccurred,
            xpGained = xpGained,
            goldGained = goldGained,
            rank = habit.rank,
            statGained = habit.associatedStat,
            statValueAfter = gainedStatValue
        )
    }

    // Purchase screen rest time mechanics
    suspend fun purchaseShopItem(item: ShopItem): Boolean {
        val currentStats = playerStatsDao.getPlayerStatsDirect() ?: return false
        if (currentStats.gold >= item.cost) {
            val updatedStats = currentStats.copy(
                gold = currentStats.gold - item.cost
            )
            playerStatsDao.insertOrUpdateStats(updatedStats)
            shopDao.incrementPurchaseCount(item.id)
            return true
        }
        return false
    }

    suspend fun createShopItem(title: String, description: String, cost: Int, iconName: String) {
        val newItem = ShopItem(
            title = title,
            description = description,
            cost = cost,
            isBuiltIn = false,
            iconName = iconName
        )
        shopDao.insertShopItem(newItem)
    }

    suspend fun deleteShopItem(item: ShopItem) {
        shopDao.deleteShopItem(item)
    }

    suspend fun allocateStatPoint(statName: String): Boolean {
        val currentStats = playerStatsDao.getPlayerStatsDirect() ?: return false
        if (currentStats.statPoints > 0) {
            val updatedStats = when (statName.uppercase()) {
                "STRENGTH" -> currentStats.copy(
                    strength = currentStats.strength + 1,
                    statPoints = currentStats.statPoints - 1
                )
                "AGILITY" -> currentStats.copy(
                    agility = currentStats.agility + 1,
                    statPoints = currentStats.statPoints - 1
                )
                "SENSE" -> currentStats.copy(
                    sense = currentStats.sense + 1,
                    statPoints = currentStats.statPoints - 1
                )
                "VITALITY" -> currentStats.copy(
                    vitality = currentStats.vitality + 1,
                    statPoints = currentStats.statPoints - 1
                )
                "INTELLIGENCE" -> currentStats.copy(
                    intelligence = currentStats.intelligence + 1,
                    statPoints = currentStats.statPoints - 1
                )
                else -> currentStats
            }
            if (updatedStats != currentStats) {
                playerStatsDao.insertOrUpdateStats(updatedStats)
                return true
            }
        }
        return false
    }

    // Custom level threshold logic: increases as level scales up
    private fun getXPNeededForLevel(level: Int): Int {
        return level * 150
    }

    // Automatically analyze title & description to return E, D, C, B, A, or S Rank!
    fun analyzeTaskComplexity(title: String, description: String): String {
        val scanText = (title + " " + description).lowercase()
        var score = 0

        // High priority keywords
        val sTriggers = listOf("monarch", "boss", "impossible", "thesis", "marathon", "all-nighter", "hackathon", "launch", "publish", "exam", "beast", "deployment", "critical")
        val aTriggers = listOf("project", "presentation", "sprint", "difficult", "master", "coding", "complex", "intense", "heavy", "10k", "assignment", "final")
        val bTriggers = listOf("study", "gym", "write", "practice", "workout", "clean", "prepare", "read", "run", "5k", "learn", "exercise", "homework")
        val cTriggers = listOf("walk", "stretch", "hydrate", "water", "review", "minor", "short", "checklist", "sleep", "nap", "brush", "meditate", "clean")

        for (trigger in sTriggers) if (scanText.contains(trigger)) score += 10
        for (trigger in aTriggers) if (scanText.contains(trigger)) score += 6
        for (trigger in bTriggers) if (scanText.contains(trigger)) score += 4
        for (trigger in cTriggers) if (scanText.contains(trigger)) score += 2

        // Scale by character limits to reward detailed planning
        score += title.trim().length / 6
        score += description.trim().length / 14

        return when {
            score >= 20 -> "S"
            score >= 11 -> "A"
            score >= 7 -> "B"
            score >= 4 -> "C"
            score >= 2 -> "D"
            else -> "E"
        }
    }

    // ==================== SYSTEM ECHO GUARDIAN ENGINE ====================
    suspend fun triggerShadowSoldierSummon(habitTitle: String, category: String, habitId: Int): ShadowSoldier {
        val existing = shadowSoldierDao.getShadowByHabitId(habitId)
        if (existing != null) {
            val updated = existing.copy(
                level = existing.level + 1,
                power = existing.power + (existing.level * 40) + 120,
                evolutionStage = when {
                    existing.level >= 15 -> 4 // Eternal Marshal
                    existing.level >= 8 -> 3  // Apex Commander
                    existing.level >= 3 -> 2  // Elite Guardian
                    else -> 1
                }
            )
            shadowSoldierDao.insertOrUpdateShadow(updated)
            return updated
        } else {
            val type = when (category.uppercase()) {
                "STRENGTH" -> "Warrior"
                "AGILITY" -> "Assassin"
                "SENSE" -> "Support"
                "VITALITY" -> "Tank"
                "INTELLIGENCE" -> "Mage"
                else -> "Warrior"
            }
            
            // Generate a cool original fantasy Echo Guardian name
            val prefix = when (type) {
                "Warrior" -> "Gryphon Vanguard"
                "Assassin" -> "Aether Stalker"
                "Support" -> "Lumina Shaman"
                "Tank" -> "Bastion Golem"
                "Mage" -> "Nexus Evoker"
                else -> "Echo Guardian"
            }
            val coolName = "$prefix of $habitTitle"
            
            val newShadow = ShadowSoldier(
                name = coolName,
                type = type,
                level = 1,
                power = 150,
                evolutionStage = 1,
                associatedHabitId = habitId
            )
            shadowSoldierDao.insertOrUpdateShadow(newShadow)
            return newShadow
        }
    }

    // ==================== DUNGEONS AND BOSSES ENGINE ====================
    suspend fun dealDamageToDungeonBoss(dungeonId: Int, damage: Int): Pair<Boolean, DungeonState?> {
        val dungeon = dungeonDao.getDungeonById(dungeonId) ?: return Pair(false, null)
        if (dungeon.isBossDefeated) return Pair(false, dungeon)

        val newHp = maxOf(0, dungeon.bossHp - damage)
        val completion = (((dungeon.bossMaxHp - newHp).toFloat() / dungeon.bossMaxHp) * 100).toInt()
        
        if (newHp == 0) {
            // Boss Defeated!
            val updatedDungeon = dungeon.copy(
                bossHp = 0,
                isBossDefeated = true,
                isCleared = true,
                completionPercentage = 100
            )
            dungeonDao.insertOrUpdateDungeon(updatedDungeon)

            // Award Player rewards
            val currentStats = playerStatsDao.getPlayerStatsDirect() ?: PlayerStats()
            
            var tempLevel = currentStats.level
            var tempXP = currentStats.xp + dungeon.xpReward
            var tempGold = currentStats.gold + dungeon.goldReward
            var isLevelUpOccurred = false
            
            var uStrength = currentStats.strength
            var uAgility = currentStats.agility
            var uSense = currentStats.sense
            var uVitality = currentStats.vitality
            var uIntelligence = currentStats.intelligence
            var uStatPoints = currentStats.statPoints + 3 // Dungeon clear bonus!

            while (tempXP >= getXPNeededForLevel(tempLevel)) {
                tempXP -= getXPNeededForLevel(tempLevel)
                tempLevel++
                isLevelUpOccurred = true
                uStrength += 2
                uAgility += 2
                uSense += 2
                uVitality += 2
                uIntelligence += 2
                tempGold += 300
                uStatPoints += 5
            }

            val updatedTitle = when {
                tempLevel >= 30 -> "National Level Vanguard"
                tempLevel >= 20 -> "S-Rank Vanguard"
                tempLevel >= 15 -> "A-Rank Vanguard"
                tempLevel >= 10 -> "B-Rank Vanguard"
                tempLevel >= 5  -> "C-Rank Vanguard"
                tempLevel >= 3  -> "D-Rank Vanguard"
                else -> "E-Rank Vanguard"
            }

            val updatedStats = currentStats.copy(
                level = tempLevel,
                xp = tempXP,
                gold = tempGold,
                strength = uStrength,
                agility = uAgility,
                sense = uSense,
                vitality = uVitality,
                intelligence = uIntelligence,
                statPoints = uStatPoints,
                title = updatedTitle
            )
            playerStatsDao.insertOrUpdateStats(updatedStats)
            
            return Pair(true, updatedDungeon)
        } else {
            val updatedDungeon = dungeon.copy(
                bossHp = newHp,
                completionPercentage = completion
            )
            dungeonDao.insertOrUpdateDungeon(updatedDungeon)
            return Pair(false, updatedDungeon)
        }
    }

    // ==================== EQUIPMENT SYSTEM ENGINE ====================
    suspend fun purchaseEquipment(itemId: Int): Boolean {
        val item = equipmentItemDao.getEquipmentById(itemId) ?: return false
        if (item.isPurchased) return false

        val currentStats = playerStatsDao.getPlayerStatsDirect() ?: return false
        if (currentStats.gold >= item.goldCost) {
            // Deduct Gold and purchase
            val updatedStats = currentStats.copy(gold = currentStats.gold - item.goldCost)
            playerStatsDao.insertOrUpdateStats(updatedStats)
            
            val updatedItem = item.copy(isPurchased = true)
            equipmentItemDao.insertOrUpdateEquipment(updatedItem)
            return true
        }
        return false
    }

    suspend fun toggleEquipItem(itemId: Int): Boolean {
        val item = equipmentItemDao.getEquipmentById(itemId) ?: return false
        if (!item.isPurchased) return false

        val currentStats = playerStatsDao.getPlayerStatsDirect() ?: return false

        if (item.isEquipped) {
            // Unequip
            equipmentItemDao.insertOrUpdateEquipment(item.copy(isEquipped = false))
            
            // Remove stat bonuses
            val updatedStats = when (item.statBonusType.uppercase()) {
                "STRENGTH" -> currentStats.copy(strength = maxOf(10, currentStats.strength - item.statBonusValue))
                "AGILITY" -> currentStats.copy(agility = maxOf(10, currentStats.agility - item.statBonusValue))
                "SENSE" -> currentStats.copy(sense = maxOf(10, currentStats.sense - item.statBonusValue))
                "VITALITY" -> currentStats.copy(vitality = maxOf(10, currentStats.vitality - item.statBonusValue))
                "INTELLIGENCE" -> currentStats.copy(intelligence = maxOf(10, currentStats.intelligence - item.statBonusValue))
                "ALL" -> currentStats.copy(
                    strength = maxOf(10, currentStats.strength - item.statBonusValue),
                    agility = maxOf(10, currentStats.agility - item.statBonusValue),
                    sense = maxOf(10, currentStats.sense - item.statBonusValue),
                    vitality = maxOf(10, currentStats.vitality - item.statBonusValue),
                    intelligence = maxOf(10, currentStats.intelligence - item.statBonusValue)
                )
                else -> currentStats
            }
            playerStatsDao.insertOrUpdateStats(updatedStats)
        } else {
            // Equip (automatically unequips same category first via Dao transaction)
            val equippedInCategory = equipmentItemDao.getEquippedItemsDirect().firstOrNull { it.category == item.category }
            
            var statsWithUnequip = currentStats
            if (equippedInCategory != null) {
                // Subtract old equipped item stats first
                statsWithUnequip = when (equippedInCategory.statBonusType.uppercase()) {
                    "STRENGTH" -> currentStats.copy(strength = maxOf(10, currentStats.strength - equippedInCategory.statBonusValue))
                    "AGILITY" -> currentStats.copy(agility = maxOf(10, currentStats.agility - equippedInCategory.statBonusValue))
                    "SENSE" -> currentStats.copy(sense = maxOf(10, currentStats.sense - equippedInCategory.statBonusValue))
                    "VITALITY" -> currentStats.copy(vitality = maxOf(10, currentStats.vitality - equippedInCategory.statBonusValue))
                    "INTELLIGENCE" -> currentStats.copy(intelligence = maxOf(10, currentStats.intelligence - equippedInCategory.statBonusValue))
                    "ALL" -> currentStats.copy(
                        strength = maxOf(10, currentStats.strength - equippedInCategory.statBonusValue),
                        agility = maxOf(10, currentStats.agility - equippedInCategory.statBonusValue),
                        sense = maxOf(10, currentStats.sense - equippedInCategory.statBonusValue),
                        vitality = maxOf(10, currentStats.vitality - equippedInCategory.statBonusValue),
                        intelligence = maxOf(10, currentStats.intelligence - equippedInCategory.statBonusValue)
                    )
                    else -> currentStats
                }
            }

            equipmentItemDao.equipItem(item)

            // Add new stats
            val finalStats = when (item.statBonusType.uppercase()) {
                "STRENGTH" -> statsWithUnequip.copy(strength = statsWithUnequip.strength + item.statBonusValue)
                "AGILITY" -> statsWithUnequip.copy(agility = statsWithUnequip.agility + item.statBonusValue)
                "SENSE" -> statsWithUnequip.copy(sense = statsWithUnequip.sense + item.statBonusValue)
                "VITALITY" -> statsWithUnequip.copy(vitality = statsWithUnequip.vitality + item.statBonusValue)
                "INTELLIGENCE" -> statsWithUnequip.copy(intelligence = statsWithUnequip.intelligence + item.statBonusValue)
                "ALL" -> statsWithUnequip.copy(
                    strength = statsWithUnequip.strength + item.statBonusValue,
                    agility = statsWithUnequip.agility + item.statBonusValue,
                    sense = statsWithUnequip.sense + item.statBonusValue,
                    vitality = statsWithUnequip.vitality + item.statBonusValue,
                    intelligence = statsWithUnequip.intelligence + item.statBonusValue
                )
                else -> statsWithUnequip
            }
            playerStatsDao.insertOrUpdateStats(finalStats)
        }
        return true
    }

    // ==================== DAILY LOGIN SYSTEM ENGINE ====================
    suspend fun claimDailyLoginReward(): Pair<Int, String> {
        val state = dailyLoginDao.getLoginStateDirect() ?: DailyLoginState(id = 1, consecutiveDays = 0, lastClaimedTimestamp = 0)
        
        // Demo mode bypass: Allow claim if at least 15 seconds have passed since last claim
        val isEligible = (System.currentTimeMillis() - state.lastClaimedTimestamp) >= 15000L
        if (!isEligible && state.lastClaimedTimestamp > 0) {
            return Pair(-1, "COOLDOWN: System claim limits in effect. Wait 15s to claim again.")
        }

        val nextDay = if (state.consecutiveDays >= 7) 1 else state.consecutiveDays + 1
        
        val rewardDescription = when (nextDay) {
            1 -> "+100 Gold"
            2 -> "+150 Gold, +1 Strength"
            3 -> "+200 Gold, +1 Agility"
            4 -> "+250 Gold, +1 Sense"
            5 -> "+300 Gold, +1 Vitality"
            6 -> "+400 Gold, +1 Intelligence"
            7 -> "+1000 Gold, +5 Stat Points (MONARCH CHEST!)"
            else -> "+100 Gold"
        }

        // Apply reward to PlayerStats
        val currentStats = playerStatsDao.getPlayerStatsDirect() ?: PlayerStats()
        var uGold = currentStats.gold
        var uStrength = currentStats.strength
        var uAgility = currentStats.agility
        var uSense = currentStats.sense
        var uVitality = currentStats.vitality
        var uIntelligence = currentStats.intelligence
        var uStatPoints = currentStats.statPoints

        when (nextDay) {
            1 -> uGold += 100
            2 -> { uGold += 150; uStrength += 1 }
            3 -> { uGold += 200; uAgility += 1 }
            4 -> { uGold += 250; uSense += 1 }
            5 -> { uGold += 300; uVitality += 1 }
            6 -> { uGold += 400; uIntelligence += 1 }
            7 -> { uGold += 1000; uStatPoints += 5 }
        }

        playerStatsDao.insertOrUpdateStats(
            currentStats.copy(
                gold = uGold,
                strength = uStrength,
                agility = uAgility,
                sense = uSense,
                vitality = uVitality,
                intelligence = uIntelligence,
                statPoints = uStatPoints
            )
        )

        // Save new claim status
        dailyLoginDao.insertOrUpdateLoginState(
            state.copy(
                consecutiveDays = nextDay,
                lastClaimedTimestamp = System.currentTimeMillis()
            )
        )

        return Pair(nextDay, rewardDescription)
    }

    // ==================== GACHA SYSTEM ENGINE ====================
    suspend fun drawGachaChest(): String {
        val currentStats = playerStatsDao.getPlayerStatsDirect() ?: return "ERROR: Player stats not found."
        val cost = 80
        if (currentStats.gold < cost) {
            return "ERROR: Insufficient Gold! Gacha chest costs $cost Gold."
        }

        // Subtract cost
        playerStatsDao.insertOrUpdateStats(currentStats.copy(gold = currentStats.gold - cost))

        val roll = (1..100).random()
        return when {
            roll <= 10 -> {
                // Rare Weapon Unlock! Set one unpurchased to purchased
                val allEquip = equipmentItemDao.getAllEquipmentFlow().firstOrNull() ?: emptyList()
                val unowned = allEquip.filter { !it.isPurchased }
                if (unowned.isNotEmpty()) {
                    val prize = unowned.random()
                    equipmentItemDao.insertOrUpdateEquipment(prize.copy(isPurchased = true))
                    "SYSTEM: S-Rank Mystery Chest opened. Unlocked: [${prize.name}] (${prize.rarity})!"
                } else {
                    val bonusGold = 400
                    playerStatsDao.insertOrUpdateStats(currentStats.copy(gold = currentStats.gold - cost + bonusGold))
                    "SYSTEM: All equipment owned. Refunded: $bonusGold Gold!"
                }
            }
            roll <= 45 -> {
                // Gold Reward
                val prizeGold = (150..350).random()
                val statsNow = playerStatsDao.getPlayerStatsDirect() ?: currentStats
                playerStatsDao.insertOrUpdateStats(statsNow.copy(gold = statsNow.gold + prizeGold))
                "SYSTEM: Vanguard's Supply Box opened. Found: +$prizeGold Gold!"
            }
            roll <= 80 -> {
                // Stat point booster!
                val statsNow = playerStatsDao.getPlayerStatsDirect() ?: currentStats
                playerStatsDao.insertOrUpdateStats(statsNow.copy(statPoints = statsNow.statPoints + 1))
                "SYSTEM: Elixir of Elation consumed. Gained: +1 Stat Point!"
            }
            else -> {
                // Direct stat boost!
                val statsNow = playerStatsDao.getPlayerStatsDirect() ?: currentStats
                val stats = listOf("STRENGTH", "AGILITY", "SENSE", "VITALITY", "INTELLIGENCE")
                val statSelected = stats.random()
                val finalStats = when (statSelected) {
                    "STRENGTH" -> statsNow.copy(strength = statsNow.strength + 1)
                    "AGILITY" -> statsNow.copy(agility = statsNow.agility + 1)
                    "SENSE" -> statsNow.copy(sense = statsNow.sense + 1)
                    "VITALITY" -> statsNow.copy(vitality = statsNow.vitality + 1)
                    else -> statsNow.copy(intelligence = statsNow.intelligence + 1)
                }
                playerStatsDao.insertOrUpdateStats(finalStats)
                "SYSTEM: Rare Attribute Elixir consumed. Permanent boost: +1 $statSelected!"
            }
        }
    }

    // ==================== PRESTIGE REAWAKENING SYSTEM ====================
    suspend fun reawakenPrestige(): Boolean {
        val currentStats = playerStatsDao.getPlayerStatsDirect() ?: return false
        if (currentStats.level < 15) return false // Require Level 15+ for Prestige simulation

        val updatedStats = currentStats.copy(
            level = 1,
            xp = 0,
            gold = currentStats.gold + 1000, // Reawakening bonus!
            strength = 12, // Permanently higher base stats!
            agility = 12,
            sense = 12,
            vitality = 12,
            intelligence = 12,
            statPoints = 20, // Huge starting bonus points
            title = "Awakened Warden"
        )
        playerStatsDao.insertOrUpdateStats(updatedStats)
        return true
    }
}
