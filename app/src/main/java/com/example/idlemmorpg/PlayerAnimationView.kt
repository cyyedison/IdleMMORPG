package com.example.idlemmorpg

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.*

class PlayerAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var isAttacking = false
    private var armRotation = -30f  // 初始位置：舉起
    private var bodyBounce = 0f
    private var attackAnimator: AnimatorSet? = null
    private var currentPlayer: Player? = null

    // 顏色
    private val bodyColor = Paint().apply {
        color = Color.parseColor("#8B4513")
        isAntiAlias = true
    }

    private val skinColor = Paint().apply {
        color = Color.parseColor("#FDBCB4")
        isAntiAlias = true
    }

    private val legColor = Paint().apply {
        color = Color.parseColor("#000080")
        isAntiAlias = true
    }

    private val weaponColor = Paint().apply {
        color = Color.parseColor("#C0C0C0")
        isAntiAlias = true
        strokeWidth = 8f
    }

    private val shadowPaint = Paint().apply {
        color = Color.parseColor("#40000000")
        isAntiAlias = true
    }

    // 血條相關
    private val healthBarBgPaint = Paint().apply {
        color = Color.parseColor("#FF6B6B")
        isAntiAlias = true
    }

    private val healthBarFgPaint = Paint().apply {
        color = Color.parseColor("#4ECDC4")
        isAntiAlias = true
    }

    // 傷害數字相關
    private val damageNumbers = mutableListOf<DamageNumber>()
    private val damageTextPaint = Paint().apply {
        color = Color.RED
        textSize = 48f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        // 計算身體彈跳偏移
        val bounceOffset = sin(bodyBounce * PI / 180).toFloat() * 5f

        // 繪製陰影
        drawShadow(canvas, centerX, height - 20f)

        // 繪製左手臂（在身體後面，垂下）
        canvas.save()
        canvas.drawRoundRect(centerX - 42, centerY + bounceOffset - 30,
            centerX - 30, centerY + bounceOffset + 20, 6f, 6f, skinColor)
        canvas.restore()

        // 繪製腿部
        drawLegs(canvas, centerX, centerY + bounceOffset)

        // 繪製身體
        drawBody(canvas, centerX, centerY + bounceOffset)

        // 繪製頭部
        drawHead(canvas, centerX, centerY + bounceOffset - 80)

        // 繪製右手臂和武器（在身體前面）
        drawRightArmWithWeapon(canvas, centerX, centerY + bounceOffset)

        // 繪製玩家血條
        currentPlayer?.let { player ->
            drawPlayerHealthBar(canvas, player, centerX, centerY - 120)
        }

        // 繪製傷害數字
        drawDamageNumbers(canvas)
    }

    private fun drawShadow(canvas: Canvas, x: Float, y: Float) {
        canvas.drawOval(x - 40, y - 10, x + 40, y + 10, shadowPaint)
    }

    private fun drawHead(canvas: Canvas, x: Float, y: Float) {
        // 頭部
        canvas.drawCircle(x, y, 35f, skinColor)

        // 眼睛（看向前方）
        canvas.drawCircle(x - 12, y - 8, 4f, Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        })
        canvas.drawCircle(x + 12, y - 8, 4f, Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        })

        // 嘴巴（戰鬥的表情）
        canvas.drawArc(x - 8, y + 8, x + 8, y + 14, 0f, 180f, false,
            Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            })
    }

    private fun drawBody(canvas: Canvas, x: Float, y: Float) {
        // 身體 (矩形)
        canvas.drawRoundRect(x - 30, y - 40, x + 30, y + 40, 15f, 15f, bodyColor)
    }

    private fun drawLegs(canvas: Canvas, x: Float, y: Float) {
        // 左腿
        canvas.drawRoundRect(x - 25, y + 35, x - 5, y + 100, 10f, 10f, legColor)
        // 右腿
        canvas.drawRoundRect(x + 5, y + 35, x + 25, y + 100, 10f, 10f, legColor)

        // 腳
        canvas.drawRoundRect(x - 35, y + 95, x - 5, y + 110, 8f, 8f, Paint().apply { color = Color.BLACK })
        canvas.drawRoundRect(x + 5, y + 95, x + 35, y + 110, 8f, 8f, Paint().apply { color = Color.BLACK })
    }

    private fun drawRightArmWithWeapon(canvas: Canvas, x: Float, y: Float) {
        canvas.save()

        // 右手臂 (攻擊動畫) - 調整旋轉中心點到肩膀位置
        canvas.translate(x + 25, y - 35)  // 調整到肩膀位置
        canvas.rotate(armRotation)

        // 繪製上臂
        canvas.drawRoundRect(-5f, 0f, 7f, 30f, 6f, 6f, skinColor)

        // 繪製前臂和手
        canvas.translate(1f, 28f)
        canvas.rotate(20f) // 前臂稍微彎曲
        canvas.drawRoundRect(-5f, 0f, 5f, 25f, 5f, 5f, skinColor)

        // 繪製手掌（握劍的手）
        canvas.drawCircle(0f, 25f, 7f, skinColor)

        // 繪製武器（在同一個變換中）
        // 劍從手中向外延伸
        // 劍柄（在手中）
        canvas.drawRect(-3f, 20f, 3f, 35f, Paint().apply {
            color = Color.parseColor("#8B4513")
            isAntiAlias = true
        })

        // 護手
        canvas.drawRect(-10f, 35f, 10f, 38f, Paint().apply {
            color = Color.parseColor("#FFD700")
            isAntiAlias = true
        })

        // 劍身
        val bladePaint = Paint().apply {
            color = Color.parseColor("#C0C0C0")
            isAntiAlias = true
        }
        canvas.drawRect(-3f, 38f, 3f, 85f, bladePaint)

        // 劍尖
        val path = Path().apply {
            moveTo(0f, 85f)
            lineTo(-3f, 95f)
            lineTo(3f, 95f)
            close()
        }
        canvas.drawPath(path, bladePaint)

        canvas.restore()
    }

    private fun drawPlayerHealthBar(canvas: Canvas, player: Player, x: Float, y: Float) {
        val barWidth = 80f
        val barHeight = 8f

        // 計算血量比例
        val healthProgress = player.currentHp.toFloat() / player.maxHp.toFloat()

        // 背景
        canvas.drawRoundRect(
            x - barWidth/2, y, x + barWidth/2, y + barHeight,
            4f, 4f, healthBarBgPaint
        )

        // 前景（血量）
        canvas.drawRoundRect(
            x - barWidth/2, y,
            x - barWidth/2 + barWidth * healthProgress, y + barHeight,
            4f, 4f, healthBarFgPaint
        )

        // 血量文字
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("${player.currentHp}/${player.maxHp}", x, y - 5, textPaint)
    }

    private fun drawDamageNumbers(canvas: Canvas) {
        val iterator = damageNumbers.iterator()
        while (iterator.hasNext()) {
            val damage = iterator.next()
            damage.update()

            if (damage.isExpired()) {
                iterator.remove()
            } else {
                damage.draw(canvas, damageTextPaint)
            }
        }
    }

    fun startAttackAnimation() {
        if (isAttacking) return

        isAttacking = true

        // 手臂旋轉動畫 - 從上往下揮劍
        val armAnimator = ObjectAnimator.ofFloat(this, "armRotation", -170f, -45f, -170f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }

        // 身體彈跳動畫
        val bounceAnimator = ObjectAnimator.ofFloat(this, "bodyBounce", 0f, 360f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }

        attackAnimator = AnimatorSet().apply {
            playTogether(armAnimator, bounceAnimator)
            addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    isAttacking = false
                }
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    isAttacking = false
                }
                override fun onAnimationRepeat(animation: android.animation.Animator) {}
            })
            start()
        }
    }

    fun startContinuousAttack() {
        val continuousAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                // 從舉起到揮下的動作 - 更流暢的弧線
                val swingProgress = sin(progress * PI).toFloat()
                armRotation = -170f + swingProgress * 140f  // 從-45度到75度
                bodyBounce = progress * 360f
                invalidate()
            }
        }
        continuousAnimator.start()
        attackAnimator = AnimatorSet().apply {
            play(continuousAnimator)
        }
    }

    fun stopAttackAnimation() {
        attackAnimator?.cancel()
        armRotation = -30f  // 回到舉起位置
        bodyBounce = 0f
        isAttacking = false
        invalidate()
    }

    fun showDamage(damage: Int) {
        val x = width * 0.7f + (Math.random() * 100 - 50).toFloat()
        val y = height * 0.3f + (Math.random() * 100 - 50).toFloat()
        damageNumbers.add(DamageNumber(damage, x, y))
        invalidate()
    }

    fun updatePlayer(player: Player) {
        currentPlayer = player
        invalidate()
    }

    // 動畫屬性的setter，用於ObjectAnimator
    fun setArmRotation(rotation: Float) {
        armRotation = rotation
        invalidate()
    }

    fun getArmRotation(): Float = armRotation

    fun setBodyBounce(bounce: Float) {
        bodyBounce = bounce
        invalidate()
    }

    fun getBodyBounce(): Float = bodyBounce
}

// 傷害數字類
class DamageNumber(
    private val damage: Int,
    private var x: Float,
    private var y: Float
) {
    private var alpha = 255
    private var scale = 1f
    private var age = 0

    fun update() {
        age++
        y -= 3f // 向上漂浮
        x += (Math.random() * 2 - 1).toFloat() // 輕微搖擺
        alpha = maxOf(0, 255 - age * 8) // 漸漸透明
        scale = minOf(1.5f, 1f + age * 0.02f) // 輕微放大
    }

    fun draw(canvas: Canvas, paint: Paint) {
        canvas.save()
        canvas.translate(x, y)
        canvas.scale(scale, scale)

        paint.alpha = alpha
        paint.textAlign = Paint.Align.CENTER

        // 陰影效果
        paint.color = Color.BLACK
        canvas.drawText("-$damage", 2f, 2f, paint)

        // 主要文字
        paint.color = Color.RED
        canvas.drawText("-$damage", 0f, 0f, paint)

        canvas.restore()
    }

    fun isExpired(): Boolean = alpha <= 0
}