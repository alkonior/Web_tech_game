package bot

import field.GameField
import java.awt.Point
import kotlin.math.abs

open class SimpleBot(val field:GameField,
                     var position:Point,
                     open var target:Point) {

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
                    NOTHING -> "huitebe"
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
        }


        open fun findWayTo(): Dirrections
        {
            var max1 = position.x-target.x
            var max2 = position.y-target.y
            if ((max1 == 0) and( max2==0))
                return Dirrections.NOTHING
            if (abs(max1) > abs(max2))
            {
                if (max1>0)
                {
                    return Dirrections.LEFT
                }else
                    return Dirrections.RIGHT
            }   else
                if (max2>0)
                {
                    return Dirrections.UP
                }else
                    return Dirrections.DOWN

        }



}