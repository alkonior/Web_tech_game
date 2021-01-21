package gameengine

import bot.MouseBot
import field.GameField
import javafx.beans.InvalidationListener
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import server.Server
import tornadofx.plusAssign
import java.util.*
import kotlinx.coroutines.*
import kotlin.concurrent.thread

class GameEngine:EventListener {
    public enum class GameStage{
        ServerConnection,
        LobbyConnection,
        Lobby,
        Game
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

    public var lastError  = SimpleObjectProperty<Throwable?>(null)

    lateinit var main_thread: Thread;

    public val max_seconds = 5000
    private val delay_seconds = 100

    private var server = Server();
    private lateinit var bot: MouseBot;

    public var sessionId = "AAAAAA";

    public  var playersInLobby = SimpleIntegerProperty(0)
    public  var playersReadyLobby = SimpleIntegerProperty(0)


    init {
        current_stage.addListener(InvalidationListener { lastError.value = null })
    }

    fun connect(ip: String, port: String): Boolean {
        if (current_stage.value == GameStage.ServerConnection) {

            if (server.connect(ip, port.toInt())) {
                current_stage.value = GameStage.LobbyConnection
                if (this::main_thread.isInitialized) {
                    main_thread.join()
                }
                main_thread = thread {
                    server_comand_reeder()
                }

                return true;
            }else
                return false
        }else
            return false
    }

    suspend fun create_new_lobby() {
        if (current_stage.value == GameStage.LobbyConnection) {
            TODO("Server.send(\"что-то то там\")")
        }
    }

    suspend fun connect_this_lobby(string: String) {
        if (current_stage.value == GameStage.LobbyConnection) {
            TODO("Server.send(\"что-то то там\")")
        }
    }


    fun update_timer() {
        if (current_stage.value == GameStage.Game) {
            cur_second += delay_seconds
        }
    }

    fun move_mouse_to(x: Int, y: Int) {

    }


    fun server_comand_reeder() = runBlocking {
        coroutineScope {
            launch(Dispatchers.Default) {
                start_game()
            }
            while (current_stage.value != GameStage.ServerConnection) {
                var mes = server.getMess()
                launch(Dispatchers.Default) {
                    try {

                        command(mes)
                    }
                    catch (ex: Throwable)
                    {
                        lastError.value = ex
                    }
                }
                delay(50)
            }
        }
    }

    suspend fun command(_msg: String) {
    val msg = _msg.split(" ")
    when (msg[0]) {
        //Запрос на создание сессии
        "500" -> {}
        //Удачное создание лобби
        "505" -> sucsessLobbyCreation(msg[1])
        //Удачное присоединение к лобби
        "509" -> sucsessLobbyConnection(msg[1],msg[2])
        //Сессия не существует
        "506" -> throw Throwable("Session doesn't exist.")
        //Сессия полная
        "507" -> throw Throwable("Session is full.")
        "508" -> playersReady("0",msg[1])

        else -> {}
    }
}

    private fun playersReady(s1: String, s2: String) {
        playersInLobby.value = s1.toInt() + s2.toInt()
        playersReadyLobby.value = s1.toInt()
    }

    private suspend fun sucsessLobbyConnection(s: String, s1: String) {
        if (current_stage.value==GameStage.LobbyConnection){
            sessionId = s
            current_stage.value = GameStage.Lobby
            delay(20)
            playersReady(s,s1)
        }
        if (current_stage.value==GameStage.Lobby)
        {
            playersReady(s,s1)
        }
    }

    private suspend fun sucsessLobbyCreation(s: String) {
        if (current_stage.value==GameStage.LobbyConnection){
            sessionId = s
            current_stage.value = GameStage.Lobby

        }

    }


    suspend fun start_game() {
        timer.schedule(timerTask, 0, delay_seconds.toLong())
    }

    fun createLobby() {
        if (current_stage.value == GameStage.LobbyConnection)
        {
            server.sendMess("110")
        }
    }

    fun connectLobby(id :String) {
        if (current_stage.value == GameStage.LobbyConnection) {
            server.sendMess("105 ${id}")
        }
    }


    fun playerReady()
    {
        if (current_stage.value == GameStage.Lobby)
            server.sendMess("112")
    }

    fun playerBack()
    {
        if (current_stage.value == GameStage.Lobby) {
            server.sendMess("106")
            current_stage.value = GameStage.LobbyConnection
        }
    }

}