package com.example.colortilebattletest

import android.graphics.Canvas
import android.graphics.PointF

class DragSystem {
    companion object {
        const val SIZE = 100
    }

    private val animations: Array<DragAnimation> = Array(SIZE, { it -> DragAnimation() })

    fun init() {
    }

    fun draw(canvas: Canvas) {
        for (i in animations.indices) {
            if (!animations[i].isExists) continue
            animations[i].draw(canvas)
        }
    }

    fun add(from: PointF, to: PointF, size: Float, type: Tile.Type) {
        for (i in animations.indices) {
            if (!animations[i].isExists) {
                animations[i].isExists = true
                animations[i].init(from, to, size, type)
                return
            }
        }
    }

    fun move() {
        for (i in animations.indices) {
            if (!animations[i].isExists) continue
            animations[i].move()
        }
    }
}