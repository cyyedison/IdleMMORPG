package com.example.idlemmorpg

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color
import android.view.Gravity

class MainActivity : AppCompatActivity() {
    private lateinit var gameManager: GameManager

    // UI元素
    private lateinit var avatarButton: ImageView
    private lateinit var healthBar: ProgressBar
    private lateinit var expBar: ProgressBar
    private lateinit var locationText: TextView
    private lateinit var mainDisplayArea: FrameLayout

    // 導航按鈕
    private lateinit var btnInventory: LinearLayout
    private lateinit var btnTraining: LinearLayout
    private lateinit var btnShop: LinearLayout
    private lateinit var btnSettings: LinearLayout

    // 彈出菜單
    private lateinit var overlayContainer: FrameLayout
    private lateinit var popupTitle: TextView
    private lateinit var popupRecyclerView: RecyclerView
    private lateinit var btnClosePopup: LinearLayout

    // 角色資訊彈出窗
    private lateinit var playerInfoOverlay: FrameLayout
    private lateinit var playerInfoText: TextView
    private lateinit var btnClosePlayerInfo: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 全屏設置
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        setContentView(R.layout.activity_main_new)

        gameManager = GameManager(this)
        initViews()
        setupNavigation()
        updateUI()
        showMainCity()
    }

    private fun initViews() {
        // 頂部狀態元素
        avatarButton = findViewById(R.id.avatarButton)
        healthBar = findViewById(R.id.healthBar)
        expBar = findViewById(R.id.expBar)
        locationText = findViewById(R.id.locationText)
        mainDisplayArea = findViewById(R.id.mainDisplayArea)

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

        // 角色資訊
        playerInfoOverlay = findViewById(R.id.playerInfoOverlay)
        playerInfoText = findViewById(R.id.playerInfoText)
        btnClosePlayerInfo = findViewById(R.id.btnClosePlayerInfo)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        popupRecyclerView.layoutManager = LinearLayoutManager(this)

        // 頭像點擊
        avatarButton.setOnClickListener { showPlayerInfoPanel() }

        // 彈出菜單關閉
        btnClosePopup.setOnClickListener { hidePopupMenu() }
        overlayContainer.setOnClickListener { hidePopupMenu() }

        // 角色資訊關閉
        btnClosePlayerInfo.setOnClickListener { hidePlayerInfoPanel() }
        playerInfoOverlay.setOnClickListener { hidePlayerInfoPanel() }
    }

    private fun setupNavigation() {
        btnInventory.setOnClickListener {
            updateButtonSelection(btnInventory)
            gameManager.changeLocation("inventory")
            updateUI()
            showInventory()
            hidePopupMenu()
        }

        btnTraining.setOnClickListener {
            updateButtonSelection(btnTraining)
            showTrainingPopup()
        }

        btnShop.setOnClickListener {
            updateButtonSelection(btnShop)
            showShopPopup()
        }

        btnSettings.setOnClickListener {
            updateButtonSelection(btnSettings)
            gameManager.changeLocation("settings")
            updateUI()
            showSettings()
            hidePopupMenu()
        }
    }

    private fun updateButtonSelection(selectedButton: View) {
        listOf(btnInventory, btnTraining, btnShop, btnSettings).forEach { it.isSelected = false }
        selectedButton.isSelected = true
    }

    private fun showTrainingPopup() {
        popupTitle.text = "⚔️ 選擇練功樓"
        popupRecyclerView.adapter = PopupMenuAdapter(UIHelpers.GameData.TRAINING_LOCATIONS) { location ->
            gameManager.changeLocation(location)
            updateUI()
            showTrainingGround()
            hidePopupMenu()
        }
        showPopupMenu()
    }

    private fun showShopPopup() {
        popupTitle.text = "🏪 選擇商店"
        popupRecyclerView.adapter = PopupMenuAdapter(UIHelpers.GameData.SHOP_LOCATIONS) { location ->
            gameManager.changeLocation(location)
            updateUI()
            when (location) {
                "weaponShop" -> showWeaponShop()
                "armorShop" -> showArmorShop()
                "convenienceStore" -> showConvenienceStore()
            }
            hidePopupMenu()
        }
        showPopupMenu()
    }

    private fun showPopupMenu() {
        overlayContainer.visibility = View.VISIBLE
        overlayContainer.alpha = 0f
        overlayContainer.animate().alpha(1f).setDuration(200).start()
    }

    private fun hidePopupMenu() {
        overlayContainer.animate().alpha(0f).setDuration(200).withEndAction {
            overlayContainer.visibility = View.GONE
        }.start()
    }

    private fun showPlayerInfoPanel() {
        val player = gameManager.player
        val potionText = if (player.potions.isEmpty()) "無藥品"
        else player.potions.entries.joinToString("\n") { "${it.key.name}: ${it.value}個" }

        playerInfoText.text = """
            👤 玩家詳細資訊
            
            🔰 等級: ${player.level}
            ⭐ 經驗: ${player.experience}/${player.experienceToNextLevel}
            ❤️ 血量: ${player.currentHp}/${player.maxHp}
            ⚔️ 攻擊力: ${player.attack} (基礎${player.baseAttack} + 武器${player.weaponAttack})
            🛡️ 防禦力: ${player.defense} (基礎${player.baseDefense} + 防具${player.armorDefense})
            💰 金幣: ${player.gold}
            
            🧪 藥品庫存:
            $potionText
            
            📊 戰鬥統計:
            • 自動戰鬥狀態: ${if (gameManager.isAutoBattling()) "進行中" else "已停止"}
            • 目前位置: ${UIHelpers.getLocationDisplayName(gameManager.currentLocation)}
        """.trimIndent()

        playerInfoOverlay.visibility = View.VISIBLE
        playerInfoOverlay.alpha = 0f
        playerInfoOverlay.animate().alpha(1f).setDuration(200).start()
    }

    private fun hidePlayerInfoPanel() {
        playerInfoOverlay.animate().alpha(0f).setDuration(200).withEndAction {
            playerInfoOverlay.visibility = View.GONE
        }.start()
    }

    private fun updateUI() {
        val player = gameManager.player
        locationText.text = UIHelpers.getLocationDisplayName(gameManager.currentLocation)

        val healthProgress = (player.currentHp.toFloat() / player.maxHp.toFloat() * 100).toInt()
        healthBar.progress = healthProgress

        val expProgress = (player.experience.toFloat() / player.experienceToNextLevel.toFloat() * 100).toInt()
        expBar.progress = expProgress
    }

    private fun clearMainDisplay() {
        mainDisplayArea.removeAllViews()
    }

    // 顯示各個頁面的方法
    private fun showMainCity() {
        clearMainDisplay()
        val contentView = createScrollableContent(UIHelpers.GameTexts.WELCOME_TEXT)
        mainDisplayArea.addView(contentView)
    }

    private fun showTrainingGround() {
        clearMainDisplay()
        val battleView = BattleView(this, gameManager)
        gameManager.setBattleView(battleView)

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            setPadding(8, 8, 8, 8)
        }

        val battleContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        }
        battleContainer.addView(battleView)

        val buttonLayout = createTrainingButtons()

        mainLayout.addView(battleContainer)
        mainLayout.addView(buttonLayout)
        mainDisplayArea.addView(mainLayout)
    }

    private fun createTrainingButtons(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            setPadding(16, 16, 16, 16)

            val startButton = Button(this@MainActivity).apply {
                text = "🎯 開始自動打怪"
                setBackgroundColor(UIHelpers.Colors.GREEN)
                setTextColor(UIHelpers.Colors.WHITE)
                setPadding(20, 15, 20, 15)
                isEnabled = !gameManager.isAutoBattling()
                setOnClickListener {
                    if (!gameManager.isAutoBattling()) {
                        gameManager.startAutoBattle()
                        updateUI()
                        showTrainingGround()
                    }
                }
            }

            val stopButton = Button(this@MainActivity).apply {
                text = "⏹️ 停止打怪"
                setBackgroundColor(UIHelpers.Colors.RED)
                setTextColor(UIHelpers.Colors.WHITE)
                setPadding(20, 15, 20, 15)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(20, 0, 0, 0)
                }
                isEnabled = gameManager.isAutoBattling()
                setOnClickListener {
                    if (gameManager.isAutoBattling()) {
                        gameManager.stopAutoBattle()
                        updateUI()
                        showTrainingGround()
                    }
                }
            }

            addView(startButton)
            addView(stopButton)
        }
    }

    private fun showInventory() {
        clearMainDisplay()
        val contentText = """
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

        val contentView = createTitledContent("🎒 背包系統 🎒", contentText)
        mainDisplayArea.addView(contentView)
    }

    private fun showWeaponShop() {
        clearMainDisplay()
        val contentView = createShopContent(
            "⚔️ 五金鋪 - 武器商店 ⚔️",
            "目前武器攻擊力: +${gameManager.player.weaponAttack}",
            UIHelpers.GameData.WEAPONS
        ) { weapon ->
            if (gameManager.buyWeapon(weapon)) {
                Toast.makeText(this, "✅ 購買${weapon.name}成功！", Toast.LENGTH_SHORT).show()
                updateUI()
                showWeaponShop()
            } else {
                Toast.makeText(this, "❌ 金幣不足！還需要${weapon.price - gameManager.player.gold}金幣", Toast.LENGTH_SHORT).show()
            }
        }
        mainDisplayArea.addView(contentView)
    }

    private fun showArmorShop() {
        clearMainDisplay()
        val contentView = createShopContent(
            "🛡️ 衣服店 - 防具商店 🛡️",
            "目前防具防禦力: +${gameManager.player.armorDefense}",
            UIHelpers.GameData.ARMORS
        ) { armor ->
            if (gameManager.buyArmor(armor)) {
                Toast.makeText(this, "✅ 購買${armor.name}成功！", Toast.LENGTH_SHORT).show()
                updateUI()
                showArmorShop()
            } else {
                Toast.makeText(this, "❌ 金幣不足！還需要${armor.price - gameManager.player.gold}金幣", Toast.LENGTH_SHORT).show()
            }
        }
        mainDisplayArea.addView(contentView)
    }

    private fun showConvenienceStore() {
        clearMainDisplay()
        val contentView = createPotionShopContent()
        mainDisplayArea.addView(contentView)
    }

    private fun showSettings() {
        clearMainDisplay()
        val contentView = createTitledContent("⚙️ 遊戲設定 ⚙️", UIHelpers.GameTexts.SETTINGS_TEXT)
        mainDisplayArea.addView(contentView)
    }

    // UI輔助方法
    private fun createScrollableContent(text: String): ScrollView {
        return ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            setPadding(16, 16, 16, 16)

            addView(TextView(this@MainActivity).apply {
                this.text = text
                textSize = 16f
                setTextColor(UIHelpers.Colors.WHITE)
                setPadding(16, 16, 16, 20)
                setBackgroundColor(UIHelpers.Colors.SEMI_TRANSPARENT_LIGHT_BLUE)
            })
        }
    }

    private fun createTitledContent(title: String, content: String): ScrollView {
        return ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            setPadding(16, 16, 16, 16)

            val contentLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            val titleView = TextView(this@MainActivity).apply {
                text = title
                textSize = 20f
                setTextColor(UIHelpers.Colors.GOLD)
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 20)
                setBackgroundColor(UIHelpers.Colors.SEMI_TRANSPARENT_BLACK)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, 16)
                }
            }

            val contentView = TextView(this@MainActivity).apply {
                text = content
                textSize = 16f
                setTextColor(UIHelpers.Colors.WHITE)
                setPadding(16, 16, 16, 20)
                setBackgroundColor(UIHelpers.Colors.SEMI_TRANSPARENT_GRAY)
            }

            contentLayout.addView(titleView)
            contentLayout.addView(contentView)
            addView(contentLayout)
        }
    }

    private fun <T> createShopContent(title: String, currentStatus: String, items: List<T>, onBuyClick: (T) -> Unit): ScrollView {
        return ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            setPadding(16, 16, 16, 16)

            val contentLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            // 標題
            contentLayout.addView(createShopTitle(title))

            // 當前狀態
            contentLayout.addView(createStatusText(currentStatus))

            // 商品列表
            items.forEach { item ->
                contentLayout.addView(createShopButton(item, onBuyClick))
            }

            addView(contentLayout)
        }
    }

    private fun createShopTitle(title: String): TextView {
        return TextView(this).apply {
            text = title
            textSize = 20f
            setTextColor(UIHelpers.Colors.GOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
            setBackgroundColor(UIHelpers.Colors.SEMI_TRANSPARENT_BLACK)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 16)
            }
        }
    }

    private fun createStatusText(status: String): TextView {
        return TextView(this).apply {
            text = status
            textSize = 14f
            setTextColor(Color.parseColor("#00FF7F"))
            setPadding(8, 8, 8, 16)
            setBackgroundColor(UIHelpers.Colors.SEMI_TRANSPARENT_BLACK)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 16)
            }
        }
    }

    private fun <T> createShopButton(item: T, onBuyClick: (T) -> Unit): Button {
        return Button(this).apply {
            when (item) {
                is Weapon -> {
                    text = "${item.name}\n攻擊+${item.attack} - ${item.price}💰"
                    setBackgroundColor(UIHelpers.Colors.GOLD)
                    setTextColor(UIHelpers.Colors.BROWN)
                }
                is Armor -> {
                    text = "${item.name}\n防禦+${item.defense} - ${item.price}💰"
                    setBackgroundColor(UIHelpers.Colors.LIGHT_BLUE)
                    setTextColor(UIHelpers.Colors.DARK_BLUE)
                }
            }
            setPadding(16, 12, 16, 12)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 8, 0, 8)
            }
            setOnClickListener { onBuyClick(item) }
        }
    }

    private fun createPotionShopContent(): ScrollView {
        return ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            setPadding(16, 16, 16, 16)

            val contentLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            // 標題和介紹
            contentLayout.addView(createShopTitle("🏪 便利店 - 回血藥品 🧪"))
            contentLayout.addView(createPotionIntro())

            // 藥品列表
            UIHelpers.GameData.POTIONS.forEach { potion ->
                contentLayout.addView(createPotionContainer(potion))
            }

            addView(contentLayout)
        }
    }

    private fun createPotionIntro(): TextView {
        return TextView(this).apply {
            text = UIHelpers.GameTexts.POTION_INTRO
            textSize = 14f
            setTextColor(UIHelpers.Colors.TOMATO)
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 16)
            setBackgroundColor(UIHelpers.Colors.SEMI_TRANSPARENT_YELLOW)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 16)
            }
        }
    }

    private fun createPotionContainer(potion: HealingPotion): LinearLayout {
        val currentCount = gameManager.player.potions[potion] ?: 0

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 8)
            setBackgroundColor(UIHelpers.Colors.SEMI_TRANSPARENT_GREEN)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 8, 0, 8)
            }

            val potionInfo = TextView(this@MainActivity).apply {
                text = "${potion.name}\n${potion.description}\n目前持有: ${currentCount}個"
                textSize = 14f
                setTextColor(UIHelpers.Colors.WHITE)
                setPadding(8, 8, 8, 8)
            }

            val buttonLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            val buyOneButton = Button(this@MainActivity).apply {
                text = "買1個\n${potion.price}💰"
                setBackgroundColor(UIHelpers.Colors.LIGHT_GREEN)
                setTextColor(UIHelpers.Colors.DARK_GREEN)
                setPadding(12, 8, 12, 8)
                setOnClickListener {
                    if (gameManager.buyPotion(potion, 1)) {
                        Toast.makeText(this@MainActivity, "✅ 購買1個${potion.name}成功！", Toast.LENGTH_SHORT).show()
                        updateUI()
                        showConvenienceStore()
                    } else {
                        Toast.makeText(this@MainActivity, "❌ 金幣不足！還需要${potion.price - gameManager.player.gold}金幣", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val buyTenButton = Button(this@MainActivity).apply {
                text = "買10個\n${potion.price * 10}💰"
                setBackgroundColor(Color.parseColor("#90EE90"))
                setTextColor(UIHelpers.Colors.DARK_GREEN)
                setPadding(12, 8, 12, 8)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(16, 0, 0, 0)
                }
                setOnClickListener {
                    if (gameManager.buyPotion(potion, 10)) {
                        Toast.makeText(this@MainActivity, "✅ 購買10個${potion.name}成功！", Toast.LENGTH_SHORT).show()
                        updateUI()
                        showConvenienceStore()
                    } else {
                        Toast.makeText(this@MainActivity, "❌ 金幣不足！還需要${potion.price * 10 - gameManager.player.gold}金幣", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            buttonLayout.addView(buyOneButton)
            buttonLayout.addView(buyTenButton)

            addView(potionInfo)
            addView(buttonLayout)
        }
    }

    // 生命週期方法
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

        // 根據當前位置顯示相應內容
        when {
            gameManager.currentLocation.startsWith("trainingGround") -> {
                updateButtonSelection(btnTraining)
                showTrainingGround()
            }
            gameManager.currentLocation == "inventory" -> {
                updateButtonSelection(btnInventory)
                showInventory()
            }
            gameManager.currentLocation == "weaponShop" -> {
                updateButtonSelection(btnShop)
                showWeaponShop()
            }
            gameManager.currentLocation == "armorShop" -> {
                updateButtonSelection(btnShop)
                showArmorShop()
            }
            gameManager.currentLocation == "convenienceStore" -> {
                updateButtonSelection(btnShop)
                showConvenienceStore()
            }
            gameManager.currentLocation == "settings" -> {
                updateButtonSelection(btnSettings)
                showSettings()
            }
            else -> {
                showMainCity()
            }
        }
    }

    fun updatePlayerStats() {
        updateUI()
    }
}