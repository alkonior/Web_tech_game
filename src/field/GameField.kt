package field

import javafx.beans.InvalidationListener
import javafx.beans.Observable

class GameField(var width:Int, var height:Int, ) : Observable {

    private var field: Array<Array<Int>> = Array(width) { Array<Int>(height) { 0 } }

    constructor():this(10,10)
    {
    }
    override fun addListener(p0: InvalidationListener?) {
        TODO("Not yet implemented")
    }

    override fun removeListener(p0: InvalidationListener?) {
        TODO("Not yet implemented")
    }

    fun get(i: Int, j:Int):Int
    {
        if ((0<=i) and (i<width))
        {
            if ((0<=j) and (j<height))
            {
                return field[i][j]
            }
        }
        return -1;
    }

}
