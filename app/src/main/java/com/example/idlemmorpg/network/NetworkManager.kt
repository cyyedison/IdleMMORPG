package com.example.idlemmorpg.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetworkManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: NetworkManager? = null
        
        fun getInstance(context: Context): NetworkManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkManager(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        // 🔧 重要：修改這裡的IP為你的電腦IP
        private const val BASE_URL_EMULATOR = "http://192.168.1.96:8080/" // Android 模擬器
        private const val BASE_URL_DEVICE = "http://192.168.1.96:8080/" // 實體設備，替換為你的電腦IP
        
        // 當前使用的URL - 如果用模擬器用第一個，實體設備用第二個
        private const val CURRENT_BASE_URL = BASE_URL_EMULATOR
    }
    
    private val sharedPrefs = context.getSharedPreferences("game_auth", Context.MODE_PRIVATE)
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(CURRENT_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val gameApi: GameApiService = retrofit.create(GameApiService::class.java)
    
    fun getStoredToken(): String? {
        return sharedPrefs.getString("auth_token", null)
    }
    
    fun getStoredPlayerId(): Int? {
        val playerId = sharedPrefs.getInt("player_id", -1)
        return if (playerId == -1) null else playerId
    }
    
    fun saveAuthInfo(playerId: Int, token: String) {
        with(sharedPrefs.edit()) {
            putInt("player_id", playerId)
            putString("auth_token", token)
            apply()
        }
    }
    
    fun clearAuthInfo() {
        with(sharedPrefs.edit()) {
            remove("player_id")
            remove("auth_token")
            apply()
        }
    }
    
    // 檢查網路連接
    fun isNetworkAvailable(): Boolean {
        return try {
            val runtime = Runtime.getRuntime()
            val process = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue = process.waitFor()
            exitValue == 0
        } catch (e: Exception) {
            false
        }
    }
}