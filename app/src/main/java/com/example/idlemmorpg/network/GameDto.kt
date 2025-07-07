package com.example.idlemmorpg.network

// 請求數據類
data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val password: String)
data class BuyItemRequest(val itemId: Int, val quantity: Int)
data class UseItemRequest(val itemId: Int, val quantity: Int = 1)

// 響應數據類
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val playerId: Int? = null,
    val token: String? = null
)

data class RegisterResponse(
    val success: Boolean,
    val message: String
)

data class ServerPlayerData(
    val id: Int,
    val username: String,
    val level: Int,
    val experience: Long,
    val experienceToNextLevel: Long,
    val maxHp: Int,
    val currentHp: Int,
    val baseAttack: Int,
    val baseDefense: Int,
    val totalAttack: Int,
    val totalDefense: Int,
    val gold: Long,
    val weaponAttack: Int,
    val armorDefense: Int,
    val currentLocation: String,
    val isAutoBattling: Boolean,
    val lastActivityTime: String,
    val lastBattleTime: String
)

data class ServerItemData(
    val id: Int,
    val name: String,
    val type: String, // "WEAPON", "ARMOR", "POTION"
    val attack: Int,
    val defense: Int,
    val healAmount: Int,
    val price: Long,
    val description: String
)

data class InventoryItemData(
    val id: Int,
    val item: ServerItemData,
    val quantity: Int,
    val isEquipped: Boolean
)

data class BattleResponse(
    val success: Boolean,
    val message: String,
    val rewards: OfflineRewards? = null
)

data class OfflineRewards(
    val experience: Long,
    val gold: Long,
    val monstersKilled: Int,
    val timeOfflineSeconds: Long
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)