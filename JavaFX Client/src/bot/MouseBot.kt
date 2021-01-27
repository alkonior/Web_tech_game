package bot

import com.google.common.collect.HashMultiset
import com.google.common.collect.Multiset
import field.CellValue
import field.GameField
import java.awt.Point
import java.util.*
import kotlin.math.abs
import  com.google.common.collect.TreeMultiset


class MouseBot(field: GameField, position: Point, target: Point) : SimpleBot(field, position, target) {

    var dist =
        MutableList<MutableList<Int>>(field.width) { MutableList<Int>(field.height) { Int.MAX_VALUE } };
    var aDist =
        MutableList<MutableList<Int>>(field.width) { MutableList<Int>(field.height) { Int.MAX_VALUE } };
    var checked =
        MutableList(field.width) { MutableList(field.height) { false } };


    private fun setDist(p: Point, dist_: Int) {
        dist[p.x][p.y] = dist_
        field[p.x, p.y].text = dist_.toString()
    }

    private fun setADist(p: Point, dist_: Int) {
        aDist[p.x][p.y] = dist_
        field[p.x, p.y].text = dist_.toString()
    }

    override var target: Point = target
        set(value) {
            if (target != value) {
                theWay.clear()
            }
            field = value
        }

    var aStarVoid = 4
    var aStarFloor = 2
    var way_len = 15
    var void_punish = 5

    private var moveDirections = listOf(
        Point(1, 0),
        Point(0, 1),
        Point(-1, 0),
        Point(0, -1),
    )

    class PointDist(var p: Point, var d: Int, var lastpd: PointDist? = null)

    var theWay = mutableListOf<Point>()


    fun aStarDist() {
        for (p in 0 until field.width)
            for (q in 0 until field.height)
                aDist[p][q] = 100000

        val point_queue: Queue<PointDist> = LinkedList<PointDist>()
        point_queue.add(PointDist(target, 0))
        while (point_queue.size > 0) {
            var addPoints: MutableSet<Point> = mutableSetOf()
            while (point_queue.size > 0) {
                var pd = point_queue.poll()!!
                run {
                    for (dir in moveDirections) {
                        if (!checked[(pd.p + dir).x][(pd.p + dir).y]) {
                            when (field[pd.p + dir].value) {
                                CellValue.WALL -> {
                                }
                                CellValue.FLOOR -> {
                                    if (aDist[(pd.p + dir).x][(pd.p + dir).y] > pd.d + aStarFloor * way_len) {
                                        setADist(pd.p + dir, pd.d + aStarFloor * way_len)
                                        addPoints.add(pd.p + dir)
                                    }
                                }
                                CellValue.VOID -> {
                                    if (aDist[(pd.p + dir).x][(pd.p + dir).y] > pd.d + aStarVoid * way_len) {
                                        setADist(pd.p + dir, pd.d + aStarVoid * way_len)
                                        addPoints.add(pd.p + dir)
                                    }
                                }
                                else -> {
                                }
                            }
                        }
                    }
                }
                checked[pd.p.x][pd.p.y] = true
            }
            for (p in addPoints) {
                point_queue.add(PointDist(p, aDist[p.x][p.y]))
            }

        }
    }

