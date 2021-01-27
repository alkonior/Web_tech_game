package bot

import field.CellValue
import field.GameField
import java.awt.Point
import kotlin.math.abs

open class SimpleBot(val field:GameField,
                     var position:Point,
                     open var target:Point) {

    protected operator fun Point.plus(point: Point): Point {
        return Point(this.x + point.x, this.y + point.y)
    }


    enum class Dirrections{
            LEFT,
            RIGHT,
            UP,
            DOWN,
            NOTHING;

            override fun  toString():String
            {
                return when(this)
                {
                    LEFT -> "202"
                    RIGHT -> "203"
                    DOWN -> "204"
                    UP -> "205"
                    NOTHING -> "1488"
                }
            }

            fun toInt():Int
            {
                return when(this)
                {
                    LEFT -> 1
                    RIGHT -> 2
                    DOWN -> 3
                    UP -> 4
                    NOTHING -> 0
                }
            }
            fun toDir():Point{
                return when(this)
                {
                    LEFT -> Point(-1,0)
                    RIGHT -> Point(1,0)
                    DOWN -> Point(0,1)
                    UP -> Point(0,-1)
                    NOTHING -> Point(0,0)
                }
            }
        }


        open fun findWayTo(): Dirrections
        {
            var max1 = position.x-target.x
            var max2 = position.y-target.y
            if ((max1 == 0) and( max2==0))
                return Dirrections.NOTHING
            var res =
            if (abs(max1) > abs(max2))
            {
                if (max1>0)
                {
                    Dirrections.LEFT
                }else
                    Dirrections.RIGHT
            }   else
                if (max2>0)
                {
                    Dirrections.UP
                }else
                    Dirrections.DOWN

            if  (field[position+res.toDir()].value==CellValue.WALL)
            {
                return Dirrections.NOTHING
            }else
                return res

        }



}