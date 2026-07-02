package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shadow_soldiers")
data class ShadowSoldier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Warrior", "Mage", "Assassin", "Tank", "Support"
    val level: Int = 1,
    val power: Int = 150,
    val evolutionStage: Int = 1, // 1: Echo Recruit, 2: Elite Guardian, 3: Apex Commander, 4: Eternal Marshal
    val associatedHabitId: Int = 0,
    val summonTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "equipment_items")
data class EquipmentItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "WEAPON", "ARMOR", "ACCESSORY", "ARTIFACT", "BOOK", "POTION"
    val rarity: String, // "COMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC"
    val statBonusType: String, // "STRENGTH", "AGILITY", "SENSE", "VITALITY", "INTELLIGENCE", "ALL"
    val statBonusValue: Int,
    val goldCost: Int,
    val isPurchased: Boolean = false,
    val isEquipped: Boolean = false,
    val iconName: String
)

@Entity(tableName = "dungeons")
data class DungeonState(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val theme: String, // "Morning", "Study", "Fitness", "Work", "Weekend"
    val isCleared: Boolean = false,
    val bossName: String,
    val bossHp: Int,
    val bossMaxHp: Int,
    val goldReward: Int,
    val xpReward: Int,
    val isBossDefeated: Boolean = false,
    val completionPercentage: Int = 0
)

@Entity(tableName = "daily_login_state")
data class DailyLoginState(
    @PrimaryKey val id: Int = 1,
    val consecutiveDays: Int = 0,
    val lastClaimedTimestamp: Long = 0
)

@Entity(tableName = "player_achievements")
data class PlayerAchievement(
    @PrimaryKey val id: String, // e.g. "Bronze Vanguard", "Silver Vanguard", "Eternal Warden"
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 10
)
