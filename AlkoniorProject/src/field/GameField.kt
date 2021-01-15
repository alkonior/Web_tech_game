package field

import javafx.beans.InvalidationListener
import javafx.beans.Observable

enum class CellValue {
    VOID,
    FLOOR,
    WALL,
    EXIT,
    RED,
    GREEN,
    BLUE,
    YELLOW,

}

class GameField(var width: Int, var height: Int) : Observable {


    public class CellInfo(var value: CellValue, var shadow: Int) {
    }

    private val listeners = mutableMapOf<Int, InvalidationListener>();

    private var field =
        MutableList<MutableList<CellInfo>>(width) { MutableList<CellInfo>(height) { CellInfo(CellValue.VOID, 0) } };


    init {
        for (i in 0..(width - 1)) {
            field[i][0].value = CellValue.WALL;
            field[i][height - 1].value = CellValue.WALL;
        }
        for (j in 0..(height - 1)) {
            field[0][j].value = CellValue.WALL;
            field[width - 1][j].value = CellValue.WALL;
        }
        for (i in 1..(width - 2)) {
            for (j in 1..(height - 2)) {
                field[i][j].value = CellValue.FLOOR
            }
        }
        field[5][6].value = CellValue.RED
        field[5][7].value = CellValue.RED
    }


    constructor() : this(10, 10) {

    }


    operator fun get(i: Int, j: Int): CellInfo {
        if ((0 <= i) and (i < width)) {
            if ((0 <= j) and (j < height)) {
                return field[i][j]
            }
        }
        return CellInfo(CellValue.VOID, 0);
    }

    operator fun set(i: Int, j: Int, value: CellInfo) {
        field[i][j] = value;
        listeners.forEach {listener ->
                listener.value.invalidated(this);
        }
    }

    override fun addListener(p0: InvalidationListener?) {
        if (p0 != null) {
            listeners.set(p0.hashCode(), p0);
        }
    }

    override fun removeListener(p0: InvalidationListener?) {
        if (p0 != null) {
            if (listeners.containsKey(p0.hashCode())) {
                listeners.remove(p0.hashCode())
            }
        }
    }

}
