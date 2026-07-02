package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShadowSoldierDao {
    @Query("SELECT * FROM shadow_soldiers ORDER BY power DESC")
    fun getAllShadowsFlow(): Flow<List<ShadowSoldier>>

    @Query("SELECT * FROM shadow_soldiers WHERE id = :id")
    suspend fun getShadowById(id: Int): ShadowSoldier?

    @Query("SELECT * FROM shadow_soldiers WHERE associatedHabitId = :habitId LIMIT 1")
    suspend fun getShadowByHabitId(habitId: Int): ShadowSoldier?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateShadow(shadow: ShadowSoldier): Long

    @Update
    suspend fun updateShadow(shadow: ShadowSoldier)

    @Delete
    suspend fun deleteShadow(shadow: ShadowSoldier)
}

@Dao
interface EquipmentItemDao {
    @Query("SELECT * FROM equipment_items")
    fun getAllEquipmentFlow(): Flow<List<EquipmentItem>>

    @Query("SELECT * FROM equipment_items WHERE id = :id")
    suspend fun getEquipmentById(id: Int): EquipmentItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateEquipment(item: EquipmentItem): Long

    @Query("UPDATE equipment_items SET isEquipped = 0 WHERE category = :category")
    suspend fun unequipAllInCategory(category: String)

    @Transaction
    suspend fun equipItem(item: EquipmentItem) {
        unequipAllInCategory(item.category)
        insertOrUpdateEquipment(item.copy(isEquipped = true))
    }

    @Query("SELECT * FROM equipment_items WHERE isEquipped = 1")
    suspend fun getEquippedItemsDirect(): List<EquipmentItem>

    @Query("SELECT * FROM equipment_items WHERE isEquipped = 1")
    fun getEquippedItemsFlow(): Flow<List<EquipmentItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<EquipmentItem>)
}

@Dao
interface DungeonDao {
    @Query("SELECT * FROM dungeons")
    fun getAllDungeonsFlow(): Flow<List<DungeonState>>

    @Query("SELECT * FROM dungeons WHERE id = :id")
    suspend fun getDungeonById(id: Int): DungeonState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDungeon(dungeon: DungeonState): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dungeons: List<DungeonState>)
}

@Dao
interface DailyLoginDao {
    @Query("SELECT * FROM daily_login_state WHERE id = 1 LIMIT 1")
    fun getLoginStateFlow(): Flow<DailyLoginState?>

    @Query("SELECT * FROM daily_login_state WHERE id = 1 LIMIT 1")
    suspend fun getLoginStateDirect(): DailyLoginState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLoginState(state: DailyLoginState)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM player_achievements")
    fun getAllAchievementsFlow(): Flow<List<PlayerAchievement>>

    @Query("SELECT * FROM player_achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): PlayerAchievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAchievement(achievement: PlayerAchievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<PlayerAchievement>)
}
