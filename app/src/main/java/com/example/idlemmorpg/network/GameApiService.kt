package com.example.idlemmorpg.network

import retrofit2.Response
import retrofit2.http.*

interface GameApiService {
    
    @POST("api/players/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    @POST("api/players/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @GET("api/players/{playerId}")
    suspend fun getPlayer(@Path("playerId") playerId: Int): Response<ServerPlayerData>
    
    @POST("api/players/{playerId}/battle/start")
    suspend fun startBattle(@Path("playerId") playerId: Int): Response<BattleResponse>
    
    @POST("api/players/{playerId}/battle/stop")
    suspend fun stopBattle(@Path("playerId") playerId: Int): Response<BattleResponse>
    
    @POST("api/players/{playerId}/sync")
    suspend fun syncPlayer(@Path("playerId") playerId: Int): Response<ServerPlayerData>
    
    @GET("api/items")
    suspend fun getItems(): Response<List<ServerItemData>>
    
    @GET("api/players/{playerId}/inventory")
    suspend fun getInventory(@Path("playerId") playerId: Int): Response<List<InventoryItemData>>
    
    @POST("api/players/{playerId}/inventory/buy")
    suspend fun buyItem(@Path("playerId") playerId: Int, @Body request: BuyItemRequest): Response<ApiResponse<String>>
    
    @POST("api/players/{playerId}/inventory/use")
    suspend fun useItem(@Path("playerId") playerId: Int, @Body request: UseItemRequest): Response<ApiResponse<String>>
}