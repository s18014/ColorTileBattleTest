package com.example.colortilebattletest

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Tile {
    companion object {
        const val PADDING = 5f
    }

    var isExists = false
    var type = Type.NONE

    private var left: Float = 0f
    private var top: Float = 0f
    private var right: Float = 0f
    private var bottom: Float = 0f

    enum class Type {
        RED,
        GREEN,
        BLUE,
        NONE
    }

    fun init(type: Type, left: Float, top: Float, right: Float, bottom: Float) {
        this.type = type
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    fun draw(canvas: Canvas) {
        val paint = Paint()
        when (type) {
            Type.RED -> paint.color = Color.parseColor("#ee5555")
            Type.GREEN -> paint.color = Color.parseColor("#55ee55")
            Type.BLUE -> paint.color = Color.parseColor("#5555ee")
            else -> return
        }
        canvas.drawRoundRect(left + PADDING, top + PADDING, right - PADDING, bottom - PADDING, 10f, 10f, paint)
    }

    fun move(left: Float, top: Float, right: Float, bottom: Float) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }
}
