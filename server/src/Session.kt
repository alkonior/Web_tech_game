import kotlin.math.abs
import kotlin.random.Random

class Session(_status: Status) {
    var playerCount: Int = 0
    var players: MutableMap<Int, Player> = mutableMapOf()
    var id: Int = 0
    private var map: Map = Map("rand")
    var turn: Int = 0
    var status: Status
    var ready: Int = 0

    private class Map(_seed: String) {
        var random: Random = Random(_seed.hashCode())
        var height: Int = 31
        var width: Int = 31

        var map: MutableList<MutableList<Int>> = MutableList(height) { MutableList(width) { 1 } }

        fun mazeMake() {
            var x: Int = 3
            var y: Int = 3
            var c: Int
            var a: Int = 0

            while (a < 100) {
                map[y][x] = 0
                a++
                while (true) {
                    c = abs(random.nextInt() % 4)
                    when (c) {
                        0 -> {
                            if (y != 1) {
                                if (map[y - 2][x] == 1) { //Путь вверх
                                    map[y - 1][x] = 0
                                    map[y - 2][x] = 0
                                    y -= 2
                                }
                            }
                        }
                        1 -> {
                            if (y != height - 2) {
                                if (map[y + 2][x] == 1) { //Вниз
                                    map[y + 1][x] = 0
                                    map[y + 2][x] = 0
                                    y += 2
                                }
                            }
                        }
                        2 -> {
                            if (x != 1) {
                                if (map[y][x - 2] == 1) { //Налево
                                    map[y][x - 1] = 0
                                    map[y][x - 2] = 0
                                    x -= 2

                                }
                            }
                        }

                        3 -> {
                            if (x != width - 2) {
                                if (map[y][x + 2] == 1) { //Направо
                                    map[y][x + 1] = 0
                                    map[y][x + 2] = 0
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
                    } while (map[y][x] != 0)
                }
            }
        }

        private fun deadend(x: Int, y: Int): Boolean {
            var a: Int = 0

            if (x != 1) {
                if (map[y][x - 2] == 0)
                    a += 1
            } else a += 1

            if (y != 1) {
                if (map[y - 2][x] == 0)
                    a += 1
            } else a += 1

            if (x != width - 2) {
                if (map[y][x + 2] == 0)
                    a += 1
            } else a += 1

            if (y != height - 2) {
                if (map[y + 2][x] == 0)
                    a += 1
            } else a += 1

            return (a == 4)
        }


    }

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

    private fun generateMap(): Map {
        var map: Map = Map("${Random.nextInt()}")
        var i = 1
        map.mazeMake()
        while (map.map[1][1] != 0 || map.map[29][29] != 0 || map.map[1][29] != 0 || map.map[29][1] != 0
            || map.map[15][15] != 0
        ) {
            map = Map("${Random.nextInt()}")
            map.mazeMake()
        }
        map.map[15][15] = 2
        return map
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
        map = generateMap()
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
            map.map[x.pos[0]][x.pos[1]] += color
            x.write(
                "510 $color ${x.pos[0]} ${x.pos[1]} " +
                        "${map.map[x.pos[0] - 1][x.pos[1]]} ${map.map[x.pos[0] + 1][x.pos[1]]} " +
                        "${map.map[x.pos[0]][x.pos[1] + 1]} " + "${map.map[x.pos[0]][x.pos[1] - 1]} " +
                        "${map.map[x.pos[0]][x.pos[1]]}"
            )
            x.color = color++
        }
        play()
    }

    private fun play() {
        while (true) {
            ready = 0
            for (x in players.values) {
                if (x.ready) {
                    ready++
                }
                if (ready == playerCount) {
                    doTurn()
                    break
                }
            }
        }
    }

    private fun doTurn() {
        for (x in players.values) {
            if (map.map[x.transPos[0]][x.transPos[1]] != 1) {
                map.map[x.pos[0]][x.pos[1]] -= x.color
                x.pos = arrayOf(x.transPos[0], x.transPos[1])
                map.map[x.pos[0]][x.pos[1]] += x.color
            }
        }
        newTurn()
    }

    private fun newTurn() {
        for (x in players.values) {
            x.write(
                "777 ${x.pos[0]} ${x.pos[1]} " +
                        "${map.map[x.pos[0] - 1][x.pos[1]]} ${map.map[x.pos[0] + 1][x.pos[1]]} " +
                        "${map.map[x.pos[0]][x.pos[1] + 1]} " + "${map.map[x.pos[0]][x.pos[1] - 1]} " +
                        "${map.map[x.pos[0]][x.pos[1]]}"
            )
            x.ready = false
        }
    }
}