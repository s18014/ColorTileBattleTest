package com.example.colortilebattletest

import android.graphics.Canvas
import android.graphics.PointF

class DragSystem {
    private val SIZE = 16
    private val animations: Array<DragAnimation> = Array(SIZE, { it -> DragAnimation() })

    fun init() {
    }

    fun draw(canvas: Canvas) {
        for (i in animations.indices) {
            if (!animations[i].isExists) continue
            animations[i].draw(canvas)
        }
    }

    fun add(from: PointF, to: PointF) {
        for (i in animations.indices) {
            if (!animations[i].isExists) {
                animations[i].isExists = true
                animations[i].init(from, to)
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