package gameengine

import bot.MouseBot
import field.GameField
import javafx.beans.property.SimpleIntegerProperty
import server.Server
import tornadofx.plusAssign
import java.util.*
import kotlinx.coroutines.*
import kotlin.concurrent.thread

class GameEngine {
    private enum class GameStage {
        ServerConnection,
        LobbyConnection,
        Game
    }

    private var current_stage: GameStage = GameStage.ServerConnection;

    public var field = GameField();
    public var cur_second = SimpleIntegerProperty(0);
    private val timer = Timer("game timer", false)
    private val timerTask = object : TimerTask() {
        override fun run() {
            update_timer()
        }
    }
    lateinit var main_thread: Thread;

    public val max_seconds = 3000
    private val delay_seconds = 100

    private var server = Server();
    private lateinit var bot: MouseBot;

    public var sessionId = "AAAAAA";

    fun connect(ip: String, port: String) {
        if (current_stage == GameStage.ServerConnection) {
            /*
            if (server.connect(ip, port.toInt())) {
                current_stage = GameStage.LobbyConnection
            }
            */
            current_stage = GameStage.Game
            if (this::main_thread.isInitialized) {
                main_thread.join()
            }
            main_thread = thread {
                server_comand_reeder()
            }


        }
    }

    suspend fun create_new_lobby() {
        if (current_stage == GameStage.LobbyConnection) {
            TODO("Server.send(\"что-то то там\")")
        }
    }

    suspend fun connect_this_lobby(string: String) {
        if (current_stage == GameStage.LobbyConnection) {
            TODO("Server.send(\"что-то то там\")")
        }
    }


    fun update_timer() {
        if (current_stage == GameStage.Game) {
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
            while (current_stage != GameStage.ServerConnection) {
                println(server.getMess())

            }
        }
    }

    suspend fun start_game() {
        timer.schedule(timerTask, 0, delay_seconds.toLong())
        while (current_stage == GameStage.Game) {
            delay(200)
            println("${cur_second}")
            if (cur_second.value >= max_seconds)
            {
                cur_second.value = 0
            }
        }

    }

}