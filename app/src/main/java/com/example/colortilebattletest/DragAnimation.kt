package com.example.colortilebattletest

import android.graphics.*

class DragAnimation {
    private val endTime = 200L
    private var startTime = 0L
    var isExists = false
    var size = 0f

    private lateinit var pointF: PointF
    private lateinit var fromPointF: PointF
    private lateinit var toPointF: PointF
    private lateinit var type: Tile.Type
    private lateinit var tile: Tile

    fun init(fromPointF: PointF, toPointF: PointF, size: Float, type: Tile.Type) {
        this.pointF = fromPointF
        this.fromPointF = fromPointF
        this.toPointF = toPointF
        this.type = type
        this.size = size
        this.startTime = System.currentTimeMillis()
        this.tile = Tile()
        this.tile.init(type, fromPointF.x, fromPointF.y, fromPointF.x + size, fromPointF.y + size)
    }

    fun draw(canvas: Canvas) {
        // TODO: 探索した場所に黒点を作成、別にクラスにする予定
        if (tile.type == Tile.Type.NONE) {
            val time = System.currentTimeMillis() - startTime
            var ratio = time / endTime.toFloat()
            if (ratio > 1) ratio = 1f
            val paint = Paint()
            val p0 = fromPointF
            val p1 = toPointF
            val x = (1 - ratio) * p0.x + ratio * p1.x
            val y = (1 - ratio) * p0.y + ratio * p1.y
            paint.color = Color.argb(((1 - ratio) * 255).toInt(), 100, 100, 100)
            canvas.drawCircle(x + size / 2, y + size / 2, 15f, paint)
        }
        tile.draw(canvas)
    }

    fun move() {
        val time = System.currentTimeMillis() - startTime
        if (time > endTime) isExists = false
        var ratio = time / endTime.toFloat()
        val p0 = fromPointF
        val p1 = toPointF
        val x = (1 - ratio) * p0.x + ratio * p1.x
        val y = (1 - ratio) * p0.y + ratio * p1.y
        tile.move(x + 5f, y + 5f, x + size - 5f, y + size - 5f)
        pointF = PointF(x, y)
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
