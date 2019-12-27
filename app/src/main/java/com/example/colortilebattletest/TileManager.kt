package com.example.colortilebattletest

import android.graphics.*
import java.util.*

class TileManager {
    var boardSize: Point
    var tileSize: Float = 0f
    lateinit var boardPosition: PointF
    lateinit var windowSize: PointF
    lateinit var boardScreenSize: PointF
    lateinit var tiles: Array<Array<Tile>>
    private val dragSystem = DragSystem()

    enum class CheckState {
        NONE,
        CHECKED,
        EXISTS,
        DELETE,
        TOUCHED
    }

    constructor(boardSize: Point) {
        this.boardSize = boardSize
    }

    fun init(windowSize: PointF) {
        this.windowSize = windowSize
        tileSize = windowSize.x / boardSize.x.toFloat()
        boardScreenSize = PointF(boardSize.x * tileSize, boardSize.y * tileSize)

        // 空白を半分にして上下に分散
        boardPosition = PointF(0f, (windowSize.y - boardScreenSize.y) / 2f)
        tiles = Array(boardSize.y, { Array(boardSize.x, { Tile()})})
        for (idxY in tiles.indices) {
            for (idxX in tiles[idxY].indices) {
                val x = boardPosition.x + idxX * tileSize
                val y = boardPosition.y + idxY * tileSize
                var type = Tile.Type.GREEN
                when ((Math.random() * 3).toInt()) {
                    0 -> type = Tile.Type.RED
                    1 -> type = Tile.Type.GREEN
                    2 -> type = Tile.Type.BLUE
                }
                tiles[idxY][idxX].init(type, x, y, x + tileSize, y + tileSize)
                if (Math.random() < 0.7) {
                    tiles[idxY][idxX].isExists = true
                }
            }
        }
    }

    fun move() {
        dragSystem.move()
    }

    fun draw(canvas: Canvas) {
        // TODO: 背景も別クラスにする
        val paint = Paint()

        for (idxY in 0..boardSize.y + 5) {
            for (idxX in 0..boardSize.x + 1) {
                if ((idxX + idxY) % 2 == 0) {
                    paint.color = Color.parseColor("#eeeeee")
                } else {
                    paint.color = Color.parseColor("#ffffff")
                }
                val pointF = toWorldPoint(PointF(idxX * tileSize - tileSize * 2f, idxY * tileSize - tileSize * 3f))
                canvas.drawRect(
                    pointF.x,
                    pointF.y,
                    pointF.x + tileSize,
                    pointF.y + tileSize,
                    paint)
            }
        }



        dragSystem.draw(canvas)
        for (y in tiles.indices) {
            for (x in tiles[y].indices) {
                if (!tiles[y][x].isExists) continue
                tiles[y][x].draw(canvas)
            }
        }
    }

    fun onTouch(p: PointF) {
        val index = toArrayIndex(toLocalPoint(p))
        index?.let {
            val result = crossLineChecker(tiles, it.x, it.y, boardSize.x, boardSize.y)
            for (idxY in result.indices) {
                for (idxX in result[idxY].indices) {
                    val state = result[idxY][idxX]
                    when (state) {
                        CheckState.CHECKED, CheckState.TOUCHED, CheckState.EXISTS -> {
                            val from = PointF(idxX * tileSize, idxY * tileSize)
                            val to = PointF(it.x * tileSize, it.y * tileSize)
                            dragSystem.add(toWorldPoint(from), toWorldPoint(to), tileSize, Tile.Type.NONE)
                        }
                    }
                }
            }
            deleteTiles(result)
        }
    }

    fun toWorldPoint(p: PointF): PointF {
        return PointF(p.x + boardPosition.x, p.y + boardPosition.y)
    }

    fun toLocalPoint(p: PointF): PointF {
        return PointF(p.x - boardPosition.x, p.y - boardPosition.y)
    }

    // ローカル座標から配列の位置を取得
    fun toArrayIndex(p: PointF): Point? {
        val x = Math.floor((p.x / tileSize).toDouble()).toInt()
        val y = Math.floor((p.y / tileSize).toDouble()).toInt()
        if (x >= boardSize.x || y >= boardSize.y || x < 0 || y < 0) return null
        return Point(x, y)
    }

    private fun crossLineChecker(tileMap: Array<Array<Tile>>, x: Int, y: Int, maxX: Int, maxY: Int): Array<Array<CheckState>> {
        val checkMap = Array(boardSize.y, { Array(boardSize.x, { CheckState.NONE }) })

        // 上下左右に走査
        lineChecker(tileMap, x, y, 1, 0, maxX, maxY, checkMap)
        lineChecker(tileMap, x, y, -1, 0, maxX, maxY, checkMap)
        lineChecker(tileMap, x, y, 0, 1, maxX, maxY, checkMap)
        lineChecker(tileMap, x, y, 0, -1, maxX, maxY, checkMap)
        checkMap[y][x] = CheckState.TOUCHED

        // TODO: 探索結果の処理は別関数で
        // checkMapから存在しているタイルを取得
        /*
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
         */
        return checkMap
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

    private fun deleteTiles(foundTiles: Array<Array<CheckState>>) {
        val tileEraseList = mutableListOf<Point>()
        var touchPoint: Point? = null
        // tileのtypeをカウント
        val tileTypeCount: EnumMap<Tile.Type, Int> = EnumMap<Tile.Type, Int>(Tile.Type::class.java)
        for (idxY in foundTiles.indices) {
            for (idxX in foundTiles[idxY].indices) {
                if (foundTiles[idxY][idxX] == CheckState.TOUCHED) {
                    touchPoint = Point(idxX, idxY)
                }
                if (foundTiles[idxY][idxX] == CheckState.EXISTS) {
                    val type = tiles[idxY][idxX].type
                    tileTypeCount.put(type, tileTypeCount.getOrDefault(type, 0) + 1)
                }
            }
        }
        if (touchPoint == null) return

        // 同じtypeが2つ以上あったら削除フラグと削除アニメーションを作成
        for (idxY in foundTiles.indices) {
            for (idxX in foundTiles[idxY].indices) {
                if (foundTiles[idxY][idxX] == CheckState.EXISTS) {
                    val type = tiles[idxY][idxX].type
                    if (tileTypeCount.getOrDefault(type, 0) > 1) {
                        tiles[idxY][idxX].isExists = false
                        val from = PointF(idxX * tileSize, idxY * tileSize)
                        val to = PointF(touchPoint.x * tileSize, touchPoint.y * tileSize)
                        dragSystem.add(toWorldPoint(from), toWorldPoint(to), tileSize, tiles[idxY][idxX].type)
                        tileEraseList.add(Point(idxX, idxY))
                    }
                }
            }
        }

        for (p in tileEraseList) {
            val count = Point(touchPoint.x - p.x, touchPoint.y - p.y)
            println(count)
            for (idxY in p.x..count.x) {
                for (idxX in p.y..count.y) {
                    print("$idxX, ")
                }
                println()
            }
        }
    }
}
