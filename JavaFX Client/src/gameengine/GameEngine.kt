package gameengine

import bot.MouseBot
import bot.SimpleBot
import field.*
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import server.Server
import tornadofx.plusAssign
import java.util.*
import kotlinx.coroutines.*
import tornadofx.cleanBind
import java.awt.Point
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class GameEngine : EventListener {
    public enum class GameStage {
        ServerConnection,
        LobbyConnection,
        Lobby,
        Game,
        WinScreen,
        Die
    }


    var still_reading_server = AtomicBoolean(false)
    var sp_mod: Boolean = false

    public var current_stage = SimpleObjectProperty(GameStage.ServerConnection);

    public var field = GameField();
    public var cur_second = SimpleIntegerProperty(0);
    private var timer = Timer("game timer", false)
    private val timerTask = object : TimerTask() {
        override fun run() {
            update_timer()
        }
    }

    public var lastError = SimpleObjectProperty<Throwable?>(null)

    lateinit var main_thread: Thread;

    public val max_seconds = 5000
    private val delay_seconds = 50
    var bot_delay: Long = 200

    private var server = Server();
    private var bot = SimpleBot(field, Point(), Point());

    public var sessionId = "";
    public var serverIp = ""
    public var serverPort = "2020"

    public var playersInLobby = SimpleIntegerProperty(0)
    public var playersReadyLobby = SimpleIntegerProperty(0)

    val listener: InvalidationListener = InvalidationListener {
        if (current_stage.value == GameStage.Die) {
            try {
                still_reading_server.set(false)
            } catch (ex: Throwable) {
                println(ex.message)
            }
            try {
                server.close()
            } catch (ex: Throwable) {
                println(ex.message)
            }
            try {
                field.clear()
            } catch (ex: Throwable) {
                println(ex.message)
            }
            try {
                main_thread.interrupt()
            } catch (ex: Throwable) {
                println(ex.message)
            }
            try {
                timer.cancel()
                timer.purge()
            } catch (ex: Throwable) {
                println(ex.message)
            }
            try {
                Platform.exit()
            } catch (ex: Throwable) {
                println(ex.message)
            }
        }
    }

    var error_nullifire = InvalidationListener {
        lastError.value = null
    }

    init {
        timer.schedule(timerTask, 0, delay_seconds.toLong())
        current_stage.addListener(error_nullifire)
        current_stage.addListener(listener)
    }

    fun connect(ip: String, port: String): Boolean {
        if (current_stage.value == GameStage.ServerConnection) {

            serverIp = ip
            serverPort = port
            if (server.connect(ip, port.toInt())) {
                current_stage.value = GameStage.LobbyConnection

                still_reading_server.set(true)
                if (this::main_thread.isInitialized)
                {
                    main_thread.join()
                }
                main_thread = thread {

                    try {
                        server_comand_reeder()
                    } catch (ex: Throwable) {
                        lastError.value = ex
                    }
                }

                return true;
            } else
                return false
        } else
            return false
    }


    fun update_timer() {
        if (current_stage.value == GameStage.Game) {
            cur_second += delay_seconds
        }
    }


    fun server_comand_reeder() = runBlocking {
        coroutineScope {
            try {
                while (still_reading_server.get()) {
                    try {
                        var mes = server.getMess()
                        launch(Dispatchers.Default) {
                            try {
                                command(mes)
                            } catch (ex: Throwable) {
                                lastError.value = ex
                            }
                        }
                    } catch (ex: NoSuchElementException) {
                        server.close()
                        still_reading_server.set(false)
                        current_stage.value = GameStage.ServerConnection
                        lastError.value = Throwable("Lost connection with server.")
                    } catch (ex: Throwable) {
                        lastError.value = Throwable(ex.message)
                    }
                }
            } catch (ex: Throwable) {
                lastError.value = ex
            }
        }
    }

    suspend fun command(_msg: String) {
        val msg = _msg.split(" ")
        when (msg[0]) {
            //Запрос на создание сессии
            "500" -> {
            }
            //Удачное создание лобби
            "505" -> sucsessLobbyCreation(msg[1])
            //Удачное присоединение к лобби
            "509" -> sucsessLobbyConnection(msg[1], msg[2])
            //Сессия не существует
            "506" -> throw Throwable("Session doesn't exist.")
            //Сессия полная
            "507" -> throw Throwable("Session is full.")
            "511" -> throw Throwable("Game didn't start yet.")
            "508" -> playersReady("0", msg[1])
            "700" -> fix_turn_number(msg[1])
            "510" -> start_game(msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg[9], msg[10])
            "777" -> make_turn(msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg)
            "770" -> update_spectator(msg[1],msg)
            "513" -> start_spectator(msg[1], msg[2], msg)
            "555" -> wining_list(msg)
            else -> {
            }
        }
    }

    private fun start_spectator(width: String, height: String, msg: List<String>) = runBlocking {
        if (sp_mod)
            if (current_stage.value == GameStage.LobbyConnection) {
                cur_second.value = 0

                field = GameField(width.toInt(), height.toInt())
                for (i in 1..(4)) {
                    field.players_position.add(
                        Mouse(
                            Point(0, 0),
                            listOf(MouseValue.RED, MouseValue.BLUE, MouseValue.GREEN, MouseValue.YELLOW)[(i - 1) % 4]
                        )
                    )
                }

                for ( i in 0 until field.width)
                    for ( j in 0 until field.height)
                    {
                        field[i,j].value = when(msg[3+i*field.width + j])
                        {
                            "0" -> CellValue.FLOOR
                            "1" -> CellValue.WALL
                            "2" -> CellValue.EXIT
                            else -> CellValue.FLOOR
                        }
                        field[i,j].shadow = 0
                    }

                cur_player_pos = Point(field.width/2, field.height/2)
                cur_target_point = Point(field.width/2, field.height/2)

                current_stage.value = GameStage.Game

                run {
                    update_spectator(
                        "0",
                        msg.subList((3+field.height*field.width-2),(msg.size))
                    )
                }

            }
    }

    private suspend fun update_spectator(turn: String, msg: List<String>) {
        if (sp_mod)
            if (current_stage.value == GameStage.Game) {
                cur_second.value = 0
                cur_turn.value = turn.toInt()
                for (i in 2 until msg.size step 3) {
                    field.players_position[msg[i].toInt() - 3].p.x = msg[i + 1].toInt()
                    field.players_position[msg[i].toInt() - 3].p.y = msg[i + 2].toInt()
                }
                field.ping()
            }
    }

    public var didPlayrWin = -1
    public var playerWinId = mutableListOf<Int>()
    public var playerWinColor = mutableListOf<Int>()

    private fun wining_list(msg: List<String>) {
        if (current_stage.value == GameStage.Game) {
            field.clear()

            this.playerWinId.clear()
            playerWinColor.clear()
            didPlayrWin = -1
            for (i in 1..(msg.size - 1) step 2) {
                playerWinId.add(msg[i].toInt())
                playerWinColor.add(msg[i + 1].toInt())
                if (msg[i + 1].toInt() == player_id) {
                    didPlayrWin = 1
                }
            }
            if (sp_mod) {
                didPlayrWin = 0
            }
            this.has_moved = true
            this.sp_mod = false
            current_stage.value = GameStage.WinScreen
        }
    }


    private suspend fun fix_turn_number(s: String) {
        if (current_stage.value == GameStage.Game) {
            cur_turn.value = s.toInt()
            if (!sp_mod)
            if (cur_target_point != cur_player_pos) {
                delay(bot_delay)
                if (!has_moved) {
                    move_mouse_to(cur_target_point.x, cur_target_point.y)
                }
            }
        }
    }


    private suspend fun playersReady(s1: String, s2: String) {
        if (current_stage.value == GameStage.Lobby) {
            playersInLobby.value = s2.toInt()
            playersReadyLobby.value = s1.toInt()
        }
    }

    private suspend fun sucsessLobbyConnection(s: String, s1: String) {
        if (current_stage.value == GameStage.LobbyConnection) {
            playersReady(s, s1)
            current_stage.value = GameStage.Lobby
        }
        if (current_stage.value == GameStage.Lobby) {
            playersReady(s, s1)
        }
    }

    private suspend fun sucsessLobbyCreation(s: String) {
        if (current_stage.value == GameStage.LobbyConnection) {
            sessionId = s
            playersInLobby.value = 1
            playersReadyLobby.value = 0
            current_stage.value = GameStage.Lobby
            timer = Timer()
        }
    }

    var cur_turn = SimpleIntegerProperty(0)
    var cur_direction = 0
    var cur_player_pos = Point()

    var cur_target_point = Point(0, 0)

    var has_moved = false;

    fun move_mouse_to(x: Int, y: Int) {
        if (!sp_mod) {
            if (current_stage.value == GameStage.Game) {
                has_moved = true
                cur_target_point = Point(x, y)
                bot.position = cur_player_pos
                bot.target = Point(x, y)

                var move = bot.findWayTo()
                cur_direction = move.toInt()
                field.ping()
                if (move != SimpleBot.Dirrections.NOTHING)
                    server.sendMess(move.toString() + " " + cur_turn.value)

            }
        }else
        {
            cur_target_point = Point(x, y)
        }
    }

    var player_id = -1

    suspend fun start_game(
        color: String,
        width: String,
        height: String,
        x: String,
        y: String,
        left: String,
        right: String,
        down: String,
        up: String,
        center: String
    ) = runBlocking {
        if (!sp_mod)
            if (current_stage.value == GameStage.Lobby) {
                cur_second.value = 0

                field = GameField(width.toInt(), height.toInt())
                for (i in 1..(4)) {
                    field.players_position.add(
                        Mouse(
                            Point(0, 0),
                            listOf(MouseValue.RED, MouseValue.BLUE, MouseValue.GREEN, MouseValue.YELLOW)[(i - 1) % 4]
                        )
                    )
                }

                player_id = color.toInt()
                field.players_position[color.toInt() - 3].p = Point(x.toInt(), y.toInt())
                cur_player_pos = Point(x.toInt(), y.toInt())
                cur_target_point = cur_player_pos


                field[field.width / 2, field.height / 2].value = CellValue.EXIT

                bot = MouseBot(field, cur_player_pos, cur_target_point);


            }
        current_stage.value = GameStage.Game

        launch(Dispatchers.Default) {
            delay(50)
            make_turn(
                "-1",
                x,
                y,
                left,
                right,
                down,
                up,
                center, listOf()
            );
        }

    }


    private suspend fun make_turn(
        turn: String,
        x: String,
        y: String,
        left: String,
        right: String,
        down: String,
        up: String,
        center: String,
        msg: List<String>
    ) {

            if (current_stage.value == GameStage.Game) {
                field[cur_player_pos.x, cur_player_pos.y].shadow = 1
                field[cur_player_pos.x + 1, cur_player_pos.y].shadow = 1
                field[cur_player_pos.x - 1, cur_player_pos.y].shadow = 1
                field[cur_player_pos.x, cur_player_pos.y + 1].shadow = 1
                field[cur_player_pos.x, cur_player_pos.y - 1].shadow = 1

                cur_player_pos.x = x.toInt()
                cur_player_pos.y = y.toInt()

                field[cur_player_pos.x, cur_player_pos.y].value = IntToCell(center.toInt())
                field[cur_player_pos.x - 1, cur_player_pos.y].value = IntToCell(left.toInt())
                field[cur_player_pos.x + 1, cur_player_pos.y].value = IntToCell(right.toInt())
                field[cur_player_pos.x, cur_player_pos.y - 1].value = IntToCell(up.toInt())
                field[cur_player_pos.x, cur_player_pos.y + 1].value = IntToCell(down.toInt())

                field[cur_player_pos.x, cur_player_pos.y].shadow = 0
                field[cur_player_pos.x + 1, cur_player_pos.y].shadow = 0
                field[cur_player_pos.x - 1, cur_player_pos.y].shadow = 0
                field[cur_player_pos.x, cur_player_pos.y + 1].shadow = 0
                field[cur_player_pos.x, cur_player_pos.y - 1].shadow = 0

                has_moved = false

                for (i in 9 until msg.size step 3) {
                    field.players_position[msg[i].toInt() - 3].p.x = msg[i + 1].toInt()
                    field.players_position[msg[i].toInt() - 3].p.y = msg[i + 2].toInt()
                }
                field.ping()

                cur_turn.value = turn.toInt() + 1

                cur_second.value = 0;

                if (cur_target_point != cur_player_pos) {
                    delay(bot_delay)
                    if (!has_moved) {
                        move_mouse_to(cur_target_point.x, cur_target_point.y)
                    }
                }
            }
    }

    fun createLobby() {
        if (current_stage.value == GameStage.LobbyConnection) {
            server.sendMess("110")
        }
    }

    fun connectLobby(id: String) {
        if (current_stage.value == GameStage.LobbyConnection) {
            sessionId = id
            if (sp_mod) {
                server.sendMess("305 ${id}")
            } else {
                server.sendMess("105 ${id}")
            }
        }
    }


    fun playerReady() {
        if (current_stage.value == GameStage.Lobby)
            server.sendMess("112")
    }

    fun playerBack() {
        if (current_stage.value == GameStage.Lobby) {
            server.sendMess("106")
            current_stage.value = GameStage.LobbyConnection
        }
    }

    fun backToLobbyConnect() {
        if (current_stage.value == GameStage.WinScreen) {
            this.sessionId = ""
            sp_mod = false
            server.sendMess("106")
            current_stage.value = GameStage.LobbyConnection
        }
    }

}