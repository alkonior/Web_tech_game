import java.awt.Point
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.timerTask
import kotlin.random.Random

class Session(_status: Status) {
    var playerCount: Int = 0
    var players: MutableMap<Int, Player> = mutableMapOf()
    var id: Int = 0
    private var maze: Maze = Maze("rand")
    var turn: Int = 0
    var status: Status
    var ready: Int = 0
    private val calculating: AtomicBoolean = AtomicBoolean(false)


    enum class Status {
        IDLING,
        LOBBY,
        INGAME,
        FINISHED
    }

    init {
        status = _status
    }

    //Конструктор не Idle сессии
    constructor(_status: Status, _id: Int, _player: Player) : this(_status) {
        id = _id
        _player.session = this
        _player.status = Player.Status.valueOf(status.name)
        playerCount++
        players[_player.id] = _player
    }

    private fun generateMap(): Maze {
        var maze: Maze = Maze("${Random.nextInt()}") // <---------- СИД СЮДА
        maze.mazeMake()
        while (maze[1, 1] != 0 || maze[29, 29] != 0 || maze[1, 29] != 0 || maze[29, 1] != 0
            || maze[15, 15] != 0
        ) {
            maze = Maze("${Random.nextInt()}")
            maze.mazeMake()
        }
        maze.maze[15][15] = 2
        return maze
    }

    //Добавление игрока в сессию
    fun addPlayer(_player: Player): String {
        //Проверка на заполненность сессии, если она игровая
        if (status != Status.IDLING && playerCount == 4)
            return "507"
        _player.session = this
        _player.status = Player.Status.valueOf(status.name)
        playerCount++
        players.put(_player.id, _player)
        return "509"
    }

    //Удаление игрока из данной сессии
    fun removePlayer(_player: Player) {
        playerCount--
        players.remove(_player.id)
        if (_player.status == Player.Status.LOBBY || _player.status == Player.Status.READY) {
            ready = 0
            for (x in players.values) {
                x.status = Player.Status.valueOf(status.name)
            }
        }
    }

    fun setupGame() {
        //Исторически сварилось, что цвета игроков и соответственно стартовая точка передается от 3 до 6
        var color = 3
        maze = generateMap()
        status = Status.INGAME
        ready = 0
        for (x in players.values) {
            x.status = Player.Status.INGAME
            when (color) {
                3 -> x.pos = arrayOf(1, 1)
                4 -> x.pos = arrayOf(1, 29)
                5 -> x.pos = arrayOf(29, 1)
                6 -> x.pos = arrayOf(29, 29)
            }
            x.transPos = arrayOf(x.pos[0], x.pos[1])
            maze.maze[x.pos[0]][x.pos[1]] += color
            x.write(
                "510 $color ${maze.width} ${maze.height} ${x.pos[0]} ${x.pos[1]} " +
                        "${maze[x.pos[0] - 1, x.pos[1]]} ${maze[x.pos[0] + 1, x.pos[1]]} " +
                        "${maze[x.pos[0], x.pos[1] + 1]} " + "${maze[x.pos[0], x.pos[1] - 1]} " +
                        "${maze[x.pos[0], x.pos[1]]}"
            )
            x.color = color++
        }
        play()
    }

    private fun play() {
        val running: AtomicBoolean = AtomicBoolean(false)
        var currentTimer: Timer = Timer(false)
        while (true) {
            if (!running.get()) {
                running.set(true)
                println("Idle таймер на $turn ход")
                currentTimer = Timer("Game $turn $id", false)
                currentTimer.schedule(timerTask { if(!calculating.get()) { doTurn(); running.set(false);
                    calculating.set(false)} }, 5500)
            }
            ready = 0
            for (x in players.values) {
                if (x.ready) {
                    ready++
                }
            }
            if (ready == playerCount && !calculating.get()) {
                currentTimer.cancel()
                println("Ручной ввод на $turn")
                running.set(false)
                doTurn()
                calculating.set(false)
            }
        }
    }

    private fun doTurn() {
        calculating.set(true)
        for (x in players.values) {
            if (maze[x.transPos[0], x.transPos[1]] != 1) {
                maze.maze[x.pos[0]][x.pos[1]] -= x.color
                x.pos = arrayOf(x.transPos[0], x.transPos[1])
                maze.maze[x.pos[0]][x.pos[1]] += x.color
            }
        }
        newTurn()
    }

    private fun newTurn() {
        for (x in players.values) {
            var msg: String = "777 $turn ${x.pos[0]} ${x.pos[1]} " +
                    "${maze[x.pos[0] - 1, x.pos[1]]} ${maze[x.pos[0] + 1, x.pos[1]]} " +
                    "${maze[x.pos[0], x.pos[1] + 1]} " + "${maze[x.pos[0], x.pos[1] - 1]} " +
                    "${maze[x.pos[0], x.pos[1]]}"
            val positions = listOf(
                Point(x.pos[0] - 1, x.pos[1]), Point((x.pos[0] + 1), x.pos[1]),
                Point(x.pos[0], (x.pos[1] + 1)), Point(x.pos[0], (x.pos[1] - 1)),
                Point(x.pos[0], x.pos[1])
            )
            for (y in players.values) {
                if (Point(y.pos[0], y.pos[1]) in positions) {
                    msg += " ${y.pos[0]} ${y.pos[1]}"
                } else {
                    msg += " 0 0"
                }
            }
            x.write(msg)
            x.ready = false
        }
        turn++
    }
}