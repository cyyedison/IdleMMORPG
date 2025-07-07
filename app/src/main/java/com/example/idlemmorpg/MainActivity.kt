package com.example.idlemmorpg

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.idlemmorpg.network.ServerItemData

class MainActivity : AppCompatActivity() {
    private lateinit var serverGameManager: ServerGameManager

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

    // 背包彈出窗
    private lateinit var inventoryOverlay: FrameLayout

    companion object {
        private const val INVENTORY_VIEW_ID = 999001
    }

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

        serverGameManager = ServerGameManager(this)
        initViews()
        setupNavigation()
        
        // 檢查是否已登入
        checkLoginStatus()
        
        // 開始定期同步
        startPeriodicSync()
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

        // 背包彈出窗
        inventoryOverlay = findViewById(R.id.inventoryOverlay)

        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        popupRecyclerView.layoutManager = LinearLayoutManager(this)

        // 头像点击
        avatarButton.setOnClickListener {
            if (inventoryOverlay.visibility == View.VISIBLE) {
                hideInventoryPopup()
            }
            showPlayerInfoPanel()
        }

        // 弹出菜单关闭
        btnClosePopup.setOnClickListener { hidePopupMenu() }
        overlayContainer.setOnClickListener { hidePopupMenu() }

        // 角色资讯关闭
        btnClosePlayerInfo.setOnClickListener { hidePlayerInfoPanel() }
        playerInfoOverlay.setOnClickListener { hidePlayerInfoPanel() }

