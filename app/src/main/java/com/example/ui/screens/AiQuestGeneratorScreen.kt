package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.PlayerStats
import com.example.ui.viewmodel.QuestViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// Styling colors
val SystemDarkBlue = Color(0xFF030712)
val SystemCardDark = Color(0xFF111827)
val SystemNeonCyan = Color(0xFF06B6D4)
val SystemPurple = Color(0xFF8B5CF6)
val SystemGold = Color(0xFFFBBF24)
val SystemRed = Color(0xFFEF4444)
val DarkGreyBorder = Color(0xFF1F2937)
val TextMutedGrey = Color(0xFF9CA3AF)

data class QuestOption(
    val title: String,
    val description: String,
    val type: String, // "MAIN", "SIDE", "BONUS", "EMERGENCY"
    val rank: String, // "E", "D", "C", "B", "A", "S", "SS"
    val xpReward: Int,
    val goldReward: Int,
    val statReward: String // "STR +2", "AGI +1", "INT +3", "VIT +2", "PER +1"
)

@Composable
fun AiQuestGeneratorScreen(
    viewModel: QuestViewModel,
    stats: PlayerStats?
) {
    val coroutineScope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }
    var coachMessage by remember { mutableStateOf("The System is monitoring your parameters. Initiate scan to formulate daily calibration missions.") }
    var generatedQuests by remember { mutableStateOf<List<QuestOption>>(emptyList()) }
    
    // Pulse animation for holographic borders
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "border_alpha"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemDarkBlue)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Holographic System Screen Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SystemCardDark)
                    .border(
                        BorderStroke(
                            2.dp,
                            Brush.linearGradient(
                                listOf(
                                    SystemNeonCyan.copy(alpha = borderAlpha),
                                    SystemPurple.copy(alpha = borderAlpha)
                                )
                            )
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Troubleshoot,
                            contentDescription = "System Brain",
                            tint = SystemNeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "SYSTEM ACTIVE CALIBRATOR",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = coachMessage,
                        color = SystemNeonCyan,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        // Generate Action Button
        item {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isGenerating = true
                        coachMessage = "Analyzing previous habits... scanning available time parameters... forging S-Rank dimensional gateways..."
                        delay(2000)
                        
                        val (msg, quests) = runGenerator(stats)
                        coachMessage = msg
                        generatedQuests = quests
                        isGenerating = false
                        viewModel.triggerSystemAlert("⚠️ QUEST DECREE ISSUED: Holographic Quest Scroll successfully manifested!")
                    }
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SystemNeonCyan,
                    contentColor = SystemDarkBlue,
                    disabledContainerColor = SystemCardDark
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("generate_quest_button")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = SystemDarkBlue, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GENERATE SYSTEM QUESTS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Empty state instruction card
        if (generatedQuests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = SystemCardDark),
                    border = BorderStroke(1.dp, DarkGreyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = SystemPurple,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No quests compiled for this solar cycle.",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hit the generator button above. The System will read your active traits (Strength, Agility, Intelligence) to forge customized daily commissions.",
                            color = TextMutedGrey,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Generated Quests List
        items(generatedQuests) { quest ->
            val colorGradient = when (quest.type) {
                "MAIN" -> listOf(SystemNeonCyan, SystemPurple)
                "EMERGENCY" -> listOf(SystemRed, SystemPurple)
                "BONUS" -> listOf(SystemGold, SystemNeonCyan)
                else -> listOf(SystemPurple, DarkGreyBorder)
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SystemCardDark)
                    .border(
                        BorderStroke(1.5.dp, Brush.linearGradient(colorGradient)),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        // Automatically inject quest into player's main list safely!
                        viewModel.addQuestFromSystem(
                            title = "[SYSTEM] ${quest.title}",
                            description = "${quest.description} (Difficulty: ${quest.rank}-Rank. Stat Reward: ${quest.statReward})",
                            rank = quest.rank,
                            associatedStat = when {
                                quest.statReward.contains("STR") -> "STRENGTH"
                                quest.statReward.contains("AGI") -> "AGILITY"
                                quest.statReward.contains("INT") -> "INTELLIGENCE"
                                quest.statReward.contains("VIT") -> "VITALITY"
                                else -> "SENSE"
                            }
                        )
                        viewModel.triggerSystemAlert("⚔️ QUEST MANIFESTED: \"[SYSTEM] ${quest.title}\" has been added to your Quests Tab!")
                    }
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val icon = when (quest.type) {
                                "MAIN" -> Icons.Default.Campaign
                                "EMERGENCY" -> Icons.Default.ReportProblem
                                "BONUS" -> Icons.Default.Celebration
                                else -> Icons.Default.Assignment
                            }
                            val badgeColor = when (quest.type) {
                                "MAIN" -> SystemNeonCyan
                                "EMERGENCY" -> SystemRed
                                "BONUS" -> SystemGold
                                else -> SystemPurple
                            }
                            Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
                            
                            Text(
                                text = "${quest.type} QUEST",
                                color = badgeColor,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(SystemCardDark, RoundedCornerShape(4.dp))
                                .border(1.dp, Brush.linearGradient(colorGradient), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${quest.rank}-Rank",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = quest.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = quest.description,
                        color = TextMutedGrey,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                    
                    Divider(color = DarkGreyBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "REWARDS:",
                            color = SystemNeonCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "+${quest.xpReward} XP",
                                color = SystemPurple,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+${quest.goldReward}G",
                                color = SystemGold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = quest.statReward,
                                color = SystemNeonCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "👉 TAP CARD TO MANIFEST QUEST SCROLL IN QUESTS TAB",
                        color = TextMutedGrey,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

// Logic flow: queries Gemini REST or falls back locally
suspend fun runGenerator(stats: PlayerStats?): Pair<String, List<QuestOption>> = withContext(Dispatchers.IO) {
    val level = stats?.level ?: 1
    val str = stats?.strength ?: 10
    val agi = stats?.agility ?: 10
    val intel = stats?.intelligence ?: 10
    val vit = stats?.vitality ?: 10
    val sense = stats?.sense ?: 10

    // Check if Gemini API Key exists in BuildConfig
    var apiKey = ""
    try {
        val field = Class.forName("com.example.BuildConfig").getField("GEMINI_API_KEY")
        apiKey = field.get(null) as String
    } catch (e: Exception) {
        // Fallback
    }

    if (apiKey.isNotEmpty() && apiKey != "YOUR_GEMINI_API_KEY") {
        try {
            val prompt = """
                Generate a Shadow Ascension style daily quest scroll for a Level $level player: Strength: $str, Agility: $agi, Intelligence: $intel, Vitality: $vit, Sense: $sense.
                Output JSON conforming to:
                {
                  "coachMessage": "A brief logical system feedback comment about the vanguard stats.",
                  "quests": [
                    { "title": "Main physical training", "description": "Run 5km and stretch", "type": "MAIN", "rank": "C", "xpReward": 120, "goldReward": 50, "statReward": "STR +2" },
                    { "title": "Side mental calibration", "description": "Read 10 pages of book", "type": "SIDE", "rank": "D", "xpReward": 80, "goldReward": 30, "statReward": "INT +1" },
                    { "title": "Bonus meditation", "description": "15m quiet breathing", "type": "BONUS", "rank": "B", "xpReward": 140, "goldReward": 60, "statReward": "PER +2" },
                    { "title": "Emergency high priority", "description": "Slay procrastination task", "type": "EMERGENCY", "rank": "A", "xpReward": 250, "goldReward": 100, "statReward": "ALL +2" }
                  ]
                }
                Provide raw valid JSON. Do not write markdown blocks.
            """.trimIndent()

            val client = OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .build()

            val escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n")
            val requestBodyJson = """
                {
                  "contents": [{
                    "parts": [{
                      "text": "$escapedPrompt"
                    }]
                  }]
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val resString = response.body?.string() ?: ""
                // Quick String extract to bypass parsing dependencies
                if (resString.contains("text")) {
                    val rawText = resString.substringAfter("\"text\": \"").substringBefore("\"")
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                    
                    val coachMsg = rawText.substringAfter("\"coachMessage\": \"").substringBefore("\"")
                    
                    val questsList = mutableListOf<QuestOption>()
                    val parts = rawText.split("\"title\": \"")
                    for (i in 1 until parts.size) {
                        val chunk = parts[i]
                        val qTitle = chunk.substringBefore("\"")
                        val qDesc = chunk.substringAfter("\"description\": \"").substringBefore("\"")
                        val qType = chunk.substringAfter("\"type\": \"").substringBefore("\"")
                        val qRank = chunk.substringAfter("\"rank\": \"").substringBefore("\"")
                        val qXp = chunk.substringAfter("\"xpReward\": ").substringBefore(",").trim().toIntOrNull() ?: 100
                        val qGold = chunk.substringAfter("\"goldReward\": ").substringBefore(",").trim().toIntOrNull() ?: 40
                        val qStat = chunk.substringAfter("\"statReward\": \"").substringBefore("\"")
                        
                        questsList.add(QuestOption(qTitle, qDesc, qType, qRank, qXp, qGold, qStat))
                    }
                    if (questsList.isNotEmpty()) {
                        return@withContext Pair(coachMsg, questsList)
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback
        }
    }

    // --- IMMERSIVE FALLBACK SYSTEM (Highly customized, rule-based) ---
    val localCoachMessage = when {
        str > intel && str > sense -> "SYSTEM OBSERVER: Your physical attributes (Strength: $str) are out-pacing your cognitive development. Calibrating mental missions to stabilize your mana core."
        intel > str -> "SYSTEM OBSERVER: Your cognitive capacity (Intelligence: $intel) exceeds your bodily resilience. Calibrating high-intensity cardiovascular tasks to balance your stats."
        else -> "SYSTEM OBSERVER: Symmetrical growth detected. Initiating balanced daily calibration loop. Clear the active dimensional gates immediately."
    }

    val localQuests = listOf(
        QuestOption(
            title = "Daily Calibration: Complete 40 Push-ups & Squats",
            description = "Maintain basic skeletal density and muscle fiber integrity. Complete in sets of 10-15.",
            type = "MAIN",
            rank = if (level >= 10) "B" else "C",
            xpReward = 150 + (level * 20),
            goldReward = 60 + (level * 10),
            statReward = "STR +2"
        ),
        QuestOption(
            title = "Knowledge Codex: 2 Hours Focused Study",
            description = "Upgrade intellectual capacity. Read technical documentation, code, or revise textbook chapters without distraction.",
            type = "SIDE",
            rank = if (level >= 10) "A" else "C",
            xpReward = 120 + (level * 15),
            goldReward = 50 + (level * 8),
            statReward = "INT +2"
        ),
        QuestOption(
            title = "Mana Regeneration: Hydrate and Stretch",
            description = "Consume 2.5 Liters of water and complete 10 minutes of full-body flexibility stretching.",
            type = "BONUS",
            rank = "D",
            xpReward = 80 + (level * 10),
            goldReward = 30 + (level * 5),
            statReward = "VIT +1"
        ),
        QuestOption(
            title = "Gate Key: Slay the Procrastination Boss",
            description = "Avoid junk food entirely today. Finish the single most difficult task on your pending list before sunset.",
            type = "EMERGENCY",
            rank = if (level >= 15) "S" else "B",
            xpReward = 250 + (level * 30),
            goldReward = 120 + (level * 15),
            statReward = "ALL +1"
        )
    )

    Pair(localCoachMessage, localQuests)
}
