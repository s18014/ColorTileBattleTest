package com.example.colortilebattletest

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PointF
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View

class Board: SurfaceHolder.Callback, View.OnTouchListener, Runnable {

    private val FPS = 60L
    private val BOARD_SIZE_X = 10
    private val BOARD_SIZE_Y = 13
    private var WINDOW_X: Float = 0f
    private var WINDOW_Y: Float = 0f

    private var thread: Thread? = null
    private val holder: SurfaceHolder
    private val dragSystem = DragSystem()
    private val tileManager = TileManager(Point(BOARD_SIZE_X, BOARD_SIZE_Y))

    constructor(surfaceView: SurfaceView) {
        this.holder = surfaceView.holder
        holder.addCallback(this)
        surfaceView.setOnTouchListener(this)
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, w: Int, h: Int) {
        WINDOW_X = w.toFloat()
        WINDOW_Y = h.toFloat()
        tileManager.init(PointF(WINDOW_X, WINDOW_Y))
        dragSystem.init()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        thread = null
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        this.thread = Thread(this)
        this.thread?.start()
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                tileManager.onTouch(PointF(event.x, event.y))
                dragSystem.add(PointF(WINDOW_X / 2, 0f), PointF(event.x, event.y))
            }
        }
        return true
    }

    override fun run() {
        while (thread != null) {
            dragSystem.move()
            draw()
            Thread.sleep(1000 / FPS)
        }
    }

    private fun draw() {
        val canvas = holder.lockCanvas()
        canvas.drawColor(Color.WHITE)

        tileManager.draw(canvas)
        dragSystem.draw(canvas)

        holder.unlockCanvasAndPost(canvas)
    }
}
