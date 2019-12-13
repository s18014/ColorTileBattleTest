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
import java.util.*

class Board: SurfaceHolder.Callback, View.OnTouchListener, Runnable {

    private val FPS = 60L
    private val BOARD_SIZE_X = 10
    private val BOARD_SIZE_Y = 13
    private var WINDOW_X: Float = 0f
    private var WINDOW_Y: Float = 0f
    private var POS_X: Float = 0f
    private var POS_Y: Float = 0f
    private var tileSize = 0f

    private var thread: Thread? = null
    private val holder: SurfaceHolder
    private val tileMap = Array(BOARD_SIZE_Y, { Array(BOARD_SIZE_X, { Tile() }) })
    private val dragSystem = DragSystem()
    private val tileManager = TileManager(Point(BOARD_SIZE_X, BOARD_SIZE_Y))

    constructor(surfaceView: SurfaceView) {
        this.holder = surfaceView.holder
        holder.addCallback(this)
        surfaceView.setOnTouchListener(this)
    }

    fun toWorldPoint(x: Float, y: Float): PointF {
        return PointF(x+POS_X, y+POS_Y)
    }

    fun toLocalPoint(x: Float, y: Float): PointF {
        return PointF(x-POS_X, y-POS_Y)
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, w: Int, h: Int) {
        WINDOW_X = w.toFloat()
        WINDOW_Y = h.toFloat()
        tileSize = WINDOW_X / BOARD_SIZE_X.toFloat()
        POS_Y = (WINDOW_Y - BOARD_SIZE_Y * tileSize) / 2f
        tileManager.init(PointF(WINDOW_X, WINDOW_Y))
        dragSystem.init()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        thread = null
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        for (y in tileMap) {
            for (x in y) {
                if (Math.random() < 0.7) {
                    x.isExists = true
                }
                when ((Math.random() * 3).toInt()) {
                    0 -> x.type = Tile.Type.RED
                    1 -> x.type = Tile.Type.GREEN
                    2 -> x.type = Tile.Type.BLUE
                }
            }
        }
        this.thread = Thread(this)
        this.thread?.start()
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                tileManager.onTouch(PointF(event.x, event.y))
                val pointF = toLocalPoint(event.x, event.y)
                val x = Math.floor((pointF.x / tileSize).toDouble()).toInt()
                val y = Math.floor((pointF.y / tileSize).toDouble()).toInt()
                if ((x >= BOARD_SIZE_X || x < 0) || (y >= BOARD_SIZE_Y || y < 0)) return false
                try {
                    // crossLineChecker(tileMap, x, y, BOARD_SIZE_X, BOARD_SIZE_Y)
                } catch (e: ArrayIndexOutOfBoundsException) {
                    e.printStackTrace()
                }
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
        val paint = Paint()
        canvas.drawColor(Color.WHITE)

        // 背景
        for (idxY in 0..BOARD_SIZE_Y + 4) {
            for (idxX in 0..BOARD_SIZE_X + 4) {
                if ((idxX + idxY) % 2 == 0) {
                    paint.color = Color.parseColor("#eeeeee")
                } else {
                    paint.color = Color.parseColor("#ffffff")
                }
                val pointF = toWorldPoint(idxX * tileSize - tileSize * 2, idxY * tileSize - tileSize * 2)
                canvas.drawRect(
                    pointF.x,
                    pointF.y,
                    pointF.x + tileSize,
                    pointF.y + tileSize,
                    paint)
            }
        }

        // タイル
        /*
        paint.color = Color.parseColor("#44ee44")
        for ((idxY, y) in tileMap.withIndex()) {
            for ((idxX, x) in y.withIndex()) {
                if (!x.exists) continue
               val padding = 5f
                val pointF = toWorldPoint(idxX * tileSize, idxY * tileSize)
                x.draw(
                    canvas,
                    pointF.x + padding,
                    pointF.y + padding,
                    pointF.x + tileSize - padding,
                    pointF.y + tileSize - padding
                )
            }
        }
         */

        tileManager.draw(canvas)
        dragSystem.draw(canvas)

        holder.unlockCanvasAndPost(canvas)
    }

    private fun crossLineChecker(tileMap: Array<Array<Tile>>, x: Int, y: Int, maxX: Int, maxY: Int) {
        val checkMap = Array(BOARD_SIZE_Y, { Array(BOARD_SIZE_X, { index -> CheckState.NONE})})

        // 上下左右に走査
        lineChecker(tileMap, x, y, 1, 0, maxX, maxY, checkMap)
        lineChecker(tileMap, x, y, -1, 0, maxX, maxY, checkMap)
        lineChecker(tileMap, x, y, 0, 1, maxX, maxY, checkMap)
        lineChecker(tileMap, x, y, 0, -1, maxX, maxY, checkMap)

        // checkMapから存在しているタイルを計算
        val tilesExists = mutableListOf<Tile>()
        for ((idxY, y) in checkMap.withIndex()) {
            for ((idxX, state) in y.withIndex()) {
                if (state == CheckState.EXISTS) {
                    tilesExists.add(tileMap[idxY][idxX])
                }
            }
        }

        // 存在しているタイルの色を集計
        val types: EnumMap<Tile.Type, Int> = EnumMap<Tile.Type, Int>(Tile.Type::class.java)
        for (tile in tilesExists) {
            types.put(tile.type, types.getOrDefault(tile.type, 0) + 1)
        }

        // 同色のタイルが２つ以上あれば非表示にする
        for (entry in types.entries) {
            if (entry.value > 1) {
                for (tile in tilesExists) {
                    if (tile.type == entry.key) {
                        tile.isExists = false
                    }
                }
            }
        }

    }

    private fun lineChecker(
        tileMap: Array<Array<Tile>>,
        x: Int,
        y: Int,
        dx: Int,
        dy: Int,
        maxX: Int,
        maxY: Int,
        checkMap: Array<Array<CheckState>>
    ) {
        if (tileMap[y][x].isExists) {
            checkMap[y][x] = CheckState.EXISTS
        } else {
            checkMap[y][x] = CheckState.CHECKED
        }
        // ベクトル(dx, dy)に走査
        if (x + dx < maxX && x + dx > -1 && y + dy < maxY && y + dy > -1 && !tileMap[y][x].isExists) {
            lineChecker(tileMap, x + dx, y + dy, dx, dy, maxX, maxY, checkMap)
        }
    }

    enum class CheckState {
        NONE,
        CHECKED,
        EXISTS
    }
}
