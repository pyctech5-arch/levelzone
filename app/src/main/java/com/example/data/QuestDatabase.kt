package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Habit::class, 
        QuestLog::class, 
        PlayerStats::class, 
        ShopItem::class,
        ShadowSoldier::class,
        EquipmentItem::class,
        DungeonState::class,
        DailyLoginState::class,
        PlayerAchievement::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun questLogDao(): QuestLogDao
    abstract fun playerStatsDao(): PlayerStatsDao
    abstract fun shopDao(): ShopDao
    abstract fun shadowSoldierDao(): ShadowSoldierDao
    abstract fun equipmentItemDao(): EquipmentItemDao
    abstract fun dungeonDao(): DungeonDao
    abstract fun dailyLoginDao(): DailyLoginDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shadow_leveler_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed database on creation
                        INSTANCE?.let { database ->
                            scope.launch(Dispatchers.IO) {
                                seedDatabase(database)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDatabase(db: AppDatabase) {
            // Seed initial stats
            db.playerStatsDao().insertOrUpdateStats(
                PlayerStats(
                    id = 1,
                    level = 1,
                    xp = 0,
                    gold = 100,
                    strength = 10,
                    agility = 10,
                    sense = 10,
                    vitality = 10,
                    intelligence = 10,
                    title = "E-Rank Vanguard",
                    statPoints = 5
                )
            )

            // Seed initial store items
            val defaultItems = listOf(
                ShopItem(
                    title = "D-Rank Espresso Potion",
                    description = "Restores physical energy. Grants an actual 15-min coffee break.",
                    cost = 50,
                    isBuiltIn = true,
                    iconName = "local_cafe"
                ),
                ShopItem(
                    title = "C-Rank Lazy Scroll",
                    description = "Skip a minor habit or take a 30-min premium guilt-free power nap.",
                    cost = 100,
                    isBuiltIn = true,
                    iconName = "hotel"
                ),
                ShopItem(
                    title = "B-Rank Gate Key (Gaming)",
                    description = "Enter the high-tier entertainment dungeon. Entitles 1 hour of gaming/fun.",
                    cost = 200,
                    isBuiltIn = true,
                    iconName = "sports_esports"
                ),
                ShopItem(
                    title = "S-Rank Warden Day Off",
                    description = "The ultimate rest scroll. Complete skip from all daily system requirements.",
                    cost = 500,
                    isBuiltIn = true,
                    iconName = "weekend"
                )
            )
            db.shopDao().insertShopItems(defaultItems)

            // Seed initial Equipment items
            val defaultEquipment = listOf(
                EquipmentItem(
                    name = "Knight's Iron Sword",
                    category = "WEAPON",
                    rarity = "COMMON",
                    statBonusType = "STRENGTH",
                    statBonusValue = 5,
                    goldCost = 80,
                    isPurchased = false,
                    isEquipped = false,
                    iconName = "colorize"
                ),
                EquipmentItem(
                    name = "Warden Dagger",
                    category = "WEAPON",
                    rarity = "EPIC",
                    statBonusType = "AGILITY",
                    statBonusValue = 15,
                    goldCost = 350,
                    isPurchased = false,
                    isEquipped = false,
                    iconName = "content_cut"
                ),
                EquipmentItem(
                    name = "Vanguard Heavy Boots",
                    category = "ARMOR",
                    rarity = "COMMON",
                    statBonusType = "AGILITY",
                    statBonusValue = 5,
                    goldCost = 60,
                    isPurchased = false,
                    isEquipped = false,
                    iconName = "directions_run"
                ),
                EquipmentItem(
                    name = "Iron Vanguard Chestplate",
                    category = "ARMOR",
                    rarity = "RARE",
                    statBonusType = "VITALITY",
                    statBonusValue = 10,
                    goldCost = 150,
                    isPurchased = false,
                    isEquipped = false,
                    iconName = "shield"
                ),
                EquipmentItem(
                    name = "Warden Resonance Ring",
                    category = "ACCESSORY",
                    rarity = "LEGENDARY",
                    statBonusType = "ALL",
                    statBonusValue = 8,
                    goldCost = 500,
                    isPurchased = false,
                    isEquipped = false,
                    iconName = "brightness_low"
                ),
                EquipmentItem(
                    name = "Sage's Secret Book",
                    category = "BOOK",
                    rarity = "RARE",
                    statBonusType = "INTELLIGENCE",
                    statBonusValue = 12,
                    goldCost = 220,
                    isPurchased = false,
                    isEquipped = false,
                    iconName = "menu_book"
                )
            )
            db.equipmentItemDao().insertAll(defaultEquipment)

            // Seed initial themed Dungeons
            val defaultDungeons = listOf(
                DungeonState(
                    name = "Morning Gate: Sunrise Ritual",
                    theme = "Morning",
                    isCleared = false,
                    bossName = "Alarm Golem (Rank E)",
                    bossHp = 100,
                    bossMaxHp = 100,
                    goldReward = 150,
                    xpReward = 300,
                    isBossDefeated = false,
                    completionPercentage = 0
                ),
                DungeonState(
                    name = "Study Vault: Archive of Knowledge",
                    theme = "Study",
                    isCleared = false,
                    bossName = "Procrastination Fiend (Rank C)",
                    bossHp = 200,
                    bossMaxHp = 200,
                    goldReward = 250,
                    xpReward = 500,
                    isBossDefeated = false,
                    completionPercentage = 0
                ),
                DungeonState(
                    name = "Fitness Arena: Iron Coliseum",
                    theme = "Fitness",
                    isCleared = false,
                    bossName = "Gravity Overlord (Rank B)",
                    bossHp = 300,
                    bossMaxHp = 300,
                    goldReward = 350,
                    xpReward = 700,
                    isBossDefeated = false,
                    completionPercentage = 0
                ),
                DungeonState(
                    name = "Work Citadel: Deadline Crucible",
                    theme = "Work",
                    isCleared = false,
                    bossName = "Stress Drake (Rank S)",
                    bossHp = 500,
                    bossMaxHp = 500,
                    goldReward = 500,
                    xpReward = 1000,
                    isBossDefeated = false,
                    completionPercentage = 0
                )
            )
            db.dungeonDao().insertAll(defaultDungeons)

            // Seed initial achievements
            val defaultAchievements = listOf(
                PlayerAchievement(
                    id = "bronze_vanguard",
                    title = "Bronze Vanguard",
                    description = "Demonstrate basic commitment. Complete 5 daily habits or quests.",
                    isUnlocked = false,
                    progress = 0,
                    maxProgress = 5
                ),
                PlayerAchievement(
                    id = "silver_vanguard",
                    title = "Silver Vanguard",
                    description = "Rise through the ranks. Complete 20 daily habits or quests.",
                    isUnlocked = false,
                    progress = 0,
                    maxProgress = 20
                ),
                PlayerAchievement(
                    id = "nexus_warden",
                    title = "Eternal Warden",
                    description = "True ruler of the Nexus. Complete 100 daily habits or quests.",
                    isUnlocked = false,
                    progress = 0,
                    maxProgress = 100
                )
            )
            db.achievementDao().insertAll(defaultAchievements)
        }
    }
}