    override fun findWayTo(): Dirrections {
        if (position == target) {
            theWay.clear()
            return Dirrections.NOTHING
        }


        if (position !in theWay) {
            for (p in 0 until field.width)
                for (q in 0 until field.height)
                    checked[p][q] = false
            aStarDist()
            for (p in 0 until field.width)
                for (q in 0 until field.height)
                    checked[p][q] = false
            for (p in 0 until field.width)
                for (q in 0 until field.height)
                    dist[p][q] = 100000


            var checkPoints: TreeMultiset<PointDist> = TreeMultiset.create { o1, o2 -> o1.d - o2.d }


            setDist(position, 0)


            var point_queue: Queue<PointDist> = LinkedList<PointDist>()
            point_queue.add(PointDist(position, 0))


            while (point_queue.size > 0) {
                var addPoints: TreeMultiset<PointDist> = TreeMultiset.create { o1, o2 -> o1.d - o2.d }
                var pd = point_queue.first()
                if (checkPoints.size > 0)
                    if (pd.d > checkPoints.first().d) {
                        point_queue.remove(pd)
                        continue
                    }

                run {
                    for (dir in moveDirections) {
                        if (!checked[(pd.p + dir).x][(pd.p + dir).y])
                            when (field[pd.p + dir].value) {
                                CellValue.FLOOR -> {
                                    var distance = pd.d + way_len
                                    if (dist[(pd.p + dir).x][(pd.p + dir).y] > distance) {
                                        setDist(pd.p + dir, distance + way_len);
                                        var ii = 0
                                        while (addPoints.contains(PointDist(pd.p + dir, distance + ii, pd))) {
                                            ii++
                                        }
                                        addPoints.add(PointDist(pd.p + dir, distance + ii, pd))
                                        if ((pd.p + dir) == target) {
                                            checkPoints.add(PointDist(pd.p + dir, -10000, pd))
                                            break
                                        }
                                    }
                                }
                                CellValue.VOID -> {
                                    var distance =
                                        pd.d + void_punish * way_len + way_len + aDist[(pd.p + dir).x][(pd.p + dir).y]
                                    if (dist[(pd.p + dir).x][(pd.p + dir).y] > distance) {
                                        setDist(pd.p + dir, distance)
                                        var ii = 0
                                        while (checkPoints.contains(PointDist(pd.p + dir, distance + ii, pd))) {
                                            ii++
                                        }
                                        checkPoints.add(PointDist(pd.p + dir, distance + ii, pd))
                                    }
                                    if ((pd.p + dir) == target) {
                                        checkPoints.add(PointDist(pd.p + dir, -10000, pd))
                                        setDist(pd.p + dir, -1000);
                                        break
                                    }

                                }

                                else -> {
                                    if ((pd.p + dir) == target) {
                                        checkPoints.add(PointDist(pd.p + dir, -10000, pd))
                                        setDist(pd.p + dir, -1000);
                                        break
                                    }
                                }
                            }

                    }
                }

                checked[pd.p.x][pd.p.y] = true
                point_queue.remove(pd)

                if (addPoints.size > 0) {
                    if (checkPoints.size > 0) {
                        for (p in addPoints) {
                            if (p.d < checkPoints.first().d) {
                                var ii = 0
                                while (point_queue.contains(p)) {
                                    ii++
                                }
                                pd.d += ii
                                point_queue.add(p)
                            }
                        }
                    } else {
                        for (p in addPoints) {
                            var ii = 0
                            while (point_queue.contains(p)) {
                                ii++
                            }
                            pd.d += ii
                            point_queue.add(p)
                        }
                    }
                }
                addPoints.clear()

            }

            var t = checkPoints.first()
            var p = t.p
            while (p != position) {
                theWay.add(p)
                t = t.lastpd
                p = t.p
            }
            theWay.add(Point(position.x, position.y))
            theWay.reverse()
            field.ping()


        }

        //return Dirrections.NOTHING


        if ((field[target].value == CellValue.WALL) and (theWay.size == 2)) {
            return Dirrections.NOTHING
        }

        var ii = 0

        for (p in theWay) {
            if (p != position)
                ii++
            else
                break
        }
        var t = theWay[ii + 1]
        if (theWay[theWay.size - 1] == t) {
            theWay.clear()
        }

        var max1 = position.x - t.x
        var max2 = position.y - t.y
        if ((max1 == 0) and (max2 == 0))
            return Dirrections.NOTHING
        if (abs(max1) > abs(max2)) {
            if (max1 > 0) {
                return Dirrections.LEFT
            } else
                return Dirrections.RIGHT
        } else
            if (max2 > 0) {
                return Dirrections.UP
            } else
                return Dirrections.DOWN

    }
}


