package com.example.idlemmorpg

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.math.floor

class InventoryView(context: Context, private val gameManager: GameManager) : View(context) {

    // 背包配置 - 改為5x5，更大的方格
    private val INVENTORY_COLS = 5
    private val INVENTORY_ROWS = 5
    private val SLOT_SIZE = 140f  // 進一步增大槽位尺寸
    private val SLOT_PADDING = 8f

    // 區域配置
    private var inventoryAreaTop = 80f  // 給標題留出更多空間
    private var equipmentAreaTop = 0f
    private var detailAreaTop = 0f
    private var detailAreaHeight = 200f  // 增大詳細信息區域高度

    // 選中的物品
    private var selectedItem: InventoryItem? = null
    private var selectedSlot: Pair<Int, Int>? = null

    // 選中的裝備槽位
    private var selectedEquipmentSlot: EquipmentSlot? = null

    // 背包數據
    private val inventoryItems = Array(INVENTORY_ROWS) { Array<InventoryItem?>(INVENTORY_COLS) { null } }

    // 裝備槽位
    private val equipmentSlots = mutableMapOf<EquipmentSlot, InventoryItem?>()

    // 裝備槽位位置記錄 - 新增這個變量來記錄每個槽位的位置
    private val equipmentSlotPositions = mutableMapOf<EquipmentSlot, RectF>()

    // 繪製用的Paint
    private val slotPaint = Paint().apply {
        color = Color.parseColor("#3C4043")
        style = Paint.Style.FILL
        isAntiAlias = true
        alpha = 150
    }

    private val slotBorderPaint = Paint().apply {
        color = Color.parseColor("#5F6368")
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
        alpha = 180
    }

    private val selectedSlotPaint = Paint().apply {
        color = Color.parseColor("#FFD700")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 54f  // 增大字體
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val smallTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f  // 增大字體
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val countTextPaint = Paint().apply {
        color = Color.parseColor("#FFD700")
        textSize = 32f  // 增大字體
        isAntiAlias = true
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.DEFAULT_BOLD
    }

    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#2C3E50")
        isAntiAlias = true
    }

    private val detailBackgroundPaint = Paint().apply {
        color = Color.parseColor("#34495E")
        isAntiAlias = true
    }