        // 背包弹出窗关闭
        inventoryOverlay.setOnClickListener { event ->
            val x = event.x
            val y = event.y
            val inventoryContainer = inventoryOverlay.findViewById<FrameLayout>(R.id.inventoryContainer)
            val location = IntArray(2)
            inventoryContainer.getLocationOnScreen(location)

            if (x < location[0] || x > location[0] + inventoryContainer.width ||
                y < location[1] || y > location[1] + inventoryContainer.height) {
                hideInventoryPopup()
            }
        }
    }
    
    private fun checkLoginStatus() {
        lifecycleScope.launch {
            try {
                if (serverGameManager.needsLogin()) {
                    showLoginScreen()
                } else {
                    serverGameManager.autoSync()
                    updateUI()
                    showMainCity()
                }
            } catch (e: Exception) {
                showLoginScreen()
            }
        }
    }
    
    private fun showLoginScreen() {
        val loginView = createLoginView()
        mainDisplayArea.removeAllViews()
        mainDisplayArea.addView(loginView)
        
        // 隱藏導航按鈕（登入時不需要）
        findViewById<LinearLayout>(R.id.bottomNavigation).visibility = View.GONE
    }
    
    private fun showMainGameUI() {
        // 顯示導航按鈕
        findViewById<LinearLayout>(R.id.bottomNavigation).visibility = View.VISIBLE
        updateUI()
        showMainCity()
    }
    
    private fun createLoginView(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.parseColor("#1A252F"))
            
            val titleText = TextView(this@MainActivity).apply {
                text = "🏰 閒置MMORPG 🏰"
                textSize = 28f
                setTextColor(Color.parseColor("#FFD700"))
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 48)
                typeface = Typeface.DEFAULT_BOLD
            }
            
            val subtitleText = TextView(this@MainActivity).apply {
                text = "🌐 連線到遊戲服務器"
                textSize = 16f
                setTextColor(Color.parseColor("#CCCCCC"))
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 32)
            }
            
            val usernameInput = EditText(this@MainActivity).apply {
                hint = "請輸入用戶名"
                setPadding(16, 16, 16, 16)
                setBackgroundColor(Color.parseColor("#40FFFFFF"))
                setTextColor(Color.WHITE)
                setHintTextColor(Color.parseColor("#CCCCCC"))
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    140
                ).apply { bottomMargin = 16 }
            }
            
            val passwordInput = EditText(this@MainActivity).apply {
                hint = "請輸入密碼"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                setPadding(16, 16, 16, 16)
                setBackgroundColor(Color.parseColor("#40FFFFFF"))
                setTextColor(Color.WHITE)
                setHintTextColor(Color.parseColor("#CCCCCC"))
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    140
                ).apply { bottomMargin = 24 }
            }
            
            val loginButton = Button(this@MainActivity).apply {
                text = "🗡️ 登入遊戲"
                setBackgroundColor(Color.parseColor("#4CAF50"))
                setTextColor(Color.WHITE)
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    160
                ).apply { bottomMargin = 16 }
                
                setOnClickListener {
                    val username = usernameInput.text.toString().trim()
                    val password = passwordInput.text.toString().trim()
                    
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        performLogin(username, password)
                    } else {
                        Toast.makeText(this@MainActivity, "請輸入用戶名和密碼", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            val registerButton = Button(this@MainActivity).apply {
                text = "⚔️ 註冊新帳戶"
                setBackgroundColor(Color.parseColor("#2196F3"))
                setTextColor(Color.WHITE)
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    160
                ).apply { bottomMargin = 24 }
                
                setOnClickListener {
                    val username = usernameInput.text.toString().trim()
                    val password = passwordInput.text.toString().trim()
                    
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        if (password.length < 6) {
                            Toast.makeText(this@MainActivity, "密碼至少需要6個字符", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        performRegister(username, password)
                    } else {
                        Toast.makeText(this@MainActivity, "請輸入用戶名和密碼", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            val connectionText = TextView(this@MainActivity).apply {
                text = "💡 需要網路連接到遊戲服務器"
                textSize = 14f
                setTextColor(Color.parseColor("#888888"))
                gravity = Gravity.CENTER
                setPadding(16, 16, 16, 16)
            }
            
            addView(titleText)
            addView(subtitleText)
            addView(usernameInput)
            addView(passwordInput)
            addView(loginButton)
            addView(registerButton)
            addView(connectionText)
        }
    }
    
    private fun performLogin(username: String, password: String) {
        val loadingDialog = createLoadingDialog("連接服務器中...")
        loadingDialog.show()
        
        lifecycleScope.launch {
            try {
                val result = serverGameManager.login(username, password)
                loadingDialog.dismiss()
                
                if (result.isSuccess) {
                    Toast.makeText(this@MainActivity, "登入成功！歡迎回來", Toast.LENGTH_SHORT).show()
                    showMainGameUI()
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "登入失敗"
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                loadingDialog.dismiss()
                Toast.makeText(this@MainActivity, "網路連接失敗：${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun performRegister(username: String, password: String) {
        val loadingDialog = createLoadingDialog("註冊中...")
        loadingDialog.show()
        
        lifecycleScope.launch {
            try {
                val result = serverGameManager.register(username, password)
                loadingDialog.dismiss()
                
                if (result.isSuccess) {
                    Toast.makeText(this@MainActivity, "註冊成功！請登入", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "註冊失敗"
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                loadingDialog.dismiss()
                Toast.makeText(this@MainActivity, "網路連接失敗：${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun createLoadingDialog(message: String): android.app.AlertDialog {
        val builder = android.app.AlertDialog.Builder(this)
        val view = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            
            val progressBar = ProgressBar(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(64, 64).apply { 
                    rightMargin = 16 
                }
            }
            
            val textView = TextView(this@MainActivity).apply {
                text = message
                textSize = 16f
                setTextColor(Color.BLACK)
            }
            
            addView(progressBar)
            addView(textView)
        }
        
        return builder.setView(view)
            .setCancelable(false)
            .create()
    }
    
    private fun setupNavigation() {
        btnInventory.setOnClickListener {
            if (inventoryOverlay.visibility == View.VISIBLE) {
                hideInventoryPopup()
            } else {
                showInventory()
            }
        }

        btnTraining.setOnClickListener {
            if (inventoryOverlay.visibility == View.VISIBLE) {
                hideInventoryPopup()
            }
            updateButtonSelection(btnTraining)
            showTrainingPopup()
        }

        btnShop.setOnClickListener {
            if (inventoryOverlay.visibility == View.VISIBLE) {
                hideInventoryPopup()
            }
            updateButtonSelection(btnShop)
            showShopPopup()
        }

        btnSettings.setOnClickListener {
            if (inventoryOverlay.visibility == View.VISIBLE) {
                hideInventoryPopup()
            }
            updateButtonSelection(btnSettings)
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
            // 這裡可以添加改變位置的API調用
            updateUI()
            showTrainingGround()
            hidePopupMenu()
        }
        showPopupMenu()
    }

    private fun showShopPopup() {
        popupTitle.text = "🏪 選擇商店"
        popupRecyclerView.adapter = PopupMenuAdapter(UIHelpers.GameData.SHOP_LOCATIONS) { location ->
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
    
    // 修改戰鬥按鈕處理 - 使用服務器API
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
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                
                setOnClickListener {
                    if (!serverGameManager.isLoggedIn) {
                        Toast.makeText(this@MainActivity, "請先登入", Toast.LENGTH_SHORT).show()
                        showLoginScreen()
                        return@setOnClickListener
                    }
                    
                    lifecycleScope.launch {
                        try {
                            val result = serverGameManager.startAutoBattle()
                            if (result.isSuccess) {
                                Toast.makeText(this@MainActivity, "開始自動戰鬥！", Toast.LENGTH_SHORT).show()
                                updateUI()
                                showTrainingGround()
                            } else {
                                Toast.makeText(this@MainActivity, "開始戰鬥失敗：${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "網路錯誤：${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            val stopButton = Button(this@MainActivity).apply {
                text = "⏹️ 停止打怪"
                setBackgroundColor(UIHelpers.Colors.RED)
                setTextColor(UIHelpers.Colors.WHITE)
                setPadding(20, 15, 20, 15)
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(20, 0, 0, 0)
                }
                
                setOnClickListener {
                    if (!serverGameManager.isLoggedIn) {
                        Toast.makeText(this@MainActivity, "請先登入", Toast.LENGTH_SHORT).show()
                        showLoginScreen()
                        return@setOnClickListener
                    }
                    
                    lifecycleScope.launch {
                        try {
                            val result = serverGameManager.stopAutoBattle()
                            if (result.isSuccess) {
                                val rewards = result.getOrNull()!!
                                val message = if (rewards.monstersKilled > 0) {
                                    "停止戰鬥！獲得經驗：${rewards.experience}，金幣：${rewards.gold}，擊殺：${rewards.monstersKilled}隻怪物"
                                } else {
                                    "停止戰鬥！"
                                }
                                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                                updateUI()
                                showTrainingGround()
                            } else {
                                Toast.makeText(this@MainActivity, "停止戰鬥失敗：${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "網路錯誤：${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            addView(startButton)
            addView(stopButton)
        }
    }
    
    // 修改商店功能 - 使用服務器API
    private fun showWeaponShop() {
        clearMainDisplay()
        
        lifecycleScope.launch {
            try {
                val shopItems = serverGameManager.getShopItems()
                if (shopItems.isSuccess) {
                    val weapons = shopItems.getOrNull()?.filter { it.type == "WEAPON" } ?: emptyList()
                    val contentView = createServerShopContent(
                        "⚔️ 五金鋪 - 武器商店 ⚔️",
                        "目前武器攻擊力: +${serverGameManager.player?.weaponAttack ?: 0}",
                        weapons
                    ) { weaponData ->
                        buyItemFromServer(weaponData, 1) {
                            showWeaponShop()
                        }
                    }
                    mainDisplayArea.addView(contentView)
                } else {
                    Toast.makeText(this@MainActivity, "載入商店失敗", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "網路錯誤：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showArmorShop() {
        clearMainDisplay()
        
        lifecycleScope.launch {
            try {
                val shopItems = serverGameManager.getShopItems()
                if (shopItems.isSuccess) {
                    val armors = shopItems.getOrNull()?.filter { it.type == "ARMOR" } ?: emptyList()
                    val contentView = createServerShopContent(
                        "🛡️ 衣服店 - 防具商店 🛡️",
                        "目前防具防禦力: +${serverGameManager.player?.armorDefense ?: 0}",
                        armors
                    ) { armorData ->
                        buyItemFromServer(armorData, 1) {
                            showArmorShop()
                        }
                    }
                    mainDisplayArea.addView(contentView)
                } else {
                    Toast.makeText(this@MainActivity, "載入商店失敗", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "網路錯誤：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showConvenienceStore() {
        clearMainDisplay()
        
        lifecycleScope.launch {
            try {
                val shopItems = serverGameManager.getShopItems()
                if (shopItems.isSuccess) {
                    val potions = shopItems.getOrNull()?.filter { it.type == "POTION" } ?: emptyList()
                    val contentView = createPotionServerShopContent(potions)
                    mainDisplayArea.addView(contentView)
                } else {
                    Toast.makeText(this@MainActivity, "載入商店失敗", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "網路錯誤：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 從服務器購買物品
    private fun buyItemFromServer(itemData: ServerItemData, quantity: Int, onSuccess: () -> Unit) {
        lifecycleScope.launch {
            try {
                val result = serverGameManager.buyItem(itemData.id, quantity)
                if (result.isSuccess) {
                    Toast.makeText(this@MainActivity, "✅ 購買${itemData.name}成功！", Toast.LENGTH_SHORT).show()
                    updateUI()
                    onSuccess()
                } else {
                    Toast.makeText(this@MainActivity, "❌ ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "網路錯誤：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 創建服務器商店內容
    private fun createServerShopContent(
        title: String, 
        currentStatus: String, 
        items: List<ServerItemData>,
        onBuyClick: (ServerItemData) -> Unit
    ): ScrollView {
        return ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            setPadding(16, 16, 16, 16)

            val contentLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            contentLayout.addView(createShopTitle(title))
            contentLayout.addView(createStatusText(currentStatus))

            items.forEach { item ->
                contentLayout.addView(createServerShopButton(item, onBuyClick))
            }

            addView(contentLayout)
        }
    }

    private fun createServerShopButton(
        item: ServerItemData, 
        onBuyClick: (ServerItemData) -> Unit
    ): Button {
        return Button(this).apply {
            when (item.type) {
                "WEAPON" -> {
                    text = "${item.name}\n攻擊+${item.attack} - ${item.price}💰"
                    setBackgroundColor(UIHelpers.Colors.GOLD)
                    setTextColor(UIHelpers.Colors.BROWN)
                }
                "ARMOR" -> {
                    text = "${item.name}\n防禦+${item.defense} - ${item.price}💰"
                    setBackgroundColor(UIHelpers.Colors.LIGHT_BLUE)
                    setTextColor(UIHelpers.Colors.DARK_BLUE)
                }
                "POTION" -> {
                    text = "${item.name}\n回復${item.healAmount}HP - ${item.price}💰"
                    setBackgroundColor(UIHelpers.Colors.LIGHT_GREEN)
                    setTextColor(UIHelpers.Colors.DARK_GREEN)
                }
            }
            setPadding(16, 12, 16, 12)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 8, 0, 8)
            }
            setOnClickListener { 
                if (!serverGameManager.isLoggedIn) {
                    Toast.makeText(this@MainActivity, "請先登入", Toast.LENGTH_SHORT).show()
                    showLoginScreen()
                    return@setOnClickListener
                }
                onBuyClick(item) 
            }
        }
    }

    private fun createPotionServerShopContent(potions: List<ServerItemData>): ScrollView {
        return ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            setPadding(16, 16, 16, 16)

            val contentLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            contentLayout.addView(createShopTitle("🏪 便利店 - 回血藥品 🧪"))
            contentLayout.addView(createPotionIntro())

            potions.forEach { potion ->
                contentLayout.addView(createPotionServerContainer(potion))
            }

            addView(contentLayout)
        }
    }

    private fun createPotionServerContainer(potion: ServerItemData): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 8)
            setBackgroundColor(UIHelpers.Colors.SEMI_TRANSPARENT_GREEN)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 8, 0, 8)
            }

            val potionInfo = TextView(this@MainActivity).apply {
                text = "${potion.name}\n${potion.description}"
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
                    buyItemFromServer(potion, 1) {
                        showConvenienceStore()
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
                    buyItemFromServer(potion, 10) {
                        showConvenienceStore()
                    }
                }
            }

            buttonLayout.addView(buyOneButton)
            buttonLayout.addView(buyTenButton)

            addView(potionInfo)
            addView(buttonLayout)
        }
    }
    
    private fun updateUI() {
        val player = serverGameManager.player
        if (player != null) {
            locationText.text = UIHelpers.getLocationDisplayName("mainCity")

            val healthProgress = (player.currentHp.toFloat() / player.maxHp.toFloat() * 100).toInt()
            healthBar.progress = healthProgress

            val expProgress = (player.experience.toFloat() / player.experienceToNextLevel.toFloat() * 100).toInt()
            expBar.progress = expProgress
        }
    }

    private fun showPlayerInfoPanel() {
        val player = serverGameManager.player
        if (player == null) {
            Toast.makeText(this, "請先登入", Toast.LENGTH_SHORT).show()
            showLoginScreen()
            return
        }
        
        playerInfoText.text = """
            👤 玩家詳細資訊
            
            🔰 等級: ${player.level}
            ⭐ 經驗: ${player.experience}/${player.experienceToNextLevel}
            ❤️ 血量: ${player.currentHp}/${player.maxHp}
            ⚔️ 攻擊力: ${player.attack} (基礎${player.baseAttack} + 武器${player.weaponAttack})
            🛡️ 防禦力: ${player.defense} (基礎${player.baseDefense} + 防具${player.armorDefense})
            💰 金幣: ${player.gold}
            
            🌐 在線狀態: ${if (serverGameManager.isLoggedIn) "已連線" else "離線"}
            🎮 玩家ID: ${serverGameManager.getCurrentPlayerId() ?: "未知"}
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

    private fun showSettings() {
        clearMainDisplay()
        val contentView = createSettingsContent()
        mainDisplayArea.addView(contentView)
    }

    private fun createSettingsContent(): ScrollView {
        return ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            setPadding(16, 16, 16, 16)

            val contentLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            contentLayout.addView(createShopTitle("⚙️ 遊戲設定 ⚙️"))

            val settingsText = TextView(this@MainActivity).apply {
                text = """
                    🎮 閒置MMORPG - 服務器版本
                    版本: 2.0.0
                    
                    🛠️ 功能說明:
                    • 服務器端遊戲邏輯
                    • 防作弊系統
                    • 多人同步遊戲
                    • 雲端資料儲存
                    
                    📱 遊戲特色:
                    • 所有數據都在服務器計算
                    • 真正的多人遊戲體驗
                    • 防止外掛和作弊
                    • 跨設備資料同步
                    
                    🌐 網路狀態: ${if (serverGameManager.isLoggedIn) "已連線" else "未連線"}
                """.trimIndent()
                textSize = 16f
                setTextColor(UIHelpers.Colors.WHITE)
                setPadding(16, 16, 16, 20)
                setBackgroundColor(UIHelpers.Colors.SEMI_TRANSPARENT_GRAY)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = 24
                }
            }

            val logoutButton = Button(this@MainActivity).apply {
                text = "🚪 登出遊戲"
                setBackgroundColor(Color.parseColor("#F44336"))
                setTextColor(Color.WHITE)
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(20, 15, 20, 15)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 160)
                
                setOnClickListener {
                    android.app.AlertDialog.Builder(this@MainActivity)
                        .setTitle("確認登出")
                        .setMessage("確定要登出遊戲嗎？")
                        .setPositiveButton("確定") { _, _ ->
                            serverGameManager.logout()
                            Toast.makeText(this@MainActivity, "已登出", Toast.LENGTH_SHORT).show()
                            showLoginScreen()
                        }
                        .setNegativeButton("取消", null)
                        .show()
                }
            }

            contentLayout.addView(settingsText)
            contentLayout.addView(logoutButton)
            addView(contentLayout)
        }
    }

    // 添加定期同步
    private fun startPeriodicSync() {
        lifecycleScope.launch {
            while (true) {
                delay(30000) // 每30秒同步一次
                try {
                    if (serverGameManager.isLoggedIn) {
                        serverGameManager.syncPlayerData()
                        updateUI()
                    }
                } catch (e: Exception) {
                    // 靜默處理同步錯誤
                }
            }
        }
    }

    fun updatePlayerStats() {
        updateUI()
    }

    // 其他UI輔助方法
    private fun clearMainDisplay() {
        mainDisplayArea.removeAllViews()
    }

    private fun showMainCity() {
        clearMainDisplay()
        val contentView = createScrollableContent(UIHelpers.GameTexts.WELCOME_TEXT)
        mainDisplayArea.addView(contentView)
    }

    private fun showTrainingGround() {
        clearMainDisplay()
        val battleView = BattleView(this, null) // 暫時傳null，因為現在用服務器管理
        
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

    private fun showInventory() {
        showInventoryPopup()
    }

    private fun showInventoryPopup() {
        // 這裡可以實作從服務器獲取庫存的功能
        Toast.makeText(this, "服務器背包功能開發中...", Toast.LENGTH_SHORT).show()
    }

    private fun hideInventoryPopup() {
        inventoryOverlay.animate().alpha(0f).setDuration(200).withEndAction {
            inventoryOverlay.visibility = View.GONE
        }.start()
    }

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

    // 生命週期方法
    override fun onDestroy() {
        super.onDestroy()
        // 不再需要本地儲存，所有資料都在服務器
    }

    override fun onPause() {
        super.onPause()
        // 不再需要本地儲存，所有資料都在服務器
    }

    override fun onResume() {
        super.onResume()
        // 重新連接時同步資料
        if (serverGameManager.isLoggedIn) {
            lifecycleScope.launch {
                try {
                    serverGameManager.syncPlayerData()
                    updateUI()
                } catch (e: Exception) {
                    // 同步失敗可能需要重新登入
                }
            }
        }
    }

    override fun onBackPressed() {
        when {
            inventoryOverlay.visibility == View.VISIBLE -> {
                hideInventoryPopup()
            }
            playerInfoOverlay.visibility == View.VISIBLE -> {
                hidePlayerInfoPanel()
            }
            overlayContainer.visibility == View.VISIBLE -> {
                hidePopupMenu()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}