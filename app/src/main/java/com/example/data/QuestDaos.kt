package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Int): Habit?

    @Query("UPDATE habits SET isCompletedToday = 0")
    suspend fun resetDailyCompletion()

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsDirect(): List<Habit>
}

@Dao
interface QuestLogDao {
    @Query("SELECT * FROM quest_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<QuestLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: QuestLog)

    @Query("DELETE FROM quest_logs")
    suspend fun clearLogs()
}

@Dao
interface PlayerStatsDao {
    @Query("SELECT * FROM player_stats WHERE id = 1")
    fun getPlayerStatsFlow(): Flow<PlayerStats?>

    @Query("SELECT * FROM player_stats WHERE id = 1")
    suspend fun getPlayerStatsDirect(): PlayerStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: PlayerStats)
}

@Dao
interface ShopDao {
    @Query("SELECT * FROM shop_items ORDER BY cost ASC")
    fun getAllShopItems(): Flow<List<ShopItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShopItem(item: ShopItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShopItems(items: List<ShopItem>)

    @Delete
    suspend fun deleteShopItem(item: ShopItem)

    @Query("UPDATE shop_items SET purchasedCount = purchasedCount + 1 WHERE id = :id")
    suspend fun incrementPurchaseCount(id: Int)
}