    private val humanoidPaint = Paint().apply {
        color = Color.parseColor("#D4A574")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val humanoidOutlinePaint = Paint().apply {
        color = Color.parseColor("#8B7355")
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    enum class EquipmentSlot(val displayName: String, val emoji: String) {
        HELMET("頭部", "⛑️"),
        CHEST("衣服", "👕"),
        GLOVES("護手", "🧤"),
        WEAPON("武器", "⚔️"),
        LEGS("下身", "👖"),
        BOOTS("鞋子", "👢")
    }

    data class InventoryItem(
        val name: String,
        val emoji: String,
        val count: Int,
        val type: ItemType,
        val data: Any // 可以是Weapon, Armor, 或HealingPotion
    )

    enum class ItemType {
        WEAPON, ARMOR, POTION
    }

    init {
        setBackgroundColor(Color.parseColor("#1A252F"))
        initializeEquipmentSlots()
        loadPlayerItems()
    }

    private fun initializeEquipmentSlots() {
        EquipmentSlot.values().forEach { slot ->
            equipmentSlots[slot] = null
        }
    }

    private fun loadPlayerItems() {
        val player = gameManager.player

        // 清空背包
        for (row in inventoryItems.indices) {
            for (col in inventoryItems[row].indices) {
                inventoryItems[row][col] = null
            }
        }

        // 清空裝備槽
        equipmentSlots.clear()

        var currentRow = 0
        var currentCol = 0

        // 添加药品到背包
        player.potions.forEach { (potion, count) ->
            if (count > 0 && currentRow < INVENTORY_ROWS) {
                inventoryItems[currentRow][currentCol] = InventoryItem(
                    name = potion.name,
                    emoji = getPotionEmoji(potion),
                    count = count,
                    type = ItemType.POTION,
                    data = potion
                )

                currentCol++
                if (currentCol >= INVENTORY_COLS) {
                    currentCol = 0
                    currentRow++
                }
            }
        }

        // 添加库存中的武器到背包
        gameManager.getInventoryWeapons().forEach { weapon ->
            if (currentRow < INVENTORY_ROWS) {
                inventoryItems[currentRow][currentCol] = InventoryItem(
                    name = weapon.name,
                    emoji = "⚔️",
                    count = 1,
                    type = ItemType.WEAPON,
                    data = weapon
                )

                currentCol++
                if (currentCol >= INVENTORY_COLS) {
                    currentCol = 0
                    currentRow++
                }
            }
        }

        // 添加库存中的防具到背包
        gameManager.getInventoryArmors().forEach { armor ->
            if (currentRow < INVENTORY_ROWS) {
                inventoryItems[currentRow][currentCol] = InventoryItem(
                    name = armor.name,
                    emoji = "🛡️",
                    count = 1,
                    type = ItemType.ARMOR,
                    data = armor
                )

                currentCol++
                if (currentCol >= INVENTORY_COLS) {
                    currentCol = 0
                    currentRow++
                }
            }
        }

        // 添加当前装备到装备槽
        if (player.weaponAttack > 0) {
            val weapon = UIHelpers.GameData.WEAPONS.find { it.attack == player.weaponAttack }
            weapon?.let {
                equipmentSlots[EquipmentSlot.WEAPON] = InventoryItem(
                    name = it.name,
                    emoji = "⚔️",
                    count = 1,
                    type = ItemType.WEAPON,
                    data = it
                )
            }
        }

        if (player.armorDefense > 0) {
            val armor = UIHelpers.GameData.ARMORS.find { it.defense == player.armorDefense }
            armor?.let {
                equipmentSlots[EquipmentSlot.CHEST] = InventoryItem(
                    name = it.name,
                    emoji = "🛡️",
                    count = 1,
                    type = ItemType.ARMOR,
                    data = it
                )
            }
        }

        invalidate()
    }

    private fun getPotionEmoji(potion: HealingPotion): String {
        return when {
            potion.name.contains("小") -> "🧪"
            potion.name.contains("中") -> "💉"
            potion.name.contains("大") -> "🍶"
            potion.name.contains("超級") -> "💎"
            potion.name.contains("神級") -> "⭐"
            else -> "🧪"
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val padding = 30f
        inventoryAreaTop = padding + 90f  // 為標題留出空間
        detailAreaTop = inventoryAreaTop + (INVENTORY_ROWS * (SLOT_SIZE + SLOT_PADDING)) + 30f
        equipmentAreaTop = detailAreaTop + detailAreaHeight + 70f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 繪製背景
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // 繪製背包區域
        drawInventoryArea(canvas)

        // 繪製詳細信息區域
        drawDetailArea(canvas)

        // 繪製裝備區域
        drawEquipmentArea(canvas)
    }

    private fun drawInventoryArea(canvas: Canvas) {
        val startX = (width - (INVENTORY_COLS * (SLOT_SIZE + SLOT_PADDING) - SLOT_PADDING)) / 2f

        // 繪製背包標題 - 調整位置
        val titlePaint = Paint(textPaint).apply { textSize = 48f }
        canvas.drawText("🎒 背包", width / 2f, inventoryAreaTop - 30f, titlePaint)

        for (row in 0 until INVENTORY_ROWS) {
            for (col in 0 until INVENTORY_COLS) {
                val x = startX + col * (SLOT_SIZE + SLOT_PADDING)
                val y = inventoryAreaTop + row * (SLOT_SIZE + SLOT_PADDING)

                // 繪製槽位背景
                canvas.drawRoundRect(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 8f, 8f, slotPaint)

                // 繪製槽位邊框
                val borderPaint = if (selectedSlot?.first == row && selectedSlot?.second == col) {
                    selectedSlotPaint
                } else {
                    slotBorderPaint
                }
                canvas.drawRoundRect(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 8f, 8f, borderPaint)

                // 繪製物品
                val item = inventoryItems[row][col]
                if (item != null) {
                    // 繪製物品圖標
                    canvas.drawText(
                        item.emoji,
                        x + SLOT_SIZE / 2,
                        y + SLOT_SIZE / 2 + 12f,
                        textPaint
                    )

                    // 繪製數量
                    if (item.count > 1) {
                        canvas.drawText(
                            item.count.toString(),
                            x + SLOT_SIZE - 8f,
                            y + SLOT_SIZE - 8f,
                            countTextPaint
                        )
                    }
                }
            }
        }
    }

    private fun drawDetailArea(canvas: Canvas) {
        val padding = 30f  // 增大內邊距
        val detailX = padding
        val detailY = detailAreaTop
        val detailWidth = width - 2 * padding

        // 繪製詳細信息背景
        canvas.drawRoundRect(
            detailX, detailY,
            detailX + detailWidth, detailY + detailAreaHeight,
            15f, 15f, detailBackgroundPaint
        )

        // 繪製詳細信息
        if (selectedItem != null) {
            val item = selectedItem!!
            val centerX = detailX + detailWidth / 2
            val textY = detailY + 55f  // 調整文字位置

            // 物品圖標和名稱
            val iconPaint = Paint(textPaint).apply { textSize = 60f }  // 更大圖標
            canvas.drawText(item.emoji, centerX - 100f, textY + 5f, iconPaint)

            val namePaint = Paint(textPaint).apply { textSize = 36f }  // 更大名稱
            canvas.drawText(item.name, centerX + 60f, textY, namePaint)

            // 物品詳細信息
            val details = when (item.type) {
                ItemType.WEAPON -> {
                    val weapon = item.data as Weapon
                    "攻擊力: +${weapon.attack} | 價格: ${weapon.price}💰"
                }
                ItemType.ARMOR -> {
                    val armor = item.data as Armor
                    "防禦力: +${armor.defense} | 價格: ${armor.price}💰"
                }
                ItemType.POTION -> {
                    val potion = item.data as HealingPotion
                    "回復: ${potion.healAmount}HP | 數量: ${item.count}"
                }
            }

            val detailPaint = Paint(smallTextPaint).apply { textSize = 36f }  // 更大詳細信息
            canvas.drawText(details, centerX, textY + 60f, detailPaint)

            // 操作提示
            val actionHint = when (item.type) {
                ItemType.WEAPON -> "點擊右側武器槽位裝備，或再次點擊自動裝備"
                ItemType.ARMOR -> "點擊身體部位裝備，或再次點擊自動裝備"
                ItemType.POTION -> "點擊此物品使用藥品"
            }

            val hintPaint = Paint(smallTextPaint).apply { textSize = 32f }  // 更大提示文字
            canvas.drawText(actionHint, centerX, textY + 100f, hintPaint)

        } else {
            val hintPaint = Paint(smallTextPaint).apply { textSize = 28f }
            canvas.drawText(
                "",
                detailX + detailWidth / 2,
                detailY + detailAreaHeight / 2,
                hintPaint
            )
        }
    }

    private fun drawEquipmentArea(canvas: Canvas) {
        val centerX = width / 2f
        val humanoidY = equipmentAreaTop + 60f  // 增加間距
        val humanoidWidth = 360f  // 人形放大兩倍（原180f）
        val humanoidHeight = 600f  // 人形放大兩倍（原300f）

        // 繪製裝備區域標題
        val titlePaint = Paint(textPaint).apply { textSize = 52f }
        canvas.drawText("⚔️ 裝備", centerX, equipmentAreaTop + 20f, titlePaint)

        // 繪製人形圖
        drawHumanoid(canvas, centerX, humanoidY, humanoidWidth, humanoidHeight)

        // 繪製裝備槽位
        drawEquipmentSlots(canvas, centerX, humanoidY, humanoidWidth, humanoidHeight)
    }

    private fun drawHumanoid(canvas: Canvas, centerX: Float, y: Float, width: Float, height: Float) {
        val left = centerX - width / 2
        val right = centerX + width / 2

        // 頭部
        val headRadius = width / 6
        canvas.drawCircle(centerX, y + headRadius + 10f, headRadius, humanoidPaint)
        canvas.drawCircle(centerX, y + headRadius + 10f, headRadius, humanoidOutlinePaint)

        // 身體
        val bodyTop = y + headRadius * 2 + 20f
        val bodyBottom = bodyTop + height * 0.5f
        canvas.drawRoundRect(
            left + width * 0.2f, bodyTop,
            right - width * 0.2f, bodyBottom,
            8f, 8f, humanoidPaint
        )
        canvas.drawRoundRect(
            left + width * 0.2f, bodyTop,
            right - width * 0.2f, bodyBottom,
            8f, 8f, humanoidOutlinePaint
        )

        // 手臂
        val armWidth = width * 0.15f
        val armHeight = height * 0.4f
        // 左臂
        canvas.drawRoundRect(
            left, bodyTop + 10f,
            left + armWidth, bodyTop + armHeight,
            6f, 6f, humanoidPaint
        )
        canvas.drawRoundRect(
            left, bodyTop + 10f,
            left + armWidth, bodyTop + armHeight,
            6f, 6f, humanoidOutlinePaint
        )
        // 右臂
        canvas.drawRoundRect(
            right - armWidth, bodyTop + 10f,
            right, bodyTop + armHeight,
            6f, 6f, humanoidPaint
        )
        canvas.drawRoundRect(
            right - armWidth, bodyTop + 10f,
            right, bodyTop + armHeight,
            6f, 6f, humanoidOutlinePaint
        )

        // 腿部
        val legWidth = width * 0.18f
        val legHeight = height * 0.4f
        val legTop = bodyBottom
        // 左腿
        canvas.drawRoundRect(
            centerX - legWidth - 5f, legTop,
            centerX - 5f, legTop + legHeight,
            6f, 6f, humanoidPaint
        )
        canvas.drawRoundRect(
            centerX - legWidth - 5f, legTop,
            centerX - 5f, legTop + legHeight,
            6f, 6f, humanoidOutlinePaint
        )
        // 右腿
        canvas.drawRoundRect(
            centerX + 5f, legTop,
            centerX + legWidth + 5f, legTop + legHeight,
            6f, 6f, humanoidPaint
        )
        canvas.drawRoundRect(
            centerX + 5f, legTop,
            centerX + legWidth + 5f, legTop + legHeight,
            6f, 6f, humanoidOutlinePaint
        )
    }

    private fun drawEquipmentSlots(canvas: Canvas, centerX: Float, humanoidY: Float, width: Float, height: Float) {
        val slotSize = SLOT_SIZE * 1.2f  // 槽位也放大

        val slots = mapOf(
            EquipmentSlot.HELMET to Pair(centerX - slotSize/2, humanoidY),
            EquipmentSlot.CHEST to Pair(centerX - slotSize/2, humanoidY + height * 0.5f - slotSize/2),
            EquipmentSlot.GLOVES to Pair(centerX - width/2 - slotSize - 20f, humanoidY + height * 0.6f - slotSize/2),
            EquipmentSlot.WEAPON to Pair(centerX + width/2 + 20f, humanoidY + height * 0.6f - slotSize/2),
            EquipmentSlot.LEGS to Pair(centerX - slotSize/2, humanoidY + height * 0.8f - slotSize/2),
            EquipmentSlot.BOOTS to Pair(centerX - slotSize/2, humanoidY + height)
        )

        // 清空並重新記錄槽位位置
        equipmentSlotPositions.clear()

        slots.forEach { (slot, position) ->
            val x = position.first
            val y = position.second

            // 記錄槽位位置用於點擊檢測
            equipmentSlotPositions[slot] = RectF(x, y, x + slotSize, y + slotSize)

            // 繪製槽位背景
            canvas.drawRoundRect(x, y, x + slotSize, y + slotSize, 10f, 10f, slotPaint)

            // 繪製槽位邊框 - 選中的裝備槽位顯示金色邊框
            val borderPaint = if (selectedEquipmentSlot == slot) {
                selectedSlotPaint
            } else {
                slotBorderPaint
            }
            canvas.drawRoundRect(x, y, x + slotSize, y + slotSize, 10f, 10f, borderPaint)

            // 繪製裝備或空槽位標識
            val item = equipmentSlots[slot]
            if (item != null) {
                // 繪製裝備圖標
                val iconPaint = Paint(textPaint).apply { textSize = 48f }  // 更大圖標
                canvas.drawText(
                    item.emoji,
                    x + slotSize / 2,
                    y + slotSize / 2 + 16f,
                    iconPaint
                )
            } else {
                // 繪製空槽位標識
                val labelPaint = Paint(smallTextPaint).apply {
                    textSize = 36f  // 更大文字
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(
                    slot.displayName,
                    x + slotSize / 2,
                    y + slotSize / 2 + 8f,
                    labelPaint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            // 檢查是否點擊了背包槽位
            val startX = (width - (INVENTORY_COLS * (SLOT_SIZE + SLOT_PADDING) - SLOT_PADDING)) / 2f

            if (y >= inventoryAreaTop && y <= inventoryAreaTop + INVENTORY_ROWS * (SLOT_SIZE + SLOT_PADDING)) {
                val col = floor((x - startX) / (SLOT_SIZE + SLOT_PADDING)).toInt()
                val row = floor((y - inventoryAreaTop) / (SLOT_SIZE + SLOT_PADDING)).toInt()

                if (row in 0 until INVENTORY_ROWS && col in 0 until INVENTORY_COLS) {
                    val item = inventoryItems[row][col]
                    if (item != null) {
                        // 如果点击的是已选中的物品
                        if (selectedItem == item) {
                            when (item.type) {
                                ItemType.POTION -> {
                                    // 药品直接使用
                                    usePotion(item)
                                }
                                ItemType.WEAPON -> {
                                    // 武器自动装备到武器槽位
                                    autoEquipWeapon(item)
                                }
                                ItemType.ARMOR -> {
                                    // 防具自动装备到胸部槽位
                                    autoEquipArmor(item)
                                }
                            }
                        } else {
                            // 第一次点击，选中物品
                            selectedItem = item
                            selectedSlot = Pair(row, col)
                            // 清除装备槽位选中状态
                            selectedEquipmentSlot = null
                            invalidate()
                        }
                    } else {
                        // 点击空槽位，清除选中状态
                        selectedItem = null
                        selectedSlot = null
                        selectedEquipmentSlot = null
                        invalidate()
                    }
                }
                return true
            }

            // 檢查是否點擊了詳細信息區域
            val padding = 20f
            val detailX = padding
            val detailY = detailAreaTop
            val detailWidth = width - 2 * padding

            if (x >= detailX && x <= detailX + detailWidth &&
                y >= detailY && y <= detailY + detailAreaHeight) {
                selectedItem?.let { item ->
                    if (item.type == ItemType.POTION) {
                        usePotion(item)
                    }
                }
                return true
            }

            // 檢查是否點擊了裝備槽位 - 修改這部分邏輯
            equipmentSlotPositions.forEach { (slot, rect) ->
                if (rect.contains(x, y)) {
                    handleEquipmentSlotClick(slot)
                    return true
                }
            }

            return true
        }
        return super.onTouchEvent(event)
    }

    private fun handleEquipmentSlotClick(slot: EquipmentSlot) {
        val currentItem = equipmentSlots[slot]

        // 顯示點擊反饋 Toast
        val slotMessage = if (currentItem != null) {
            "點擊了 ${slot.displayName}：${currentItem.name}"
        } else {
            "點擊了空的 ${slot.displayName} 槽位"
        }
        Toast.makeText(context, slotMessage, Toast.LENGTH_SHORT).show()

        // 檢查是否點擊的是已選中的裝備槽位
        if (selectedEquipmentSlot == slot && currentItem != null) {
            // 第二次點擊同一個有裝備的槽位 - 卸下裝備
            unequipItem(slot)
            return
        }

        // 如果有背包物品被選中，檢查是否是可裝備的物品
        selectedItem?.let { item ->
            when (item.type) {
                ItemType.WEAPON -> {
                    if (slot == EquipmentSlot.WEAPON) {
                        equipWeapon(item)
                        // 裝備成功後清除選中狀態
                        selectedItem = null
                        selectedSlot = null
                        selectedEquipmentSlot = null
                        return
                    }
                }
                ItemType.ARMOR -> {
                    if (slot == EquipmentSlot.CHEST) {
                        equipArmor(item)
                        // 裝備成功後清除選中狀態
                        selectedItem = null
                        selectedSlot = null
                        selectedEquipmentSlot = null
                        return
                    }
                }
                ItemType.POTION -> {
                    // 藥品不能裝備，直接選擇該槽位的裝備（如果有的話）
                    // 不顯示錯誤訊息，因為這是正常的選擇行為
                }
            }
        }

        // 選擇該槽位的裝備（或空槽位）
        if (currentItem != null) {
            // 選中裝備槽位中的物品
            selectedItem = currentItem
            selectedEquipmentSlot = slot
            // 清除背包選中狀態
            selectedSlot = null
            invalidate()
            Toast.makeText(context, "已選中 ${currentItem.name}，再次點擊可卸下", Toast.LENGTH_SHORT).show()
        } else {
            // 點擊空槽位，只是選中該槽位
            selectedEquipmentSlot = slot
            selectedItem = null
            selectedSlot = null
            invalidate()
        }
    }

    private fun unequipItem(slot: EquipmentSlot) {
        val item = equipmentSlots[slot] ?: return

        // 檢查背包是否有空間
        if (!addItemToInventory(item)) {
            return // 背包已滿，不能卸下
        }

        // 從裝備槽移除
        equipmentSlots[slot] = null

        // 更新玩家屬性
        when (slot) {
            EquipmentSlot.WEAPON -> {
                gameManager.player.weaponAttack = 0
            }
            EquipmentSlot.CHEST -> {
                gameManager.player.armorDefense = 0
            }
            else -> {
                // 其他槽位暫時不處理
            }
        }

        // 清除選中狀態
        selectedItem = null
        selectedEquipmentSlot = null
        selectedSlot = null

        // 更新遊戲狀態
        gameManager.saveGameState()
        refreshDisplay()
        notifyMainActivity()

        Toast.makeText(context, "✅ 卸下 ${item.name} 成功！", Toast.LENGTH_SHORT).show()
    }

    private fun equipWeapon(item: InventoryItem) {
        val weapon = item.data as Weapon

        // 使用GameManager的方法装备武器
        if (gameManager.equipWeaponFromInventory(weapon)) {
            // 从背包移除
            removeItemFromInventory(item)

            // 重新加载物品以反映更改
            loadPlayerItems()

            // 更新游戏状态
            refreshDisplay()
            notifyMainActivity()

            Toast.makeText(context, "✅ 装备${weapon.name}成功！", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "装备武器失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun equipArmor(item: InventoryItem) {
        val armor = item.data as Armor

        // 使用GameManager的方法装备防具
        if (gameManager.equipArmorFromInventory(armor)) {
            // 从背包移除
            removeItemFromInventory(item)

            // 重新加载物品以反映更改
            loadPlayerItems()

            // 更新游戏状态
            refreshDisplay()
            notifyMainActivity()

            Toast.makeText(context, "✅ 装备${armor.name}成功！", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "装备防具失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun usePotion(item: InventoryItem) {
        val potion = item.data as HealingPotion
        val player = gameManager.player

        if (player.currentHp >= player.maxHp) {
            // 血量已滿，顯示提示
            Toast.makeText(context, "❤️ 血量已滿，無需使用藥品", Toast.LENGTH_SHORT).show()
            return
        }

        // 使用藥品
        if (player.usePotion(potion)) {
            // 更新背包中的數量
            selectedSlot?.let { (row, col) ->
                val inventoryItem = inventoryItems[row][col]
                if (inventoryItem != null && inventoryItem.count > 1) {
                    inventoryItems[row][col] = inventoryItem.copy(count = inventoryItem.count - 1)
                } else {
                    inventoryItems[row][col] = null
                    selectedItem = null
                    selectedSlot = null
                }
            }

            // 更新遊戲狀態
            gameManager.saveGameState()
            refreshDisplay()
            notifyMainActivity()

            Toast.makeText(context, "✅ 使用${potion.name}，回復${potion.healAmount}血量", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addItemToInventory(item: InventoryItem): Boolean {
        // 查找空槽位
        for (row in 0 until INVENTORY_ROWS) {
            for (col in 0 until INVENTORY_COLS) {
                if (inventoryItems[row][col] == null) {
                    inventoryItems[row][col] = item
                    return true
                }
            }
        }
        Toast.makeText(context, "背包已滿！", Toast.LENGTH_SHORT).show()
        return false // 背包已滿
    }

    private fun removeItemFromInventory(item: InventoryItem) {
        selectedSlot?.let { (row, col) ->
            if (inventoryItems[row][col] == item) {
                inventoryItems[row][col] = null
                selectedItem = null
                selectedSlot = null
            }
        }
    }

    private fun autoEquipWeapon(item: InventoryItem) {
        val weapon = item.data as Weapon

        // 使用GameManager的方法装备武器
        if (gameManager.equipWeaponFromInventory(weapon)) {
            // 从背包移除
            removeItemFromInventory(item)

            // 清除选中状态
            selectedItem = null
            selectedSlot = null
            selectedEquipmentSlot = null

            // 重新加载物品以反映更改
            loadPlayerItems()

            // 更新游戏状态
            refreshDisplay()
            notifyMainActivity()

            Toast.makeText(context, "✅ 自动装备${weapon.name}成功！", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "装备武器失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun autoEquipArmor(item: InventoryItem) {
        val armor = item.data as Armor

        // 使用GameManager的方法装备防具
        if (gameManager.equipArmorFromInventory(armor)) {
            // 从背包移除
            removeItemFromInventory(item)

            // 清除选中状态
            selectedItem = null
            selectedSlot = null
            selectedEquipmentSlot = null

            // 重新加载物品以反映更改
            loadPlayerItems()

            // 更新游戏状态
            refreshDisplay()
            notifyMainActivity()

            Toast.makeText(context, "✅ 自动装备${armor.name}成功！", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "装备防具失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshDisplay() {
        invalidate()
    }

    private fun notifyMainActivity() {
        val ctx = context
        if (ctx is MainActivity) {
            ctx.updatePlayerStats()
        }
    }

    fun refreshInventory() {
        loadPlayerItems()
    }
}