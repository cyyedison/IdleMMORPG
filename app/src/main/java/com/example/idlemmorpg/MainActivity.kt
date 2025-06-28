package com.example.idlemmorpg

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color
import android.view.Gravity

class MainActivity : AppCompatActivity() {
    private lateinit var gameManager: GameManager
    private lateinit var currentLocationText: TextView
    private lateinit var playerStatsText: TextView
    private lateinit var actionArea: LinearLayout

    // 底部導航按鈕（現在是LinearLayout）
    private lateinit var btnInventory: LinearLayout
    private lateinit var btnTraining: LinearLayout
    private lateinit var btnShop: LinearLayout
    private lateinit var btnSettings: LinearLayout

    // 彈出菜單相關
    private lateinit var overlayContainer: FrameLayout
    private lateinit var popupTitle: TextView
    private lateinit var popupRecyclerView: RecyclerView
    private lateinit var btnClosePopup: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 隱藏標題欄和狀態欄，實現全屏效果
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        setContentView(R.layout.activity_main)

        gameManager = GameManager(this)

        initViews()
        setupBottomNavigation()
        updateUI()

        // 預設顯示主城
        setupMainCityUI()
    }

    private fun initViews() {
        currentLocationText = findViewById(R.id.currentLocationText)
        playerStatsText = findViewById(R.id.playerStatsText)
        actionArea = findViewById(R.id.actionArea)

        // 底部導航
        btnInventory = findViewById(R.id.btnInventory)
        btnTraining = findViewById(R.id.btnTraining)
        btnShop = findViewById(R.id.btnShop)
        btnSettings = findViewById(R.id.btnSettings)

        // 彈出菜單
        overlayContainer = findViewById(R.id.overlayContainer)
        popupTitle = findViewById(R.id.popupTitle)
        popupRecyclerView = findViewById(R.id.popupRecyclerView)
        btnClosePopup = findViewById(R.id.btnClosePopup)

        popupRecyclerView.layoutManager = LinearLayoutManager(this)

        // 關閉彈出菜單
        btnClosePopup.setOnClickListener { hidePopupMenu() }
        overlayContainer.setOnClickListener { hidePopupMenu() }
    }

    private fun setupBottomNavigation() {
        // 背包按鈕
        btnInventory.setOnClickListener {
            updateButtonSelection(btnInventory)
            gameManager.changeLocation("inventory")
            updateUI()
            hidePopupMenu()
        }

        // 練功樓按鈕 - 顯示彈出菜單
        btnTraining.setOnClickListener {
            updateButtonSelection(btnTraining)
            showTrainingPopup()
        }

        // 商店按鈕 - 顯示彈出菜單
        btnShop.setOnClickListener {
            updateButtonSelection(btnShop)
            showShopPopup()
        }

        // 設定按鈕
        btnSettings.setOnClickListener {
            updateButtonSelection(btnSettings)
            gameManager.changeLocation("settings")
            updateUI()
            hidePopupMenu()
        }
    }

    private fun updateButtonSelection(selectedButton: View) {
        // 重置所有按鈕狀態
        btnInventory.isSelected = false
        btnTraining.isSelected = false
        btnShop.isSelected = false
        btnSettings.isSelected = false

        // 設置選中狀態
        selectedButton.isSelected = true
    }

    private fun showTrainingPopup() {
        popupTitle.text = "⚔️ 選擇練功樓"

        val trainingItems = listOf(
            PopupMenuItem("⚔️ 練功樓 - 1層", "trainingGround1", "適合新手的史萊姆"),
            PopupMenuItem("⚔️ 練功樓 - 2層", "trainingGround2", "哥布林出沒地"),
            PopupMenuItem("⚔️ 練功樓 - 3層", "trainingGround3", "獸人領域"),
            PopupMenuItem("⚔️ 練功樓 - 4層", "trainingGround4", "巨魔巢穴"),
            PopupMenuItem("⚔️ 練功樓 - 5層", "trainingGround5", "龍族聖地")
        )

        popupRecyclerView.adapter = PopupMenuAdapter(trainingItems) { location ->
            gameManager.changeLocation(location)
            updateUI()
            hidePopupMenu()
        }

        showPopupMenu()
    }

    private fun showShopPopup() {
        popupTitle.text = "🏪 選擇商店"

        val shopItems = listOf(
            PopupMenuItem("🔨 五金鋪", "weaponShop", "購買各種武器"),
            PopupMenuItem("👕 衣服店", "armorShop", "購買防具裝備"),
            PopupMenuItem("🏪 便利店", "convenienceStore", "購買回血藥品")
        )

        popupRecyclerView.adapter = PopupMenuAdapter(shopItems) { location ->
            gameManager.changeLocation(location)
            updateUI()
            hidePopupMenu()
        }

        showPopupMenu()
    }

    private fun showPopupMenu() {
        overlayContainer.visibility = View.VISIBLE
        overlayContainer.alpha = 0f
        overlayContainer.animate()
            .alpha(1f)
            .setDuration(200)
            .start()
    }

    private fun hidePopupMenu() {
        overlayContainer.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                overlayContainer.visibility = View.GONE
            }
            .start()
    }

    private fun updateUI() {
        val player = gameManager.player
        val location = gameManager.currentLocation

        val locationName = when (location) {
            "mainCity" -> "🏰 主城"
            "inventory" -> "🎒 背包"
            "settings" -> "⚙️ 設定"
            "trainingGround1" -> "⚔️ 練功樓 - 1層"
            "trainingGround2" -> "⚔️ 練功樓 - 2層"
            "trainingGround3" -> "⚔️ 練功樓 - 3層"
            "trainingGround4" -> "⚔️ 練功樓 - 4層"
            "trainingGround5" -> "⚔️ 練功樓 - 5層"
            "weaponShop" -> "🔨 五金鋪"
            "armorShop" -> "👕 衣服店"
            "convenienceStore" -> "🏪 便利店"
            else -> location
        }

        currentLocationText.text = "📍 目前位置: $locationName"

        val potionText = if (player.potions.isEmpty()) {
            "無藥品"
        } else {
            player.potions.entries.joinToString("\n") { "${it.key.name}: ${it.value}個" }
        }

        playerStatsText.text = """
            👤 玩家資訊
            🔰 等級: ${player.level}
            ⭐ 經驗: ${player.experience}/${player.experienceToNextLevel}
            ❤️ 血量: ${player.currentHp}/${player.maxHp}
            ⚔️ 攻擊力: ${player.attack} (基礎${player.baseAttack} + 武器${player.weaponAttack})
            🛡️ 防禦力: ${player.defense} (基礎${player.baseDefense} + 防具${player.armorDefense})
            💰 金幣: ${player.gold}
            
            🧪 藥品庫存:
            $potionText
        """.trimIndent()

        setupActionArea()
    }

    private fun setupActionArea() {
        actionArea.removeAllViews()

        when {
            gameManager.currentLocation.startsWith("trainingGround") -> {
                setupTrainingGroundUI()
            }
            gameManager.currentLocation == "weaponShop" -> {
                setupWeaponShopUI()
            }
            gameManager.currentLocation == "armorShop" -> {
                setupArmorShopUI()
            }
            gameManager.currentLocation == "convenienceStore" -> {
                setupConvenienceStoreUI()
            }
            gameManager.currentLocation == "inventory" -> {
                setupInventoryUI()
            }
            gameManager.currentLocation == "settings" -> {
                setupSettingsUI()
            }
            else -> {
                setupMainCityUI()
            }
        }
    }

    private fun setupTrainingGroundUI() {
        val battleView = BattleView(this, gameManager)
        gameManager.setBattleView(battleView)
        actionArea.addView(battleView)

        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val startButton = Button(this).apply {
            text = "🎯 開始自動打怪"
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
            setPadding(20, 15, 20, 15)
            setOnClickListener {
                gameManager.startAutoBattle()
                updateUI()
            }
        }

        val stopButton = Button(this).apply {
            text = "⏹️ 停止打怪"
            setBackgroundColor(Color.parseColor("#F44336"))
            setTextColor(Color.WHITE)
            setPadding(20, 15, 20, 15)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(20, 0, 0, 0)
            }
            setOnClickListener {
                gameManager.stopAutoBattle()
                updateUI()
            }
        }

        buttonLayout.addView(startButton)
        buttonLayout.addView(stopButton)
        actionArea.addView(buttonLayout)
    }

    private fun setupWeaponShopUI() {
        val title = TextView(this).apply {
            text = "⚔️ 五金鋪 - 武器商店 ⚔️"
            textSize = 20f
            setTextColor(Color.parseColor("#8B4513"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        actionArea.addView(title)

        val currentWeaponText = TextView(this).apply {
            text = "目前武器攻擊力: +${gameManager.player.weaponAttack}"
            textSize = 14f
            setTextColor(Color.parseColor("#2E8B57"))
            setPadding(8, 8, 8, 16)
        }
        actionArea.addView(currentWeaponText)

        val weapons = listOf(
            Weapon("🗡️ 鐵劍", 10, 100),
            Weapon("⚔️ 鋼劍", 25, 500),
            Weapon("🗡️ 銀劍", 50, 2000),
            Weapon("⚔️ 金劍", 100, 10000),
            Weapon("🗡️ 神劍", 200, 50000)
        )

        weapons.forEach { weapon ->
            val button = Button(this).apply {
                text = "${weapon.name}\n攻擊+${weapon.attack} - ${weapon.price}💰"
                setBackgroundColor(Color.parseColor("#FFD700"))
                setTextColor(Color.parseColor("#8B4513"))
                setPadding(16, 12, 16, 12)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                setOnClickListener {
                    if (gameManager.buyWeapon(weapon)) {
                        Toast.makeText(this@MainActivity, "✅ 購買${weapon.name}成功！", Toast.LENGTH_SHORT).show()
                        updateUI()
                    } else {
                        Toast.makeText(this@MainActivity, "❌ 金幣不足！還需要${weapon.price - gameManager.player.gold}金幣", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            actionArea.addView(button)
        }
    }

    private fun setupArmorShopUI() {
        val title = TextView(this).apply {
            text = "🛡️ 衣服店 - 防具商店 🛡️"
            textSize = 20f
            setTextColor(Color.parseColor("#4169E1"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        actionArea.addView(title)

        val currentArmorText = TextView(this).apply {
            text = "目前防具防禦力: +${gameManager.player.armorDefense}"
            textSize = 14f
            setTextColor(Color.parseColor("#2E8B57"))
            setPadding(8, 8, 8, 16)
        }
        actionArea.addView(currentArmorText)

        val armors = listOf(
            Armor("👕 布衣", 5, 50),
            Armor("🦺 皮甲", 15, 300),
            Armor("⛓️ 鎖甲", 30, 1500),
            Armor("🛡️ 板甲", 60, 8000),
            Armor("✨ 神甲", 120, 40000)
        )

        armors.forEach { armor ->
            val button = Button(this).apply {
                text = "${armor.name}\n防禦+${armor.defense} - ${armor.price}💰"
                setBackgroundColor(Color.parseColor("#87CEEB"))
                setTextColor(Color.parseColor("#191970"))
                setPadding(16, 12, 16, 12)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                setOnClickListener {
                    if (gameManager.buyArmor(armor)) {
                        Toast.makeText(this@MainActivity, "✅ 購買${armor.name}成功！", Toast.LENGTH_SHORT).show()
                        updateUI()
                    } else {
                        Toast.makeText(this@MainActivity, "❌ 金幣不足！還需要${armor.price - gameManager.player.gold}金幣", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            actionArea.addView(button)
        }
    }

    private fun setupConvenienceStoreUI() {
        val title = TextView(this).apply {
            text = "🏪 便利店 - 回血藥品 🧪"
            textSize = 20f
            setTextColor(Color.parseColor("#228B22"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        actionArea.addView(title)

        val introText = TextView(this).apply {
            text = "💡 自動戰鬥時會自動使用藥品回血哦！"
            textSize = 14f
            setTextColor(Color.parseColor("#FF6347"))
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 16)
            setBackgroundColor(Color.parseColor("#FFF8DC"))
        }
        actionArea.addView(introText)

        val potions = listOf(
            HealingPotion("🧪 小回血藥", 50, 1, "回復50血量的基礎藥品"),
            HealingPotion("💉 中回血藥", 150, 5, "回復150血量的進階藥品"),
            HealingPotion("🍶 大回血藥", 300, 20, "回復300血量的高級藥品"),
            HealingPotion("💎 超級回血藥", 500, 50, "回復500血量的極品藥品"),
            HealingPotion("⭐ 神級回血藥", 1000, 200, "回復1000血量的傳說藥品")
        )

        potions.forEach { potion ->
            val currentCount = gameManager.player.potions[potion] ?: 0

            val potionContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(8, 8, 8, 8)
                setBackgroundColor(Color.parseColor("#F0FFF0"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
            }

            val potionInfo = TextView(this).apply {
                text = "${potion.name}\n${potion.description}\n目前持有: ${currentCount}個"
                textSize = 14f
                setTextColor(Color.parseColor("#2F4F4F"))
                setPadding(8, 8, 8, 8)
            }

            val buttonLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            val buyOneButton = Button(this).apply {
                text = "買1個\n${potion.price}💰"
                setBackgroundColor(Color.parseColor("#98FB98"))
                setTextColor(Color.parseColor("#006400"))
                setPadding(12, 8, 12, 8)
                setOnClickListener {
                    if (gameManager.buyPotion(potion, 1)) {
                        Toast.makeText(this@MainActivity, "✅ 購買1個${potion.name}成功！", Toast.LENGTH_SHORT).show()
                        updateUI()
                    } else {
                        Toast.makeText(this@MainActivity, "❌ 金幣不足！還需要${potion.price - gameManager.player.gold}金幣", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val buyTenButton = Button(this).apply {
                text = "買10個\n${potion.price * 10}💰"
                setBackgroundColor(Color.parseColor("#90EE90"))
                setTextColor(Color.parseColor("#006400"))
                setPadding(12, 8, 12, 8)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 0, 0, 0)
                }
                setOnClickListener {
                    if (gameManager.buyPotion(potion, 10)) {
                        Toast.makeText(this@MainActivity, "✅ 購買10個${potion.name}成功！", Toast.LENGTH_SHORT).show()
                        updateUI()
                    } else {
                        Toast.makeText(this@MainActivity, "❌ 金幣不足！還需要${potion.price * 10 - gameManager.player.gold}金幣", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            buttonLayout.addView(buyOneButton)
            buttonLayout.addView(buyTenButton)

            potionContainer.addView(potionInfo)
            potionContainer.addView(buttonLayout)
            actionArea.addView(potionContainer)
        }
    }

    private fun setupMainCityUI() {
        val welcomeText = TextView(this).apply {
            text = """
                🏰 歡迎來到主城！ 🏰
                
                🎮 這裡是你的冒險起點
                使用下方的導航欄開始你的旅程
                
                💡 遊戲提示：
                • 🎒 背包：查看物品和角色狀態
                • ⚔️ 練功樓：自動打怪升級賺錢
                • 🏪 商店：購買武器、防具和藥品
                • ⚙️ 設定：查看遊戲資訊和說明
                
                🌟 特色功能：
                • 即使關閉遊戲也會繼續戰鬥哦！
                • 智能藥品自動使用系統
                • 多層練功樓等你挑戰
            """.trimIndent()
            textSize = 16f
            setTextColor(Color.parseColor("#2F4F4F"))
            setPadding(16, 16, 16, 20)
            setBackgroundColor(Color.parseColor("#F0F8FF"))
        }
        actionArea.addView(welcomeText)
    }

    private fun setupInventoryUI() {
        val title = TextView(this).apply {
            text = "🎒 背包系統 🎒"
            textSize = 20f
            setTextColor(Color.parseColor("#4A5D7A"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        actionArea.addView(title)

        val inventoryText = TextView(this).apply {
            text = """
                📦 物品欄位
                
                💰 金幣: ${gameManager.player.gold}
                
                🧪 藥品庫存:
                ${if (gameManager.player.potions.isEmpty()) "空空如也" else gameManager.player.potions.entries.joinToString("\n") { "${it.key.name}: ${it.value}個" }}
                
                ⚔️ 目前武器: +${gameManager.player.weaponAttack} 攻擊力
                🛡️ 目前防具: +${gameManager.player.armorDefense} 防禦力
                
                📊 角色狀態:
                • 等級: ${gameManager.player.level}
                • 經驗值: ${gameManager.player.experience}/${gameManager.player.experienceToNextLevel}
                • 血量: ${gameManager.player.currentHp}/${gameManager.player.maxHp}
            """.trimIndent()
            textSize = 16f
            setTextColor(Color.parseColor("#2F4F4F"))
            setPadding(16, 16, 16, 20)
            setBackgroundColor(Color.parseColor("#F8F9FA"))
        }
        actionArea.addView(inventoryText)
    }

    private fun setupSettingsUI() {
        val title = TextView(this).apply {
            text = "⚙️ 遊戲設定 ⚙️"
            textSize = 20f
            setTextColor(Color.parseColor("#4A5D7A"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        actionArea.addView(title)

        val settingsText = TextView(this).apply {
            text = """
                🎮 閒置MMORPG
                版本: 1.0.0
                
                🛠️ 功能說明:
                • 自動戰鬥系統
                • 離線掛機收益
                • 智能藥品使用
                • 裝備升級系統
                
                📱 遊戲特色:
                • 即使關閉遊戲也會繼續戰鬥
                • 多層練功樓挑戰
                • 豐富的裝備商店
                • 便利的藥品系統
                
                💡 小提示:
                • 定期檢查背包和購買裝備
                • 記得購買回血藥品
                • 挑戰更高層練功樓獲得更多經驗
            """.trimIndent()
            textSize = 14f
            setTextColor(Color.parseColor("#2F4F4F"))
            setPadding(16, 16, 16, 20)
            setBackgroundColor(Color.parseColor("#F8F9FA"))
        }
        actionArea.addView(settingsText)
    }

    override fun onDestroy() {
        super.onDestroy()
        gameManager.saveGameState()
    }

    override fun onPause() {
        super.onPause()
        gameManager.saveGameState()
    }

    override fun onResume() {
        super.onResume()
        gameManager.loadGameState()
        updateUI()
    }

    // 新增：供GameManager調用的更新方法
    fun updatePlayerStats() {
        updateUI()
    }
}

// 數據類
data class Player(
    var level: Int = 1,
    var experience: Int = 0,
    var maxHp: Int = 100,
    var currentHp: Int = 100,
    var baseAttack: Int = 10,
    var baseDefense: Int = 5,
    var gold: Int = 0,
    var weaponAttack: Int = 0,
    var armorDefense: Int = 0,
    var potions: MutableMap<HealingPotion, Int> = mutableMapOf()
) {
    val attack: Int get() = baseAttack + weaponAttack
    val defense: Int get() = baseDefense + armorDefense
    val experienceToNextLevel: Int get() = level * 100

    fun gainExperience(exp: Int) {
        experience += exp
        while (experience >= experienceToNextLevel) {
            experience -= experienceToNextLevel
            levelUp()
        }
    }

    private fun levelUp() {
        level++
        val hpIncrease = 20
        maxHp += hpIncrease
        currentHp = maxHp
        baseAttack += 2
        baseDefense += 1
    }

    fun usePotion(potion: HealingPotion): Boolean {
        val count = potions[potion] ?: 0
        if (count > 0) {
            currentHp = minOf(maxHp, currentHp + potion.healAmount)
            potions[potion] = count - 1
            if (potions[potion] == 0) {
                potions.remove(potion)
            }
            return true
        }
        return false
    }

    fun getMostEfficientPotion(): HealingPotion? {
        val missingHp = maxHp - currentHp
        if (missingHp <= 0) return null

        return potions.keys
            .filter { (potions[it] ?: 0) > 0 && it.healAmount >= missingHp }
            .minByOrNull { it.healAmount }
            ?: potions.keys
                .filter { (potions[it] ?: 0) > 0 }
                .maxByOrNull { it.healAmount }
    }

    // 新增：獲取最小的可用藥品
    fun getSmallestAvailablePotion(): HealingPotion? {
        return potions.keys
            .filter { (potions[it] ?: 0) > 0 }
            .minByOrNull { it.healAmount }
    }
}

data class Monster(
    val name: String,
    val level: Int,
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val expReward: Int,
    val goldReward: Int
) {
    var currentHp: Int = hp

    fun reset() {
        currentHp = hp
    }
}

data class Weapon(val name: String, val attack: Int, val price: Int)
data class Armor(val name: String, val defense: Int, val price: Int)
data class MenuItem(val name: String, val location: String)
data class PopupMenuItem(val name: String, val location: String, val description: String)

data class HealingPotion(
    val name: String,
    val healAmount: Int,
    val price: Int,
    val description: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HealingPotion) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

// 遊戲管理器
class GameManager(private val context: Context) {
    val player = Player()
    var currentLocation = "mainCity"
    private var isAutoBattling = false
    private var currentMonster: Monster? = null
    private val handler = Handler(Looper.getMainLooper())
    private var battleRunnable: Runnable? = null
    private var battleView: BattleView? = null

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("game_data", Context.MODE_PRIVATE)

    init {
        loadGameState()
    }

    fun setBattleView(view: BattleView) {
        battleView = view
    }

    fun getCurrentMonster(): Monster? = currentMonster
    fun isAutoBattling(): Boolean = isAutoBattling

    // 添加更新主界面的方法
    private fun updateMainUI() {
        if (context is MainActivity) {
            context.runOnUiThread {
                context.updatePlayerStats()
            }
        }
    }

    fun changeLocation(location: String) {
        stopAutoBattle()
        currentLocation = location

        // 如果進入練功樓，立即創建怪物
        if (location.startsWith("trainingGround")) {
            currentMonster = createMonsterForLocation()
        }

        saveGameState()
    }

    fun startAutoBattle() {
        if (currentLocation.startsWith("trainingGround")) {
            isAutoBattling = true
            // 不在這裡創建怪物，因為在changeLocation時已經創建了
            if (currentMonster == null) {
                currentMonster = createMonsterForLocation()
            }
            startBattleLoop()
        }
    }

    fun stopAutoBattle() {
        isAutoBattling = false
        battleRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun createMonsterForLocation(): Monster {
        val level = currentLocation.last().toString().toInt()
        return when (level) {
            1 -> Monster("史萊姆", 1, 30, 8, 2, 10, 5)
            2 -> Monster("哥布林", 3, 60, 15, 5, 25, 15)
            3 -> Monster("獸人", 5, 120, 25, 10, 50, 30)
            4 -> Monster("巨魔", 8, 200, 40, 15, 100, 60)
            5 -> Monster("龍族", 12, 350, 60, 25, 200, 120)
            else -> Monster("史萊姆", 1, 30, 8, 2, 10, 5)
        }
    }

    private fun startBattleLoop() {
        battleRunnable = object : Runnable {
            override fun run() {
                if (isAutoBattling && currentMonster != null) {
                    performBattleRound()
                    handler.postDelayed(this, 1000) // 每秒一次攻擊
                }
            }
        }
        handler.post(battleRunnable!!)
    }

    private fun performBattleRound() {
        val monster = currentMonster ?: return

        // 玩家攻擊怪物
        val playerDamage = maxOf(1, player.attack - monster.defense)
        monster.currentHp -= playerDamage
        battleView?.showMonsterDamage(playerDamage)

        if (monster.currentHp <= 0) {
            // 怪物死亡
            player.gainExperience(monster.expReward)
            player.gold += monster.goldReward

            // 移除回滿血邏輯，只回復少量血量
            val healAmount = 2
            player.currentHp = minOf(player.maxHp, player.currentHp + healAmount)

            // 生成新怪物
            currentMonster = createMonsterForLocation()

            // 更新主界面（升級、經驗、金幣變化）
            updateMainUI()
        } else {
            // 怪物攻擊玩家
            val monsterDamage = maxOf(1, monster.attack - player.defense)
            player.currentHp -= monsterDamage

            // 自動使用藥品 - 修正邏輯
            val missingHp = player.maxHp - player.currentHp
            val smallestPotion = player.getSmallestAvailablePotion()

            if (smallestPotion != null && missingHp >= smallestPotion.healAmount) {
                val oldHp = player.currentHp
                if (player.usePotion(smallestPotion)) {
                    // 使用藥品後更新主界面
                    updateMainUI()
                }
            }

            if (player.currentHp <= 0) {
                // 玩家死亡，停止戰鬥
                player.currentHp = 1
                stopAutoBattle()
            }
        }

        saveGameState()
    }

    fun buyWeapon(weapon: Weapon): Boolean {
        if (player.gold >= weapon.price) {
            player.gold -= weapon.price
            player.weaponAttack = weapon.attack
            saveGameState()
            return true
        }
        return false
    }

    fun buyArmor(armor: Armor): Boolean {
        if (player.gold >= armor.price) {
            player.gold -= armor.price
            player.armorDefense = armor.defense
            saveGameState()
            return true
        }
        return false
    }

    fun buyPotion(potion: HealingPotion, quantity: Int): Boolean {
        val totalCost = potion.price * quantity
        if (player.gold >= totalCost) {
            player.gold -= totalCost
            val currentCount = player.potions[potion] ?: 0
            player.potions[potion] = currentCount + quantity
            saveGameState()
            return true
        }
        return false
    }

    fun saveGameState() {
        with(sharedPrefs.edit()) {
            putInt("level", player.level)
            putInt("experience", player.experience)
            putInt("maxHp", player.maxHp)
            putInt("currentHp", player.currentHp)
            putInt("baseAttack", player.baseAttack)
            putInt("baseDefense", player.baseDefense)
            putInt("gold", player.gold)
            putInt("weaponAttack", player.weaponAttack)
            putInt("armorDefense", player.armorDefense)
            putString("currentLocation", currentLocation)
            putLong("lastSaveTime", System.currentTimeMillis())
            putBoolean("wasAutoBattling", isAutoBattling)

            // 保存藥品數據
            val potionData = player.potions.map { "${it.key.name}:${it.value}" }.joinToString(";")
            putString("potions", potionData)

            apply()
        }
    }

    private fun getAllPotions(): List<HealingPotion> {
        return listOf(
            HealingPotion("🧪 小回血藥", 50, 1, "回復50血量的基礎藥品"),
            HealingPotion("💉 中回血藥", 150, 5, "回復150血量的進階藥品"),
            HealingPotion("🍶 大回血藥", 300, 20, "回復300血量的高級藥品"),
            HealingPotion("💎 超級回血藥", 500, 50, "回復500血量的極品藥品"),
            HealingPotion("⭐ 神級回血藥", 1000, 200, "回復1000血量的傳說藥品")
        )
    }

    fun loadGameState() {
        player.level = sharedPrefs.getInt("level", 1)
        player.experience = sharedPrefs.getInt("experience", 0)
        player.maxHp = sharedPrefs.getInt("maxHp", 100)
        player.currentHp = sharedPrefs.getInt("currentHp", 100)
        player.baseAttack = sharedPrefs.getInt("baseAttack", 10)
        player.baseDefense = sharedPrefs.getInt("baseDefense", 5)
        player.gold = sharedPrefs.getInt("gold", 0)
        player.weaponAttack = sharedPrefs.getInt("weaponAttack", 0)
        player.armorDefense = sharedPrefs.getInt("armorDefense", 0)
        currentLocation = sharedPrefs.getString("currentLocation", "mainCity") ?: "mainCity"

        // 加載藥品數據
        val potionData = sharedPrefs.getString("potions", "") ?: ""
        player.potions.clear()
        if (potionData.isNotEmpty()) {
            val allPotions = getAllPotions()
            potionData.split(";").forEach { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val potionName = parts[0]
                    val count = parts[1].toIntOrNull() ?: 0
                    val potion = allPotions.find { it.name == potionName }
                    if (potion != null && count > 0) {
                        player.potions[potion] = count
                    }
                }
            }
        }

        // 處理離線經驗
        val lastSaveTime = sharedPrefs.getLong("lastSaveTime", System.currentTimeMillis())
        val wasAutoBattling = sharedPrefs.getBoolean("wasAutoBattling", false)

        if (wasAutoBattling) {
            calculateOfflineProgress(lastSaveTime)
        }
    }

    private fun calculateOfflineProgress(lastSaveTime: Long) {
        val offlineTime = (System.currentTimeMillis() - lastSaveTime) / 1000 // 秒
        val battleRounds = offlineTime / 1 // 每秒一次戰鬥

        if (battleRounds > 0) {
            val monster = createMonsterForLocation()
            val playerDamage = maxOf(1, player.attack - monster.defense)
            val battlesPerMonster = (monster.hp + playerDamage - 1) / playerDamage

            val monstersKilled = (battleRounds / battlesPerMonster).toInt()
            val expGained = monstersKilled * monster.expReward
            val goldGained = monstersKilled * monster.goldReward

            player.gainExperience(expGained)
            player.gold += goldGained

            // 顯示離線收益
            if (context is MainActivity) {
                Toast.makeText(context,
                    "🌙 離線收益：擊殺 $monstersKilled 隻怪物，獲得 $expGained 經驗，$goldGained 金幣",
                    Toast.LENGTH_LONG).show()
            }
        }
    }
}

// 戰鬥視圖
class BattleView(context: Context, private val gameManager: GameManager) : LinearLayout(context) {
    private val battleText: TextView
    private val playerStatusText: TextView
    private val monsterStatusText: TextView
    private val damageText: TextView
    private val playerAnimationView: PlayerAnimationView
    private val monsterAnimationView: MonsterAnimationView
    private val battleContainer: LinearLayout
    private val handler = Handler(Looper.getMainLooper())

    init {
        orientation = VERTICAL
        setPadding(16, 16, 16, 16)
        setBackgroundColor(Color.parseColor("#F0F8FF"))

        // 標題
        battleText = TextView(context).apply {
            text = "⚔️ 戰鬥區域 ⚔️"
            textSize = 20f
            setTextColor(Color.parseColor("#2E8B57"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }

        // 創建戰鬥容器（水平排列玩家和怪物）
        battleContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                300
            )
        }

        // 玩家動畫視圖
        playerAnimationView = PlayerAnimationView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        }

        // 怪物動畫視圖
        monsterAnimationView = MonsterAnimationView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        }

        battleContainer.addView(playerAnimationView)
        battleContainer.addView(monsterAnimationView)

        // 玩家狀態
        playerStatusText = TextView(context).apply {
            text = "🛡️ 玩家狀態"
            textSize = 14f
            setTextColor(Color.parseColor("#1E90FF"))
            setPadding(8, 8, 8, 8)
            setBackgroundColor(Color.parseColor("#E6F3FF"))
        }

        // 怪物狀態
        monsterStatusText = TextView(context).apply {
            text = "👹 怪物狀態"
            textSize = 14f
            setTextColor(Color.parseColor("#DC143C"))
            setPadding(8, 8, 8, 8)
            setBackgroundColor(Color.parseColor("#FFE6E6"))
        }

        // 戰鬥狀態顯示
        damageText = TextView(context).apply {
            text = "💥 戰鬥狀態"
            textSize = 16f
            setTextColor(Color.parseColor("#FF4500"))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        }

        addView(battleText)
        addView(battleContainer)
        addView(playerStatusText)
        addView(monsterStatusText)
        addView(damageText)

        // 定期更新戰鬥狀態
        updateBattleStatus()
        startUpdateLoop()
    }

    private var isAnimating = false

    private fun startUpdateLoop() {
        val updateRunnable = object : Runnable {
            override fun run() {
                updateBattleStatus()
                handler.postDelayed(this, 500)
            }
        }
        handler.post(updateRunnable)
    }

    private fun updateBattleStatus() {
        val player = gameManager.player

        playerStatusText.text = """
            🛡️ 玩家狀態
            ❤️ 血量: ${player.currentHp}/${player.maxHp}
            ⚔️ 攻擊: ${player.attack}
            🛡️ 防禦: ${player.defense}
        """.trimIndent()

        // 更新玩家血條
        playerAnimationView.updatePlayer(player)

        // 獲取當前怪物狀態
        val monster = gameManager.getCurrentMonster()
        if (monster != null) {
            monsterStatusText.text = """
                👹 ${monster.name} (Lv.${monster.level})
                ❤️ 血量: ${monster.currentHp}/${monster.hp}
                ⚔️ 攻擊: ${monster.attack}
                🛡️ 防禦: ${monster.defense}
            """.trimIndent()

            // 更新怪物動畫視圖
            monsterAnimationView.updateMonster(monster)
        } else {
            monsterStatusText.text = "👹 怪物狀態\n沒有怪物"
        }

        // 顯示戰鬥狀態和控制動畫
        if (gameManager.isAutoBattling()) {
            damageText.text = "💥 自動戰鬥中..."
            if (!isAnimating) {
                startBattleAnimation()
            }
        } else {
            damageText.text = "⏸️ 戰鬥已停止"
            stopBattleAnimation()
        }
    }

    private fun startBattleAnimation() {
        isAnimating = true
        playerAnimationView.startContinuousAttack()
        monsterAnimationView.startAnimation()
    }

    private fun stopBattleAnimation() {
        isAnimating = false
        playerAnimationView.stopAttackAnimation()
        monsterAnimationView.stopAnimation()
    }

    fun showDamage(damage: Int) {
        playerAnimationView.showDamage(damage)
        playerAnimationView.startAttackAnimation()
    }

    fun showMonsterDamage(damage: Int) {
        monsterAnimationView.showMonsterDamage(damage)
    }
}

// 彈出菜單適配器
class PopupMenuAdapter(
    private val menuItems: List<PopupMenuItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<PopupMenuAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.itemTitle)
        val descriptionText: TextView = view.findViewById(R.id.itemDescription)
        val iconText: TextView = view.findViewById(R.id.itemIcon)
        val container: LinearLayout = view.findViewById(R.id.itemContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.popup_menu_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = menuItems[position]
        holder.titleText.text = item.name
        holder.descriptionText.text = item.description

        // 設置圖標
        holder.iconText.text = when {
            item.location.startsWith("trainingGround") -> "⚔️"
            item.location == "weaponShop" -> "🔨"
            item.location == "armorShop" -> "👕"
            item.location == "convenienceStore" -> "🏪"
            else -> "🏰"
        }

        holder.container.setOnClickListener {
            onItemClick(item.location)
        }
    }

    override fun getItemCount() = menuItems.size
}