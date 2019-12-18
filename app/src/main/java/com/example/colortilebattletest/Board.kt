package com.example.colortilebattletest

import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View

class Board: SurfaceHolder.Callback, View.OnTouchListener, Runnable {
    companion object {
        const val FPS = 60L
        const val BOARD_SIZE_X = 10
        const val BOARD_SIZE_Y = 13
    }

    private var width: Float = 0f
    private var height: Float = 0f

    private var isActive = false
    private var thread: Thread? = null
    private var holder: SurfaceHolder? = null
    private val dragSystem = DragSystem()
    private val tileManager = TileManager(Point(BOARD_SIZE_X, BOARD_SIZE_Y))

    constructor(surfaceView: SurfaceView) {
        holder = surfaceView.holder
        holder?.addCallback(this)
        surfaceView.setOnTouchListener(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, p1: Int, w: Int, h: Int) {
        width = w.toFloat()
        height = h.toFloat()
        if (thread == null) {
            tileManager.init(PointF(width, height))
            dragSystem.init()
            thread = Thread(this)
            thread?.start()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        isActive = false
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                tileManager.onTouch(PointF(event.x, event.y))
            }
        }
        return true
    }

    override fun run() {
        isActive = true
        while (thread != null) {
            if (!isActive) {
                Thread.sleep(100)
                continue
            }
            draw()
            dragSystem.move()
            tileManager.move()
            Thread.sleep(1000 / FPS)
        }
    }

    private fun draw() {
        val canvas = holder?.lockCanvas()
        if (canvas == null) return
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)

        tileManager.draw(canvas)
        dragSystem.draw(canvas)

        holder?.unlockCanvasAndPost(canvas)
    }

    fun stop() {
        isActive = false
    }

    fun resume(surfaceView: SurfaceView) {
        holder = surfaceView.holder
        holder?.addCallback(this)
        surfaceView.setOnTouchListener(this)
        isActive = true
    }
}
