package field

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import java.awt.Point

enum class CellValue {
    VOID,
    FLOOR,
    WALL,
    EROOR,
    EXIT
}

fun  IntToCell(i:Int): CellValue {
    return when (i){
        0 -> CellValue.FLOOR
        1 -> CellValue.WALL
        2 -> CellValue.EXIT
        1488 -> CellValue.EROOR
        else -> CellValue.FLOOR
    }
}

enum class MouseValue {
    RED,
    BLUE,
    GREEN,
    YELLOW
}

class Mouse(var p:Point,var color:MouseValue)

class GameField(var width: Int, var height: Int) : Observable {



    public class CellInfo(var value: CellValue, var shadow: Int, var text:String) {
        constructor(value: CellValue,  shadow: Int):this(value,shadow,""){

        }
    }

    private val listeners = mutableMapOf<Int, InvalidationListener>();

    private var field =
        MutableList<MutableList<CellInfo>>(width) { MutableList<CellInfo>(height) { CellInfo(CellValue.VOID, 1) } };

    public var players_position = mutableListOf<Mouse>()

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
                field[i][j].value = CellValue.VOID
            }
        }
    }


    constructor() : this(0, 0) {
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
    }

    operator fun get(p:Point): CellInfo {
        return this[p.x,p.y]
    }

    operator fun set(p:Point, value: CellInfo) {
        this[p.x,p.y] = value;
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

    fun ping()
    {
        listeners.forEach {listener ->
            listener.value.invalidated(this);
        }
    }

    fun clear()
    {
        this.players_position.clear()
        this.field.clear()
        this.listeners.clear()
    }

}
