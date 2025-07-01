package com.example.idlemmorpg

import android.graphics.Color

object UIHelpers {

    // 顏色常量
    object Colors {
        val GOLD = Color.parseColor("#FFD700")
        val WHITE = Color.parseColor("#FFFFFF")
        val GREEN = Color.parseColor("#4CAF50")
        val RED = Color.parseColor("#F44336")
        val BLUE = Color.parseColor("#1E90FF")
        val DARK_RED = Color.parseColor("#DC143C")
        val ORANGE = Color.parseColor("#FF4500")
        val LIGHT_GREEN = Color.parseColor("#98FB98")
        val DARK_GREEN = Color.parseColor("#006400")
        val LIGHT_BLUE = Color.parseColor("#87CEEB")
        val DARK_BLUE = Color.parseColor("#191970")
        val BROWN = Color.parseColor("#8B4513")
        val TOMATO = Color.parseColor("#FF6347")
        val LIGHT_CYAN = Color.parseColor("#E0FFFF")
        val SEMI_TRANSPARENT_BLACK = Color.parseColor("#40000000")
        val SEMI_TRANSPARENT_WHITE = Color.parseColor("#40FFFFFF")
        val SEMI_TRANSPARENT_LIGHT_BLUE = Color.parseColor("#40F0F8FF")
        val SEMI_TRANSPARENT_GREEN = Color.parseColor("#40F0FFF0")
        val SEMI_TRANSPARENT_YELLOW = Color.parseColor("#40FFF8DC")
        val SEMI_TRANSPARENT_GRAY = Color.parseColor("#40F8F9FA")
    }

    // 武器數據
    object GameData {
        val WEAPONS = listOf(
            Weapon("🗡️ 鐵劍", 10, 100),
            Weapon("⚔️ 鋼劍", 25, 500),
            Weapon("🗡️ 銀劍", 50, 2000),
            Weapon("⚔️ 金劍", 100, 10000),
            Weapon("🗡️ 神劍", 200, 50000)
        )

        val ARMORS = listOf(
            Armor("👕 布衣", 5, 50),
            Armor("🦺 皮甲", 15, 300),
            Armor("⛓️ 鎖甲", 30, 1500),
            Armor("🛡️ 板甲", 60, 8000),
            Armor("✨ 神甲", 120, 40000)
        )

        val POTIONS = listOf(
            HealingPotion("🧪 小回血藥", 50, 1, "回復50血量的基礎藥品"),
            HealingPotion("💉 中回血藥", 150, 5, "回復150血量的進階藥品"),
            HealingPotion("🍶 大回血藥", 300, 20, "回復300血量的高級藥品"),
            HealingPotion("💎 超級回血藥", 500, 50, "回復500血量的極品藥品"),
            HealingPotion("⭐ 神級回血藥", 1000, 200, "回復1000血量的傳說藥品")
        )

        val TRAINING_LOCATIONS = listOf(
            PopupMenuItem("⚔️ 練功樓 - 1層", "trainingGround1", "適合新手的史萊姆"),
            PopupMenuItem("⚔️ 練功樓 - 2層", "trainingGround2", "哥布林出沒地"),
            PopupMenuItem("⚔️ 練功樓 - 3層", "trainingGround3", "獸人領域"),
            PopupMenuItem("⚔️ 練功樓 - 4層", "trainingGround4", "巨魔巢穴"),
            PopupMenuItem("⚔️ 練功樓 - 5層", "trainingGround5", "龍族聖地")
        )

        val SHOP_LOCATIONS = listOf(
            PopupMenuItem("🔨 五金鋪", "weaponShop", "購買各種武器"),
            PopupMenuItem("👕 衣服店", "armorShop", "購買防具裝備"),
            PopupMenuItem("🏪 便利店", "convenienceStore", "購買回血藥品")
        )
    }

    // 位置顯示名稱轉換
    fun getLocationDisplayName(location: String): String {
        return when (location) {
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
    }

    // 遊戲文本常量
    object GameTexts {
        const val WELCOME_TEXT = """
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
            
            👤 點擊左上角頭像查看詳細角色資訊
        """

        const val SETTINGS_TEXT = """
            🎮 閒置MMORPG
            版本: 2.0.0
            
            🛠️ 功能說明:
            • 自動戰鬥系統
            • 離線掛機收益
            • 智能藥品使用
            • 裝備升級系統
            • 全新三區域UI設計
            
            📱 遊戲特色:
            • 即使關閉遊戲也會繼續戰鬥
            • 多層練功樓挑戰
            • 豐富的裝備商店
            • 便利的藥品系統
            • 逼真的戰士角色動畫
            
            💡 小提示:
            • 點擊左上角頭像查看詳細資訊
            • 血量和經驗條顯示在頭像右方
            • 定期檢查背包和購買裝備
            • 記得購買回血藥品
            • 挑戰更高層練功樓獲得更多經驗
            
            🎯 UI設計特色:
            • 三區域佈局：狀態列、主顯示區、導航列
            • 所有功能統一在主顯示區域
            • 更簡潔清晰的遊戲界面
        """

        const val POTION_INTRO = "💡 自動戰鬥時會自動使用藥品回血哦！"
    }
}