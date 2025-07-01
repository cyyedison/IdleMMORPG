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
    private var weaponGlow = 0f // 武器發光效果

    // 顏色定義 - 更真實的色彩
    private val armorColor = Paint().apply {
        color = Color.parseColor("#2C3E50") // 深藍鋼鐵色
        isAntiAlias = true
    }

    private val armorHighlight = Paint().apply {
        color = Color.parseColor("#34495E") // 盔甲高光
        isAntiAlias = true
    }

    private val skinColor = Paint().apply {
        color = Color.parseColor("#D4A574") // 更真實的膚色
        isAntiAlias = true
    }

    private val skinShadow = Paint().apply {
        color = Color.parseColor("#B8956A") // 膚色陰影
        isAntiAlias = true
    }

    private val cloakColor = Paint().apply {
        color = Color.parseColor("#8B0000") // 深紅斗篷
        isAntiAlias = true
    }

    private val leatherColor = Paint().apply {
        color = Color.parseColor("#654321") // 皮革色
        isAntiAlias = true
    }

    private val weaponBlade = Paint().apply {
        color = Color.parseColor("#E8E8E8") // 劍身銀色
        isAntiAlias = true
    }

    private val weaponEdge = Paint().apply {
        color = Color.parseColor("#FFFFFF") // 劍刃反光
        isAntiAlias = true
        strokeWidth = 2f
    }

    private val goldColor = Paint().apply {
        color = Color.parseColor("#FFD700") // 黃金裝飾
        isAntiAlias = true
    }

    private val shadowPaint = Paint().apply {
        color = Color.parseColor("#40000000")
        isAntiAlias = true
    }

    // 血條相關
    private val healthBarBgPaint = Paint().apply {
        color = Color.parseColor("#8B0000")
        isAntiAlias = true
    }

    private val healthBarFgPaint = Paint().apply {
        color = Color.parseColor("#DC143C")
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
        val bounceOffset = sin(bodyBounce * PI / 180).toFloat() * 3f

        // 繪製陰影
        drawRealisticShadow(canvas, centerX, height - 15f)

        // 繪製斗篷（在身體後面）
        drawCloak(canvas, centerX, centerY + bounceOffset)

        // 繪製腿部盔甲
        drawArmoredLegs(canvas, centerX, centerY + bounceOffset)

        // 繪製身體盔甲
        drawArmoredBody(canvas, centerX, centerY + bounceOffset)

        // 繪製頭盔和頭部
        drawHelmetAndHead(canvas, centerX, centerY + bounceOffset - 80)

        // 繪製左手臂（盾牌手）
        drawLeftArmWithShield(canvas, centerX, centerY + bounceOffset)

        // 繪製右手臂和武器（在身體前面）
        drawRightArmWithWeapon(canvas, centerX, centerY + bounceOffset)

        // 繪製玩家血條
        currentPlayer?.let { player ->
            drawPlayerHealthBar(canvas, player, centerX, centerY - 130)
        }

        // 繪製傷害數字
        drawDamageNumbers(canvas)
    }

    private fun drawRealisticShadow(canvas: Canvas, x: Float, y: Float) {
        val shadowRadius = 45f
        val shadowGradient = RadialGradient(
            x, y, shadowRadius,
            intArrayOf(Color.parseColor("#60000000"), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        shadowPaint.shader = shadowGradient
        canvas.drawCircle(x, y, shadowRadius, shadowPaint)
        shadowPaint.shader = null
    }

    private fun drawCloak(canvas: Canvas, x: Float, y: Float) {
        // 斗篷主體 - 飄逸效果
        val cloakPath = Path().apply {
            moveTo(x - 15, y - 60) // 肩膀
            quadTo(x - 45, y - 30, x - 35, y + 40) // 左側弧線
            quadTo(x - 20, y + 45, x, y + 50) // 底部
            quadTo(x + 20, y + 45, x + 35, y + 40) // 右側
            quadTo(x + 45, y - 30, x + 15, y - 60) // 回到肩膀
            close()
        }
        canvas.drawPath(cloakPath, cloakColor)

        // 斗篷陰影
        val shadowPath = Path().apply {
            moveTo(x - 10, y - 55)
            quadTo(x - 25, y - 20, x - 20, y + 35)
            quadTo(x - 15, y + 40, x + 5, y + 45)
            lineTo(x + 15, y - 60)
            close()
        }
        val cloakShadow = Paint().apply {
            color = Color.parseColor("#660000")
            isAntiAlias = true
        }
        canvas.drawPath(shadowPath, cloakShadow)
    }

    private fun drawArmoredLegs(canvas: Canvas, x: Float, y: Float) {
        // 左腿盔甲
        drawLegArmor(canvas, x - 15, y + 35, y + 95)
        // 右腿盔甲
        drawLegArmor(canvas, x + 15, y + 35, y + 95)

        // 戰靴
        drawCombatBoots(canvas, x, y + 90)
    }

    private fun drawLegArmor(canvas: Canvas, centerX: Float, topY: Float, bottomY: Float) {
        // 主要盔甲部分
        canvas.drawRoundRect(
            centerX - 12, topY, centerX + 12, bottomY,
            8f, 8f, armorColor
        )

        // 盔甲高光
        canvas.drawRoundRect(
            centerX - 10, topY + 5, centerX - 5, bottomY - 10,
            4f, 4f, armorHighlight
        )

        // 膝蓋護甲
        canvas.drawCircle(centerX, topY + 25, 8f, goldColor)
        canvas.drawCircle(centerX, topY + 25, 6f, armorColor)

        // 盔甲接縫線
        val seamPaint = Paint().apply {
            color = Color.parseColor("#1A252F")
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawLine(centerX - 8, topY + 15, centerX + 8, topY + 15, seamPaint)
        canvas.drawLine(centerX - 8, topY + 40, centerX + 8, topY + 40, seamPaint)
    }

    private fun drawCombatBoots(canvas: Canvas, x: Float, y: Float) {
        // 左靴
        canvas.drawRoundRect(x - 25, y, x - 5, y + 18, 6f, 6f, leatherColor)
        canvas.drawRoundRect(x - 23, y + 2, x - 7, y + 8, 3f, 3f, goldColor)

        // 右靴
        canvas.drawRoundRect(x + 5, y, x + 25, y + 18, 6f, 6f, leatherColor)
        canvas.drawRoundRect(x + 7, y + 2, x + 23, y + 8, 3f, 3f, goldColor)
    }

    private fun drawArmoredBody(canvas: Canvas, x: Float, y: Float) {
        // 主體胸甲
        canvas.drawRoundRect(x - 32, y - 45, x + 32, y + 40, 15f, 15f, armorColor)

        // 胸甲裝飾
        canvas.drawRoundRect(x - 28, y - 40, x + 28, y + 35, 12f, 12f, armorHighlight)

        // 中央裝飾 - 徽章
        val emblemPath = Path().apply {
            moveTo(x, y - 25)
            lineTo(x - 8, y - 15)
            lineTo(x - 5, y)
            lineTo(x, y - 5)
            lineTo(x + 5, y)
            lineTo(x + 8, y - 15)
            close()
        }
        canvas.drawPath(emblemPath, goldColor)

        // 肩甲
        drawShoulderArmor(canvas, x - 35, y - 35) // 左肩
        drawShoulderArmor(canvas, x + 35, y - 35) // 右肩

        // 腰帶
        canvas.drawRoundRect(x - 30, y + 20, x + 30, y + 30, 5f, 5f, leatherColor)
        canvas.drawRect(x - 5, y + 18, x + 5, y + 32, goldColor) // 腰帶扣
    }

    private fun drawShoulderArmor(canvas: Canvas, x: Float, y: Float) {
        // 肩甲主體
        canvas.drawCircle(x, y, 15f, armorColor)
        canvas.drawCircle(x, y, 12f, armorHighlight)

        // 肩甲釘子
        canvas.drawCircle(x - 5, y - 5, 2f, goldColor)
        canvas.drawCircle(x + 5, y - 5, 2f, goldColor)
        canvas.drawCircle(x, y + 5, 2f, goldColor)
    }

    private fun drawHelmetAndHead(canvas: Canvas, x: Float, y: Float) {
        // 頭部（部分可見）
        canvas.drawCircle(x, y, 32f, skinColor)
        canvas.drawCircle(x, y, 28f, skinShadow)

        // 頭盔
        val helmetPath = Path().apply {
            moveTo(x - 30, y + 15)
            quadTo(x - 35, y - 10, x - 25, y - 30)
            quadTo(x, y - 40, x + 25, y - 30)
            quadTo(x + 35, y - 10, x + 30, y + 15)
            lineTo(x + 20, y + 10)
            lineTo(x - 20, y + 10)
            close()
        }
        canvas.drawPath(helmetPath, armorColor)

        // 頭盔裝飾
        canvas.drawRect(x - 2, y - 35, x + 2, y - 15, goldColor)

        // 面甲 - 只露出眼睛
        canvas.drawRoundRect(x - 25, y - 5, x + 25, y + 15, 8f, 8f, armorColor)

        // 眼縫
        val eyeSlotPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }
        canvas.drawRoundRect(x - 15, y - 2, x - 5, y + 3, 2f, 2f, eyeSlotPaint)
        canvas.drawRoundRect(x + 5, y - 2, x + 15, y + 3, 2f, 2f, eyeSlotPaint)

        // 發光的眼睛
        val eyeGlow = Paint().apply {
            color = Color.parseColor("#FF4500")
            isAntiAlias = true
        }
        canvas.drawCircle(x - 10, y, 1.5f, eyeGlow)
        canvas.drawCircle(x + 10, y, 1.5f, eyeGlow)
    }

    private fun drawLeftArmWithShield(canvas: Canvas, x: Float, y: Float) {
        canvas.save()
        canvas.translate(x - 25, y - 35)

        // 左手臂
        canvas.drawRoundRect(-8f, 0f, 8f, 35f, 6f, 6f, armorColor)
        canvas.drawRoundRect(-6f, 2f, 3f, 30f, 3f, 3f, armorHighlight)

        // 前臂
        canvas.translate(-2f, 32f)
        canvas.rotate(15f)
        canvas.drawRoundRect(-6f, 0f, 6f, 25f, 5f, 5f, armorColor)

        // 小型盾牌
        canvas.translate(-15f, 10f)
        drawSmallShield(canvas)

        canvas.restore()
    }

    private fun drawSmallShield(canvas: Canvas) {
        // 盾牌主體
        val shieldPath = Path().apply {
            moveTo(0f, -15f)
            lineTo(-12f, -10f)
            lineTo(-15f, 5f)
            lineTo(-10f, 20f)
            lineTo(0f, 25f)
            lineTo(10f, 20f)
            lineTo(15f, 5f)
            lineTo(12f, -10f)
            close()
        }
        canvas.drawPath(shieldPath, armorColor)

        // 盾牌邊框
        val shieldBorder = Paint().apply {
            color = goldColor.color
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawPath(shieldPath, shieldBorder)

        // 盾牌中央裝飾
        canvas.drawCircle(0f, 5f, 8f, goldColor)
        canvas.drawCircle(0f, 5f, 5f, armorHighlight)
    }

    private fun drawRightArmWithWeapon(canvas: Canvas, x: Float, y: Float) {
        canvas.save()

        // 右手臂 - 調整旋轉中心點到肩膀位置
        canvas.translate(x + 25, y - 35)
        canvas.rotate(armRotation)

        // 上臂盔甲
        canvas.drawRoundRect(-8f, 0f, 8f, 32f, 6f, 6f, armorColor)
        canvas.drawRoundRect(-6f, 2f, 3f, 28f, 3f, 3f, armorHighlight)

        // 肘甲
        canvas.drawCircle(0f, 30f, 6f, goldColor)

        // 前臂
        canvas.translate(0f, 28f)
        canvas.rotate(20f) // 前臂稍微彎曲
        canvas.drawRoundRect(-6f, 0f, 6f, 28f, 5f, 5f, armorColor)
        canvas.drawRoundRect(-4f, 2f, 2f, 24f, 2f, 2f, armorHighlight)

        // 手甲
        canvas.drawRoundRect(-8f, 25f, 8f, 35f, 4f, 4f, armorColor)

        // 繪製魔法劍
        drawMagicalSword(canvas)

        canvas.restore()
    }

    private fun drawMagicalSword(canvas: Canvas) {
        // 劍柄（在手中）
        val gripPaint = Paint().apply {
            color = Color.parseColor("#4A4A4A")
            isAntiAlias = true
        }
        canvas.drawRoundRect(-4f, 20f, 4f, 40f, 2f, 2f, gripPaint)

        // 劍柄纏繞
        val wrapPaint = Paint().apply {
            color = leatherColor.color
            strokeWidth = 2f
            isAntiAlias = true
        }
        for (i in 0..5) {
            canvas.drawLine(-3f, 22f + i * 3, 3f, 24f + i * 3, wrapPaint)
        }

        // 護手 - 更精細
        val crossguardPath = Path().apply {
            moveTo(-15f, 38f)
            lineTo(-8f, 35f)
            lineTo(8f, 35f)
            lineTo(15f, 38f)
            lineTo(12f, 42f)
            lineTo(-12f, 42f)
            close()
        }
        canvas.drawPath(crossguardPath, goldColor)

        // 劍身 - 魔法光芒效果
        val bladeGradient = LinearGradient(
            0f, 40f, 0f, 100f,
            intArrayOf(
                Color.parseColor("#E8E8E8"),
                Color.parseColor("#C0C0C0"),
                Color.parseColor("#E8E8E8")
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        weaponBlade.shader = bladeGradient
        canvas.drawRoundRect(-4f, 40f, 4f, 95f, 2f, 2f, weaponBlade)

        // 劍尖
        val tipPath = Path().apply {
            moveTo(0f, 95f)
            lineTo(-4f, 105f)
            lineTo(4f, 105f)
            close()
        }
        canvas.drawPath(tipPath, weaponBlade)

        // 魔法符文（劍身中央）
        val runePaint = Paint().apply {
            color = Color.parseColor("#00BFFF")
            isAntiAlias = true
            alpha = (128 + sin(weaponGlow * PI / 180).toFloat() * 127).toInt()
        }

        for (i in 0..4) {
            val runeY = 45f + i * 10f
            canvas.drawCircle(0f, runeY, 1.5f, runePaint)
            canvas.drawLine(-2f, runeY, 2f, runeY, runePaint)
        }

        // 劍刃反光
        weaponEdge.alpha = (100 + sin(weaponGlow * PI / 180).toFloat() * 100).toInt()
        canvas.drawLine(-2f, 42f, -2f, 100f, weaponEdge)

        weaponBlade.shader = null
    }

    private fun drawPlayerHealthBar(canvas: Canvas, player: Player, x: Float, y: Float) {
        val barWidth = 90f
        val barHeight = 12f

        // 計算血量比例
        val healthProgress = player.currentHp.toFloat() / player.maxHp.toFloat()

        // 血條外框
        val borderPaint = Paint().apply {
            color = Color.parseColor("#8B4513")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            x - barWidth/2 - 2, y - 2, x + barWidth/2 + 2, y + barHeight + 2,
            6f, 6f, borderPaint
        )

        // 背景
        canvas.drawRoundRect(
            x - barWidth/2, y, x + barWidth/2, y + barHeight,
            4f, 4f, healthBarBgPaint
        )

        // 前景（血量）- 漸變效果
        val healthGradient = LinearGradient(
            x - barWidth/2, y, x + barWidth/2, y,
            intArrayOf(Color.parseColor("#DC143C"), Color.parseColor("#FF6347")),
            null,
            Shader.TileMode.CLAMP
        )
        healthBarFgPaint.shader = healthGradient
        canvas.drawRoundRect(
            x - barWidth/2, y,
            x - barWidth/2 + barWidth * healthProgress, y + barHeight,
            4f, 4f, healthBarFgPaint
        )

        // 血量文字
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 16f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }
        canvas.drawText("${player.currentHp}/${player.maxHp}", x, y - 8, textPaint)

        healthBarFgPaint.shader = null
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
        val armAnimator = ObjectAnimator.ofFloat(this, "armRotation", -180f, -45f, -180f).apply {
            duration = 700
            interpolator = AccelerateDecelerateInterpolator()
        }

        // 身體彈跳動畫
        val bounceAnimator = ObjectAnimator.ofFloat(this, "bodyBounce", 0f, 360f).apply {
            duration = 700
            interpolator = AccelerateDecelerateInterpolator()
        }

        // 武器發光動畫
        val glowAnimator = ObjectAnimator.ofFloat(this, "weaponGlow", 0f, 360f).apply {
            duration = 700
            interpolator = AccelerateDecelerateInterpolator()
        }

        attackAnimator = AnimatorSet().apply {
            playTogether(armAnimator, bounceAnimator, glowAnimator)
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
            duration = 1200
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                // 從舉起到揮下的動作 - 更流暢的弧線
                val swingProgress = sin(progress * PI).toFloat()
                armRotation = -180f + swingProgress * 160f  // 更大的揮劍弧度
                bodyBounce = progress * 360f
                weaponGlow = progress * 720f // 更快的發光效果
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
        weaponGlow = 0f
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

    fun setWeaponGlow(glow: Float) {
        weaponGlow = glow
        invalidate()
    }

    fun getWeaponGlow(): Float = weaponGlow
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
        paint.color = Color.parseColor("#FF4500") // 橙紅色傷害數字
        canvas.drawText("-$damage", 0f, 0f, paint)

        canvas.restore()
    }

    fun isExpired(): Boolean = alpha <= 0
}