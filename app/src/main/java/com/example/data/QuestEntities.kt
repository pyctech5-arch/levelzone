package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val rank: String, // "E", "D", "C", "B", "A", "S"
    val associatedStat: String, // "STRENGTH", "AGILITY", "SENSE", "VITALITY", "INTELLIGENCE"
    val category: String = "Physical Training", // "Physical Training", "Mental Growth", "Mindfulness Focus", "Active Wellness", "Core Discipline"
    val isCompletedToday: Boolean = false,
    val streak: Int = 0,
    val lastCompletedTimestamp: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quest_logs")
data class QuestLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val habitTitle: String,
    val rank: String,
    val timestamp: Long = System.currentTimeMillis(),
    val xpEarned: Int,
    val goldEarned: Int
)

@Entity(tableName = "player_stats")
data class PlayerStats(
    @PrimaryKey val id: Int = 1,
    val level: Int = 1,
    val xp: Int = 0,
    val gold: Int = 100,
    val strength: Int = 10,
    val agility: Int = 10,
    val sense: Int = 10,
    val vitality: Int = 10,
    val intelligence: Int = 10,
    val title: String = "E-Rank Vanguard",
    val statPoints: Int = 5
) {
    val xpNeeded: Int
        get() = level * 150
}

@Entity(tableName = "shop_items")
data class ShopItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val cost: Int,
    val isBuiltIn: Boolean = false,
    val purchasedCount: Int = 0,
    val iconName: String = "local_cafe"
)
