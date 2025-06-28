package com.example.idlemmorpg

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

// 怪物動畫視圖
class MonsterAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentMonster: Monster? = null
    private var isAnimating = false
    private var animationProgress = 0f
    private var healthBarProgress = 1f
    private var attackAnimator: ValueAnimator? = null

    // 傷害數字相關
    private val damageNumbers = mutableListOf<MonsterDamageNumber>()
    private val damageTextPaint = Paint().apply {
        color = Color.parseColor("#FF4500")
        textSize = 42f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val shadowPaint = Paint().apply {
        color = Color.parseColor("#40000000")
        isAntiAlias = true
    }

    private val healthBarBgPaint = Paint().apply {
        color = Color.parseColor("#FF6B6B")
        isAntiAlias = true
    }

    private val healthBarFgPaint = Paint().apply {
        color = Color.parseColor("#4ECDC4")
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        currentMonster?.let { monster ->
            val centerX = width / 2f
            val centerY = height / 2f

            // 計算動畫偏移
            val bounceOffset = sin(animationProgress * PI).toFloat() * 8f

            // 繪製陰影
            drawShadow(canvas, centerX, height - 20f)

            // 根據怪物類型繪製不同形狀
            drawMonster(canvas, monster, centerX, centerY + bounceOffset)

            // 繪製血條
            drawHealthBar(canvas, monster, centerX, centerY - 80)

            // 繪製怪物傷害數字
            drawMonsterDamageNumbers(canvas)
        }
    }

    private fun drawMonsterDamageNumbers(canvas: Canvas) {
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

    fun showMonsterDamage(damage: Int) {
        val centerX = width / 2f
        val centerY = height / 2f
        val x = centerX + (Math.random() * 60 - 30).toFloat()
        val y = centerY - 20 + (Math.random() * 40 - 20).toFloat()
        damageNumbers.add(MonsterDamageNumber(damage, x, y))
        invalidate()
    }

    private fun drawShadow(canvas: Canvas, x: Float, y: Float) {
        canvas.drawOval(x - 35, y - 8, x + 35, y + 8, shadowPaint)
    }

    private fun drawMonster(canvas: Canvas, monster: Monster, x: Float, y: Float) {
        when (monster.name) {
            "史萊姆" -> drawSlime(canvas, x, y)
            "哥布林" -> drawGoblin(canvas, x, y)
            "獸人" -> drawOrc(canvas, x, y)
            "巨魔" -> drawTroll(canvas, x, y)
            "龍族" -> drawDragon(canvas, x, y)
            else -> drawSlime(canvas, x, y)
        }
    }

    private fun drawSlime(canvas: Canvas, x: Float, y: Float) {
        // 史萊姆身體（綠色圓形）
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#32CD32")
            isAntiAlias = true
        }
        canvas.drawCircle(x, y, 40f, bodyPaint)

        // 眼睛
        val eyePaint = Paint().apply { color = Color.BLACK }
        canvas.drawCircle(x - 15, y - 10, 6f, eyePaint)
        canvas.drawCircle(x + 15, y - 10, 6f, eyePaint)

        // 嘴巴
        canvas.drawArc(x - 12, y + 5, x + 12, y + 20, 0f, 180f, false,
            Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 4f
            })
    }

    private fun drawGoblin(canvas: Canvas, x: Float, y: Float) {
        // 哥布林身體（棕色）
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#8B4513")
            isAntiAlias = true
        }
        canvas.drawRoundRect(x - 25, y - 30, x + 25, y + 30, 12f, 12f, bodyPaint)

        // 頭部（綠色）
        val headPaint = Paint().apply {
            color = Color.parseColor("#228B22")
            isAntiAlias = true
        }
        canvas.drawCircle(x, y - 45, 25f, headPaint)

        // 尖耳朵
        val earPaint = Paint().apply {
            color = Color.parseColor("#228B22")
            isAntiAlias = true
        }
        canvas.drawCircle(x - 35, y - 50, 8f, earPaint)
        canvas.drawCircle(x + 35, y - 50, 8f, earPaint)

        // 眼睛
        canvas.drawCircle(x - 10, y - 50, 4f, Paint().apply { color = Color.RED })
        canvas.drawCircle(x + 10, y - 50, 4f, Paint().apply { color = Color.RED })
    }

    private fun drawOrc(canvas: Canvas, x: Float, y: Float) {
        // 獸人身體（深棕色）
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#654321")
            isAntiAlias = true
        }
        canvas.drawRoundRect(x - 35, y - 40, x + 35, y + 40, 15f, 15f, bodyPaint)

        // 頭部（灰綠色）
        val headPaint = Paint().apply {
            color = Color.parseColor("#556B2F")
            isAntiAlias = true
        }
        canvas.drawCircle(x, y - 55, 30f, headPaint)

        // 獠牙
        val tuskPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }
        canvas.drawRect(x - 15, y - 45, x - 10, y - 35, tuskPaint)
        canvas.drawRect(x + 10, y - 45, x + 15, y - 35, tuskPaint)

        // 眼睛
        canvas.drawCircle(x - 12, y - 60, 5f, Paint().apply { color = Color.RED })
        canvas.drawCircle(x + 12, y - 60, 5f, Paint().apply { color = Color.RED })

        // 手臂
        canvas.drawRoundRect(x - 55, y - 20, x - 35, y + 20, 10f, 10f, bodyPaint)
        canvas.drawRoundRect(x + 35, y - 20, x + 55, y + 20, 10f, 10f, bodyPaint)
    }

    private fun drawTroll(canvas: Canvas, x: Float, y: Float) {
        // 巨魔身體（深灰色）
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#2F4F4F")
            isAntiAlias = true
        }
        canvas.drawRoundRect(x - 40, y - 50, x + 40, y + 50, 18f, 18f, bodyPaint)

        // 頭部（較大）
        canvas.drawCircle(x, y - 70, 35f, bodyPaint)

        // 角
        val hornPaint = Paint().apply {
            color = Color.parseColor("#8B4513")
            isAntiAlias = true
        }
        canvas.drawRect(x - 25, y - 95, x - 15, y - 70, hornPaint)
        canvas.drawRect(x + 15, y - 95, x + 25, y - 70, hornPaint)

        // 眼睛（發光）
        canvas.drawCircle(x - 15, y - 75, 6f, Paint().apply { color = Color.YELLOW })
        canvas.drawCircle(x + 15, y - 75, 6f, Paint().apply { color = Color.YELLOW })

        // 大手臂
        canvas.drawRoundRect(x - 70, y - 30, x - 40, y + 30, 15f, 15f, bodyPaint)
        canvas.drawRoundRect(x + 40, y - 30, x + 70, y + 30, 15f, 15f, bodyPaint)
    }

    private fun drawDragon(canvas: Canvas, x: Float, y: Float) {
        // 龍族身體（深紅色）
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#8B0000")
            isAntiAlias = true
        }
        canvas.drawRoundRect(x - 45, y - 35, x + 45, y + 35, 20f, 20f, bodyPaint)

        // 龍頭（較長）
        canvas.drawRoundRect(x - 30, y - 70, x + 30, y - 35, 15f, 15f, bodyPaint)

        // 翅膀
        val wingPaint = Paint().apply {
            color = Color.parseColor("#4B0000")
            isAntiAlias = true
        }
        canvas.drawOval(x - 80, y - 50, x - 30, y + 10, wingPaint)
        canvas.drawOval(x + 30, y - 50, x + 80, y + 10, wingPaint)

        // 眼睛（火焰色）
        canvas.drawCircle(x - 12, y - 55, 7f, Paint().apply { color = Color.parseColor("#FF4500") })
        canvas.drawCircle(x + 12, y - 55, 7f, Paint().apply { color = Color.parseColor("#FF4500") })

        // 鼻孔（冒煙效果）
        canvas.drawCircle(x - 8, y - 40, 3f, Paint().apply { color = Color.BLACK })
        canvas.drawCircle(x + 8, y - 40, 3f, Paint().apply { color = Color.BLACK })

        // 尾巴
        canvas.drawRoundRect(x + 35, y + 20, x + 60, y + 40, 10f, 10f, bodyPaint)
    }

    private fun drawHealthBar(canvas: Canvas, monster: Monster, x: Float, y: Float) {
        val barWidth = 80f
        val barHeight = 8f

        // 更新血條進度
        healthBarProgress = monster.currentHp.toFloat() / monster.hp.toFloat()

        // 背景
        canvas.drawRoundRect(
            x - barWidth/2, y, x + barWidth/2, y + barHeight,
            4f, 4f, healthBarBgPaint
        )

        // 前景（血量）
        canvas.drawRoundRect(
            x - barWidth/2, y,
            x - barWidth/2 + barWidth * healthBarProgress, y + barHeight,
            4f, 4f, healthBarFgPaint
        )

        // 血量文字
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("${monster.currentHp}/${monster.hp}", x, y - 5, textPaint)
    }

    fun updateMonster(monster: Monster) {
        currentMonster = monster
        invalidate()
    }

    fun startAnimation() {
        if (isAnimating) return

        isAnimating = true
        attackAnimator = ValueAnimator.ofFloat(0f, 2f * PI.toFloat()).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                animationProgress = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stopAnimation() {
        isAnimating = false
        attackAnimator?.cancel()
        animationProgress = 0f
        invalidate()
    }
}

// 怪物傷害數字類
class MonsterDamageNumber(
    private val damage: Int,
    private var x: Float,
    private var y: Float
) {
    private var alpha = 255
    private var scale = 1f
    private var age = 0

    fun update() {
        age++
        y -= 2.5f // 向上漂浮
        x += (Math.random() * 1.5 - 0.75).toFloat() // 輕微搖擺
        alpha = maxOf(0, 255 - age * 6) // 漸漸透明
        scale = minOf(1.3f, 1f + age * 0.015f) // 輕微放大
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

        // 主要文字（橙色）
        paint.color = Color.parseColor("#FF4500")
        canvas.drawText("-$damage", 0f, 0f, paint)

        canvas.restore()
    }

    fun isExpired(): Boolean = alpha <= 0
}