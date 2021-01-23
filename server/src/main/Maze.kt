package main

import kotlin.math.abs
import kotlin.random.Random

class Maze(_seed: String) {
    var random: Random = Random(_seed.hashCode())
    //Размеры ТОЛЬКО нечетные.
    //Лабиринт ВСЕГДА квадратный.
    val height: Int = 31
    val width: Int = 31

    var maze: MutableList<MutableList<Int>> = MutableList(height) { MutableList(width) { 1 } }

    operator fun get(x: Int, y: Int): Int {
        return maze[x][y]
    }

    fun mazeMake() {
        var x: Int = 3
        var y: Int = 3
        var c: Int
        var a: Int = 0

        while (a < 100) {
            maze[y][x] = 0
            a++
            while (true) {
                c = abs(random.nextInt() % 4)
                when (c) {
                    0 -> {
                        if (y != 1) {
                            if (maze[y - 2][x] == 1) { //Путь вверх
                                maze[y - 1][x] = 0
                                maze[y - 2][x] = 0
                                y -= 2
                            }
                        }
                    }
                    1 -> {
                        if (y != height - 2) {
                            if (maze[y + 2][x] == 1) { //Вниз
                                maze[y + 1][x] = 0
                                maze[y + 2][x] = 0
                                y += 2
                            }
                        }
                    }
                    2 -> {
                        if (x != 1) {
                            if (maze[y][x - 2] == 1) { //Налево
                                maze[y][x - 1] = 0
                                maze[y][x - 2] = 0
                                x -= 2

                            }
                        }
                    }

                    3 -> {
                        if (x != width - 2) {
                            if (maze[y][x + 2] == 1) { //Направо
                                maze[y][x + 1] = 0
                                maze[y][x + 2] = 0
                                x += 2
                            }
                        }
                    }
                }
                if (deadend(x, y)) {
                    break
                }
            }
            if (deadend(x, y)) {
                do {
                    x = 2 * (abs(random.nextInt()) % ((width - 1) / 2)) + 1
                    y = 2 * (abs(random.nextInt()) % ((height - 1) / 2)) + 1
                } while (maze[y][x] != 0)
            }
        }
    }

    private fun deadend(x: Int, y: Int): Boolean {
        var a: Int = 0

        if (x != 1) {
            if (maze[y][x - 2] == 0)
                a += 1
        } else a += 1

        if (y != 1) {
            if (maze[y - 2][x] == 0)
                a += 1
        } else a += 1

        if (x != width - 2) {
            if (maze[y][x + 2] == 0)
                a += 1
        } else a += 1

        if (y != height - 2) {
            if (maze[y + 2][x] == 0)
                a += 1
        } else a += 1

        return (a == 4)
    }
}