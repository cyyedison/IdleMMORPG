package com.example.idlemmorpg

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class GameManager(private val context: Context) {
    val player = Player()
    var currentLocation = "mainCity"
    private var isAutoBattling = false
    private var currentMonster: Monster? = null
    private val handler = Handler(Looper.getMainLooper())
    private var battleRunnable: Runnable? = null
    private var battleView: BattleView? = null

    // 添加装备库存支持
    private val inventoryWeapons = mutableListOf<Weapon>()
    private val inventoryArmors = mutableListOf<Armor>()

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

    // 装备库存管理方法
    fun addWeaponToInventory(weapon: Weapon) {
        inventoryWeapons.add(weapon)
        saveGameState()
    }

    fun addArmorToInventory(armor: Armor) {
        inventoryArmors.add(armor)
        saveGameState()
    }

    fun removeWeaponFromInventory(weapon: Weapon): Boolean {
        val removed = inventoryWeapons.removeAll { it.name == weapon.name && it.attack == weapon.attack }
        if (removed) saveGameState()
        return removed
    }

    fun removeArmorFromInventory(armor: Armor): Boolean {
        val removed = inventoryArmors.removeAll { it.name == armor.name && it.defense == armor.defense }
        if (removed) saveGameState()
        return removed
    }

    fun getInventoryWeapons(): List<Weapon> = inventoryWeapons.toList()
    fun getInventoryArmors(): List<Armor> = inventoryArmors.toList()

    // 卸下装备的方法
    fun unequipWeapon(): Boolean {
        if (player.weaponAttack > 0) {
            val weapon = UIHelpers.GameData.WEAPONS.find { it.attack == player.weaponAttack }
            weapon?.let {
                // 先將武器攻擊力設為0，再添加到庫存
                player.weaponAttack = 0
                inventoryWeapons.add(it)
                saveGameState()

                // 調試信息
                if (context is MainActivity) {
                    Toast.makeText(context, "調試：卸下武器 ${it.name}，庫存武器數量：${inventoryWeapons.size}", Toast.LENGTH_LONG).show()
                }

                return true
            }
        }
        return false
    }

    fun unequipArmor(): Boolean {
        if (player.armorDefense > 0) {
            val armor = UIHelpers.GameData.ARMORS.find { it.defense == player.armorDefense }
            armor?.let {
                // 先將防具防禦力設為0，再添加到庫存
                player.armorDefense = 0
                inventoryArmors.add(it)
                saveGameState()

                // 調試信息
                if (context is MainActivity) {
                    Toast.makeText(context, "調試：卸下防具 ${it.name}，庫存防具數量：${inventoryArmors.size}", Toast.LENGTH_LONG).show()
                }

                return true
            }
        }
        return false
    }

    // 装备武器的方法
    fun equipWeaponFromInventory(weapon: Weapon): Boolean {
        if (removeWeaponFromInventory(weapon)) {
            // 如果当前有装备武器，先卸下
            if (player.weaponAttack > 0) {
                unequipWeapon()
            }
            player.weaponAttack = weapon.attack
            saveGameState()
            return true
        }
        return false
    }

    // 装备防具的方法
    fun equipArmorFromInventory(armor: Armor): Boolean {
        if (removeArmorFromInventory(armor)) {
            // 如果当前有装备防具，先卸下
            if (player.armorDefense > 0) {
                unequipArmor()
            }
            player.armorDefense = armor.defense
            saveGameState()
            return true
        }
        return false
    }

    private fun updateMainUI() {
        if (context is MainActivity) {
            context.runOnUiThread {
                context.updatePlayerStats()
            }
        }
    }

    fun changeLocation(location: String) {
        if (!location.startsWith("trainingGround") && isAutoBattling) {
            stopAutoBattle()
        }

        currentLocation = location

        if (location.startsWith("trainingGround")) {
            currentMonster = createMonsterForLocation()
        } else {
            currentMonster = null
        }

        saveGameState()
    }

    fun startAutoBattle() {
        if (isAutoBattling) {
            return
        }

        if (currentLocation.startsWith("trainingGround")) {
            isAutoBattling = true
            if (currentMonster == null) {
                currentMonster = createMonsterForLocation()
            }
            startBattleLoop()
            updateMainUI()
        }
    }

    fun stopAutoBattle() {
        if (!isAutoBattling) {
            return
        }

        isAutoBattling = false
        battleRunnable?.let { handler.removeCallbacks(it) }
        battleRunnable = null
        updateMainUI()
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
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(battleRunnable!!)
    }

    private fun performBattleRound() {
        val monster = currentMonster ?: return

        val playerDamage = maxOf(1, player.attack - monster.defense)
        monster.currentHp -= playerDamage
        battleView?.showMonsterDamage(playerDamage)

        if (monster.currentHp <= 0) {
            player.gainExperience(monster.expReward)
            player.gold += monster.goldReward

            val healAmount = 2
            player.currentHp = minOf(player.maxHp, player.currentHp + healAmount)

            currentMonster = createMonsterForLocation()
            updateMainUI()
        } else {
            val monsterDamage = maxOf(1, monster.attack - player.defense)
            player.currentHp -= monsterDamage

            val missingHp = player.maxHp - player.currentHp
            val smallestPotion = player.getSmallestAvailablePotion()

            if (smallestPotion != null && missingHp >= smallestPotion.healAmount) {
                if (player.usePotion(smallestPotion)) {
                    updateMainUI()
                }
            }

            if (player.currentHp <= 0) {
                player.currentHp = 1
                stopAutoBattle()
            }
        }

        saveGameState()
    }

    fun buyWeapon(weapon: Weapon): Boolean {
        if (player.gold >= weapon.price) {
            player.gold -= weapon.price
            // 将武器添加到库存而不是直接装备
            addWeaponToInventory(weapon)
            saveGameState()
            return true
        }
        return false
    }

    fun buyArmor(armor: Armor): Boolean {
        if (player.gold >= armor.price) {
            player.gold -= armor.price
            // 将防具添加到库存而不是直接装备
            addArmorToInventory(armor)
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

            val potionData = player.potions.map { "${it.key.name}:${it.value}" }.joinToString(";")
            putString("potions", potionData)

            // 保存装备库存
            val weaponData = inventoryWeapons.map { "${it.name}:${it.attack}:${it.price}" }.joinToString(";")
            putString("inventoryWeapons", weaponData)

            val armorData = inventoryArmors.map { "${it.name}:${it.defense}:${it.price}" }.joinToString(";")
            putString("inventoryArmors", armorData)

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

        // 加载装备库存
        inventoryWeapons.clear()
        val weaponData = sharedPrefs.getString("inventoryWeapons", "") ?: ""
        if (weaponData.isNotEmpty()) {
            weaponData.split(";").forEach { entry ->
                val parts = entry.split(":")
                if (parts.size == 3) {
                    val name = parts[0]
                    val attack = parts[1].toIntOrNull() ?: 0
                    val price = parts[2].toIntOrNull() ?: 0
                    inventoryWeapons.add(Weapon(name, attack, price))
                }
            }
        }

        inventoryArmors.clear()
        val armorData = sharedPrefs.getString("inventoryArmors", "") ?: ""
        if (armorData.isNotEmpty()) {
            armorData.split(";").forEach { entry ->
                val parts = entry.split(":")
                if (parts.size == 3) {
                    val name = parts[0]
                    val defense = parts[1].toIntOrNull() ?: 0
                    val price = parts[2].toIntOrNull() ?: 0
                    inventoryArmors.add(Armor(name, defense, price))
                }
            }
        }

        val lastSaveTime = sharedPrefs.getLong("lastSaveTime", System.currentTimeMillis())
        val wasAutoBattling = sharedPrefs.getBoolean("wasAutoBattling", false)

        if (wasAutoBattling) {
            calculateOfflineProgress(lastSaveTime)
        }
    }

    private fun calculateOfflineProgress(lastSaveTime: Long) {
        val offlineTime = (System.currentTimeMillis() - lastSaveTime) / 1000
        val battleRounds = offlineTime / 1

        if (battleRounds > 0) {
            val monster = createMonsterForLocation()
            val playerDamage = maxOf(1, player.attack - monster.defense)
            val battlesPerMonster = (monster.hp + playerDamage - 1) / playerDamage

            val monstersKilled = (battleRounds / battlesPerMonster).toInt()
            val expGained = monstersKilled * monster.expReward
            val goldGained = monstersKilled * monster.goldReward

            player.gainExperience(expGained)
            player.gold += goldGained

            if (context is MainActivity) {
                Toast.makeText(context,
                    "🌙 離線收益：擊殺 $monstersKilled 隻怪物，獲得 $expGained 經驗，$goldGained 金幣",
                    Toast.LENGTH_LONG).show()
            }
        }
    }
}