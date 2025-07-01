package com.example.idlemmorpg

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class BattleView(context: Context, private val gameManager: GameManager) : LinearLayout(context) {
    private val battleText: TextView
    private val playerStatusText: TextView
    private val monsterStatusText: TextView
    private val damageText: TextView
    private val playerAnimationView: PlayerAnimationView
    private val monsterAnimationView: MonsterAnimationView
    private val battleContainer: LinearLayout
    private val handler = Handler(Looper.getMainLooper())

    init {
        orientation = VERTICAL
        setPadding(8, 8, 8, 8)
        setBackgroundColor(Color.parseColor("#40F0F8FF"))

        battleText = TextView(context).apply {
            text = "⚔️ 戰鬥區域 ⚔️"
            textSize = 18f
            setTextColor(Color.parseColor("#FFD700"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 12)
        }

        battleContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                300
            )
        }

        playerAnimationView = PlayerAnimationView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        }

        monsterAnimationView = MonsterAnimationView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        }

        battleContainer.addView(playerAnimationView)
        battleContainer.addView(monsterAnimationView)

        playerStatusText = TextView(context).apply {
            text = "🛡️ 玩家狀態"
            textSize = 11f
            setTextColor(Color.parseColor("#1E90FF"))
            setPadding(6, 6, 6, 6)
            setBackgroundColor(Color.parseColor("#40E6F3FF"))
        }

        monsterStatusText = TextView(context).apply {
            text = "👹 怪物狀態"
            textSize = 11f
            setTextColor(Color.parseColor("#DC143C"))
            setPadding(6, 6, 6, 6)
            setBackgroundColor(Color.parseColor("#40FFE6E6"))
        }

        damageText = TextView(context).apply {
            text = "💥 戰鬥狀態"
            textSize = 13f
            setTextColor(Color.parseColor("#FF4500"))
            gravity = Gravity.CENTER
            setPadding(0, 6, 0, 6)
        }

        addView(battleText)
        addView(battleContainer)
        addView(playerStatusText)
        addView(monsterStatusText)
        addView(damageText)

        updateBattleStatus()
        startUpdateLoop()
    }

    private var isAnimating = false

    private fun startUpdateLoop() {
        val updateRunnable = object : Runnable {
            override fun run() {
                updateBattleStatus()
                handler.postDelayed(this, 500)
            }
        }
        handler.post(updateRunnable)
    }

    private fun updateBattleStatus() {
        val player = gameManager.player

        playerStatusText.text = """
            🛡️ 玩家狀態
            ❤️ 血量: ${player.currentHp}/${player.maxHp}
            ⚔️ 攻擊: ${player.attack}
            🛡️ 防禦: ${player.defense}
        """.trimIndent()

        playerAnimationView.updatePlayer(player)

        val monster = gameManager.getCurrentMonster()
        if (monster != null) {
            monsterStatusText.text = """
                👹 ${monster.name} (Lv.${monster.level})
                ❤️ 血量: ${monster.currentHp}/${monster.hp}
                ⚔️ 攻擊: ${monster.attack}
                🛡️ 防禦: ${monster.defense}
            """.trimIndent()

            monsterAnimationView.updateMonster(monster)
        } else {
            monsterStatusText.text = "👹 怪物狀態\n沒有怪物"
        }

        if (gameManager.isAutoBattling()) {
            damageText.text = "💥 自動戰鬥中..."
            if (!isAnimating) {
                startBattleAnimation()
            }
        } else {
            damageText.text = "⏸️ 戰鬥已停止"
            stopBattleAnimation()
        }
    }

    private fun startBattleAnimation() {
        isAnimating = true
        playerAnimationView.startContinuousAttack()
        monsterAnimationView.startAnimation()
    }

    private fun stopBattleAnimation() {
        isAnimating = false
        playerAnimationView.stopAttackAnimation()
        monsterAnimationView.stopAnimation()
    }

    fun showDamage(damage: Int) {
        playerAnimationView.showDamage(damage)
        playerAnimationView.startAttackAnimation()
    }

    fun showMonsterDamage(damage: Int) {
        monsterAnimationView.showMonsterDamage(damage)
    }
}