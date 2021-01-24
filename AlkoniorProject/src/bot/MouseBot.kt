package bot

import field.CellValue
import field.GameField
import java.awt.Point
import java.util.*
import kotlin.math.abs


class MouseBot(field: GameField, position: Point, target: Point) : SimpleBot(field, position, target) {

    var dist =
        MutableList<MutableList<Int>>(field.width) { MutableList<Int>(field.height) { Int.MAX_VALUE } };
    var checked =
        MutableList(field.width) { MutableList(field.height) { false } };
    var colors =
        MutableList(field.width) { MutableList(field.height) { 0 } };

    private fun setDist(p: Point, dist_: Int) {
        field[p].text = dist_.toString()
        dist[p.x][p.y] = dist_
    }

    override var target:Point = target
    set(value) {
        if (target!=value)
            theWay.clear()
        field = value
    }

    var aStar = 500
    var way_len = 10
    var void_punish = 100
    var color_punish = 100000

    private var moveDirections = listOf(
        Point(1, 0),
        Point(0, 1),
        Point(-1, 0),
        Point(0, -1),
    )

    class PointDist(var p: Point, var d: Int, var dir: Point, var lastpd: PointDist? = null)

    var theWay = mutableListOf<Point>()

    fun aStarAdition(p: Point): Int {
        return abs(abs(p.x - target.x) + abs(p.y - target.y))
    }

    fun setColor(x:Int,y:Int,color:Int)
    {
        colors[x][y] = color
        if (field[x+1,y].value==CellValue.VOID)
            if (colors[x+1][y]!= color)
            {
                setColor(x+1,y,color)
            }
        if (field[x-1,y].value==CellValue.VOID)
            if (colors[x-1][y]!= color)
            {
                setColor(x-1,y,color)
            }
        if (field[x,y+1].value==CellValue.VOID)
            if (colors[x][y+1]!= color)
            {
                setColor(x,y+1,color)
            }
        if (field[x,y-1].value==CellValue.VOID)
            if (colors[x][y-1]!= color)
            {
                setColor(x,y-1,color)
            }
    }


    fun colorise()
    {
        for (p in 1..30)
            for (q in 1..30)
                colors[p][q] = 0
        var color = 1
        for (p in 1..30)
            for (q in 1..30) {
                if (field[p, q].value == CellValue.VOID) {
                    if (colors[p][q] == 0) {
                        color++
                        setColor(p, q, color)
                    }
                }
            }
    }

    override fun findWayTo(): Dirrections {
        if (position == target)
        {
            return Dirrections.NOTHING
        }


        if (position !in theWay) {
            for (p in 1..30)
                for (q in 1..30)
                    setDist(Point(p,q),10000000)
            for (p in 1..30)
                for (q in 1..30)
                    checked[p][q] = false
            colorise()

            var checkPoints: TreeSet<PointDist> = TreeSet<PointDist> { o1, o2 -> o1.d - o2.d }


            setDist(position, 0)


            var point_queue: Queue<PointDist> = LinkedList<PointDist>()
            point_queue.add(PointDist(position, 0, Point(0, 0)))



            while (point_queue.size > 0) {
                var addPoints = mutableListOf<PointDist>()
                var min_dist = 100000
                for (pd in point_queue) {
                    checked[pd.p.x][pd.p.y] = true
                    for (dir in moveDirections) {
                        when (field[pd.p + dir].value) {
                            CellValue.FLOOR -> {
                                var distance =  pd.d + way_len
                                if ((dist[(pd.p + dir).x][(pd.p + dir).y] > distance + aStar*aStarAdition(pd.p + dir) ) and (! checked[(pd.p + dir).x][(pd.p + dir).y])) {
                                    setDist(pd.p + dir, distance + aStar*aStarAdition(pd.p + dir));
                                    addPoints.add(PointDist(pd.p + dir, distance, dir, pd))
                                    min_dist = distance
                                    if ((pd.p + dir) == target)
                                    {
                                        checkPoints.add(PointDist(pd.p + dir, - 10000, dir, pd))
                                    }

                                }
                            }
                            CellValue.VOID -> {
                                var distance =  pd.d + void_punish*way_len + way_len
                                if (colors[target.x][target.y]!=colors[(pd.p + dir).x][(pd.p + dir).y])
                                {
                                    distance+=color_punish*way_len
                                }
                                if ((dist[(pd.p + dir).x][(pd.p + dir).y] > distance + aStar*aStarAdition(pd.p + dir) )and (! checked[(pd.p + dir).x][(pd.p + dir).y])) {
                                    setDist(pd.p + dir, distance);
                                    checkPoints.add(PointDist(pd.p + dir, distance + aStar*aStarAdition(pd.p + dir) , dir, pd))
                                    checked[(pd.p + dir).x][(pd.p + dir).y] = true
                                }
                            }
                            else -> {
                                if ((pd.p + dir) == target)
                                {
                                    checkPoints.add(PointDist(pd.p + dir, pd.d - 10000, dir, pd))
                                    setDist(pd.p + dir, - 1000);
                                    checked[(pd.p + dir).x][(pd.p + dir).y] = true
                                }
                            }
                        }
                    }
                }

                point_queue.clear()
                if (checkPoints.size > 0){
                    if (min_dist < checkPoints.first().d) {
                        point_queue.addAll(addPoints)
                    }
                }
                    else
                    point_queue.addAll(addPoints)

            }

            var t = checkPoints.first()
            var p = t.p
            while (p != position) {
                theWay.add(p)
                t = t.lastpd
                p = t.p
            }
            theWay.add(Point(position.x,position.y))
            theWay.reverse()
            field.ping()
        }

        if ((field[target].value==CellValue.WALL) and (theWay.size == 2 ))
        {
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
        if (theWay[theWay.size-1] == t)
        {
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


private operator fun Point.plus(point: Point): Point {
    return Point(this.x + point.x, this.y + point.y)
}
