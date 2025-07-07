package com.example.idlemmorpg

import android.content.Context
import com.example.idlemmorpg.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServerGameManager(private val context: Context) {
    private val networkManager = NetworkManager.getInstance(context)
    private val gameApi = networkManager.gameApi
    
    // 本地緩存的玩家數據
    var player: Player? = null
        private set
    
    private var playerId: Int? = null
    private var authToken: String? = null
    var isLoggedIn: Boolean = false
        private set
    
    init {
        loadAuthInfo()
    }
    
    // 登入功能
    suspend fun login(username: String, password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = gameApi.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val loginResponse = response.body()!!
                playerId = loginResponse.playerId
                authToken = loginResponse.token
                isLoggedIn = true
                
                // 儲存認證資訊
                networkManager.saveAuthInfo(loginResponse.playerId!!, loginResponse.token!!)
                
                // 獲取玩家數據
                syncPlayerData()
                
                Result.success(true)
            } else {
                val errorMessage = response.body()?.message ?: "登入失敗"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 註冊功能
    suspend fun register(username: String, password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = gameApi.register(RegisterRequest(username, password))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                val errorMessage = response.body()?.message ?: "註冊失敗"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 登出功能
    fun logout() {
        player = null
        playerId = null
        authToken = null
        isLoggedIn = false
        networkManager.clearAuthInfo()
    }
    
    // 同步玩家數據
    suspend fun syncPlayerData(): Result<Player> = withContext(Dispatchers.IO) {
        try {
            val currentPlayerId = playerId ?: return@withContext Result.failure(Exception("未登入"))
            val response = gameApi.getPlayer(currentPlayerId)
            
            if (response.isSuccessful) {
                val playerData = response.body()!!
                player = convertToLocalPlayer(playerData)
                Result.success(player!!)
            } else {
                Result.failure(Exception("同步失敗: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 開始自動戰鬥
    suspend fun startAutoBattle(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val currentPlayerId = playerId ?: return@withContext Result.failure(Exception("未登入"))
            val response = gameApi.startBattle(currentPlayerId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // 更新本地狀態
                syncPlayerData()
                Result.success(response.body()?.message ?: "開始自動戰鬥")
            } else {
                val errorMessage = response.body()?.message ?: "開始戰鬥失敗"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 停止自動戰鬥
    suspend fun stopAutoBattle(): Result<OfflineRewards> = withContext(Dispatchers.IO) {
        try {
            val currentPlayerId = playerId ?: return@withContext Result.failure(Exception("未登入"))
            val response = gameApi.stopBattle(currentPlayerId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val battleResponse = response.body()!!
                // 同步最新玩家數據
                syncPlayerData()
                
                Result.success(battleResponse.rewards ?: OfflineRewards(0, 0, 0, 0))
            } else {
                val errorMessage = response.body()?.message ?: "停止戰鬥失敗"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 購買物品
    suspend fun buyItem(itemId: Int, quantity: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val currentPlayerId = playerId ?: return@withContext Result.failure(Exception("未登入"))
            val response = gameApi.buyItem(currentPlayerId, BuyItemRequest(itemId, quantity))
            
            if (response.isSuccessful && response.body()?.success == true) {
                // 重新同步玩家數據
                syncPlayerData()
                Result.success(response.body()?.message ?: "購買成功")
            } else {
                val errorMessage = response.body()?.message ?: "購買失敗"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 使用物品
    suspend fun useItem(itemId: Int, quantity: Int = 1): Result<String> = withContext(Dispatchers.IO) {
        try {
            val currentPlayerId = playerId ?: return@withContext Result.failure(Exception("未登入"))
            val response = gameApi.useItem(currentPlayerId, UseItemRequest(itemId, quantity))
            
            if (response.isSuccessful && response.body()?.success == true) {
                // 重新同步玩家數據
                syncPlayerData()
                Result.success(response.body()?.message ?: "使用成功")
            } else {
                val errorMessage = response.body()?.message ?: "使用物品失敗"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 獲取商店物品列表
    suspend fun getShopItems(): Result<List<ServerItemData>> = withContext(Dispatchers.IO) {
        try {
            val response = gameApi.getItems()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("獲取商店物品失敗"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 獲取庫存
    suspend fun getInventory(): Result<List<InventoryItemData>> = withContext(Dispatchers.IO) {
        try {
            val currentPlayerId = playerId ?: return@withContext Result.failure(Exception("未登入"))
            val response = gameApi.getInventory(currentPlayerId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("獲取庫存失敗"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 轉換服務器數據到本地Player對象
    private fun convertToLocalPlayer(playerData: ServerPlayerData): Player {
        return Player(
            level = playerData.level,
            experience = playerData.experience.toInt(),
            maxHp = playerData.maxHp,
            currentHp = playerData.currentHp,
            baseAttack = playerData.baseAttack,
            baseDefense = playerData.baseDefense,
            gold = playerData.gold.toInt(),
            weaponAttack = playerData.weaponAttack,
            armorDefense = playerData.armorDefense,
            potions = mutableMapOf() // 需要從庫存API獲取
        )
    }
    
    private fun loadAuthInfo() {
        playerId = networkManager.getStoredPlayerId()
        authToken = networkManager.getStoredToken()
        isLoggedIn = playerId != null && authToken != null
    }
    
    // 自動重新連線和同步
    suspend fun autoSync() {
        if (isLoggedIn) {
            try {
                syncPlayerData()
            } catch (e: Exception) {
                // 如果同步失敗，可能是token過期，需要重新登入
                logout()
                throw e
            }
        }
    }
    
    // 檢查是否需要重新登入
    fun needsLogin(): Boolean {
        return !isLoggedIn || playerId == null
    }
    
    // 獲取當前玩家ID
    fun getCurrentPlayerId(): Int? = playerId
    
    // 檢查網路連接
    fun isNetworkAvailable(): Boolean = networkManager.isNetworkAvailable()
}