package com.example.colortilebattletest

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF

class DragAnimation {
    private val endTime = 500L
    private var startTime = 0L
    var isExists = false

    private lateinit var pointF: PointF
    private lateinit var fromPointF: PointF
    private lateinit var toPointF: PointF
    private lateinit var direction: PointF

    fun init(fromPointF: PointF, toPointF: PointF) {
        this.pointF = fromPointF
        this.fromPointF = fromPointF
        this.toPointF = toPointF
        this.startTime = System.currentTimeMillis()
        direction = getDirection(toPointF, fromPointF)
    }

    fun draw(canvas: Canvas) {
        val paint = Paint()

        paint.color = Color.RED
        canvas.drawCircle(pointF.x, pointF.y, 30f, paint)
    }

    fun move() {
        val time = System.currentTimeMillis() - startTime
        if (time > endTime) isExists = false
        val ratio = time / endTime.toFloat()
        pointF = PointF(fromPointF.x * (1 - ratio) + toPointF.x * ratio, fromPointF.y * (1 - ratio) + toPointF.y * ratio)
        println(pointF)
    }

    fun getDirection(p1: PointF, p2: PointF): PointF {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        val length = getDistence(p1, p2)
        return PointF(dx / length, dy / length)
    }

    fun getDistence(p1: PointF, p2: PointF): Float {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return Math.sqrt(dx.toDouble() * dx.toDouble() + dy.toDouble() * dy.toDouble()).toFloat()
    }
}
