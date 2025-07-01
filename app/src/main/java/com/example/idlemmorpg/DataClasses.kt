package com.example.idlemmorpg

// 玩家數據類
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

    fun getSmallestAvailablePotion(): HealingPotion? {
        return potions.keys
            .filter { (potions[it] ?: 0) > 0 }
            .minByOrNull { it.healAmount }
    }
}

// 怪物數據類
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

// 武器數據類
data class Weapon(val name: String, val attack: Int, val price: Int)

// 防具數據類
data class Armor(val name: String, val defense: Int, val price: Int)

// 菜單項目數據類
data class MenuItem(val name: String, val location: String)

// 彈出菜單項目數據類
data class PopupMenuItem(val name: String, val location: String, val description: String)

// 回血藥品數據類
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