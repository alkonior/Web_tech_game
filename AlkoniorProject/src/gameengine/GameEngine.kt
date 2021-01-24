package gameengine

import bot.MouseBot
import bot.SimpleBot
import field.*
import javafx.beans.InvalidationListener
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import server.Server
import tornadofx.plusAssign
import java.util.*
import kotlinx.coroutines.*
import java.awt.Point
import kotlin.concurrent.thread

class GameEngine : EventListener {
    public enum class GameStage {
        ServerConnection,
        LobbyConnection,
        Lobby,
        Game,
        Die
    }

    public var current_stage = SimpleObjectProperty<GameStage>(GameStage.ServerConnection);

    public var field = GameField();
    public var cur_second = SimpleIntegerProperty(0);
    private val timer = Timer("game timer", false)
    private val timerTask = object : TimerTask() {
        override fun run() {
            update_timer()
        }
    }

    public var lastError = SimpleObjectProperty<Throwable?>(null)

    lateinit var main_thread: Thread;

    public val max_seconds = 5000
    private val delay_seconds = 50

    private var server = Server();
    private  var bot = SimpleBot(field, Point(), Point());

    public var sessionId = "";

    public var playersInLobby = SimpleIntegerProperty(0)
    public var playersReadyLobby = SimpleIntegerProperty(0)


    init {
        current_stage.addListener(InvalidationListener {
            lastError.value = null
        })
        current_stage.addListener(InvalidationListener {
            if (current_stage.value == GameStage.Die) {
                try {
                    if (this::main_thread.isInitialized) {
                        main_thread.interrupt()
                        timer.cancel()
                    }
                    server.close()
                } catch (ex: Throwable) {
                }
            }
        })
    }

    fun connect(ip: String, port: String): Boolean {
        if (current_stage.value == GameStage.ServerConnection) {

            if (server.connect(ip, port.toInt())) {
                current_stage.value = GameStage.LobbyConnection
                if (this::main_thread.isInitialized) {
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
                while (current_stage.value != GameStage.ServerConnection) {
                    try {
                        var mes = server.getMess()
                        launch(Dispatchers.Default) {
                            try {
                                command(mes)
                            } catch (ex: Throwable) {
                                lastError.value = ex
                            }
                        }
                        delay(50)
                    } catch (ex: Throwable) {
                        lastError.value = ex
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
            "508" -> playersReady("0", msg[1])
            "700" -> fix_turn_number(msg[1])
            "510" -> start_game(msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7], msg[8], msg[9], msg[10])
            "777" -> make_turn(msg[1], msg[2], msg[3], msg[4], msg[5], msg[6],msg[7],msg[8],msg)
            else -> {
            }
        }
    }

    var bot_delay:Long = 100

    private suspend fun fix_turn_number(s: String)
    {
        cur_turn.value = s.toInt()
        if (cur_target_point != cur_player_pos)
        {
            delay(bot_delay)
            if (!has_moved)
            {
                move_mouse_to(cur_target_point.x,cur_target_point.y)
            }
        }
    }


    private suspend fun playersReady(s1: String, s2: String) {
        playersInLobby.value = s2.toInt()
        playersReadyLobby.value = s1.toInt()
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
        }
    }

    var cur_turn = SimpleIntegerProperty(0)
    var cur_direction = 0
    var cur_player_pos = Point()

    var cur_target_point = Point(0,0)

    var has_moved = false;

    fun move_mouse_to(x: Int, y: Int) {
        has_moved = true
        cur_target_point =  Point(x,y)
        bot.target =  Point(x,y)
        bot.position = cur_player_pos

        var move = bot.findWayTo()
        cur_direction = move.toInt()
        field.ping()
        if (move!=SimpleBot.Dirrections.NOTHING)
            server.sendMess(move.toString()+" "+cur_turn.value)

    }

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
    ) {
        timer.schedule(timerTask, 0, delay_seconds.toLong())

        field = GameField(width.toInt(), height.toInt())
        for (i in 1..(playersInLobby.value)) {
            field.players_position.add(
                Mouse(
                    Point(0, 0),
                    listOf(MouseValue.RED, MouseValue.YELLOW, MouseValue.BLUE, MouseValue.GREEN)[(i - 1) % 4]
                )
            )
        }

        field.players_position[color.toInt() - 3].p = Point(x.toInt(), y.toInt())
        cur_player_pos = Point(x.toInt(), y.toInt())
        cur_target_point = cur_player_pos
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

        field[15,15].value = CellValue.EXIT

        bot = MouseBot(field, cur_player_pos, cur_target_point);


        current_stage.value = GameStage.Game
    }


    private suspend fun make_turn (
        turn : String,
        x: String,
        y: String,
        left: String,
        right: String,
        down: String,
        up: String,
        center: String,
        msg: List<String>
    ) {

        field[cur_player_pos.x, cur_player_pos.y].shadow = 1
        field[cur_player_pos.x + 1, cur_player_pos.y].shadow = 1
        field[cur_player_pos.x - 1, cur_player_pos.y].shadow = 1
        field[cur_player_pos.x, cur_player_pos.y + 1].shadow = 1
        field[cur_player_pos.x, cur_player_pos.y - 1].shadow = 1

        cur_player_pos.x = x.toInt()
        cur_player_pos.y= y.toInt()

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

        var k = 0
        for (i in 9 until msg.size step 2) {
            field.players_position[k].p.x = msg[i].toInt()
            field.players_position[k].p.y = msg[i + 1].toInt()
            k++
        }
        field.ping()

        cur_turn.value = turn.toInt()+1

        cur_second.value = 0;

        if (cur_target_point != cur_player_pos)
        {
            delay(bot_delay)
            if (!has_moved)
            {
                move_mouse_to(cur_target_point.x,cur_target_point.y)
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
            server.sendMess("105 ${id}")
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

}