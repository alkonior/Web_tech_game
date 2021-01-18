package dk.im2b

import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread
import Player
import Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

var server: Server = Server()

fun main(args: Array<String>) {
    server.run()
}

class Server{
    private lateinit var waitList: Session
    var lobbyCount: Int = 0
    var sessions: MutableMap<Int, Session> = mutableMapOf()
    private var playerCount: Int = 0
    private val server = ServerSocket(2020)

    fun run(){
        println("Server is running on port ${server.localPort}")
        // Создаем лобби в котором находятся все игроки до подключения в игровые комнаты
        waitList = Session(Session.Status.IDLING)
        println("Waitlist is ready! There are ${waitList.playerCount} live connections now!")
        println("----------------------------------------------------------------")
        while (true) {
            val client = server.accept()
            println("Client connected: ${client.inetAddress.hostAddress}. His ID is ${playerCount}")
            // Отпускаем клиента в отдельный поток обработки
            thread { ClientHandler(client, playerCount++, waitList).run() }
        }
    }

    fun createSession(_player: Player){
        sessions[lobbyCount] = Session(Session.Status.LOBBY, lobbyCount++, _player)
    }

    fun deleteSession(_id: Int){
        sessions.remove(_id)
    }
}

class ClientHandler(_client: Socket, _playerId: Int, _waitList: Session) {
    private val reader: Scanner
    private val writer: OutputStream
    private var running: Boolean = false
    private val player: Player

    init{
        reader = Scanner(_client.getInputStream())
        writer = _client.getOutputStream()
        player = Player(_playerId, _client, _waitList)
        player.session.addPlayer(player)
        println("Player ${player.id} was moved to the main lobby!")
    }

    fun run() = runBlocking {
        running = true
        player.write("500")
        while (running) {
            try {
                val text = reader.nextLine()
                launch((Dispatchers.Default)){
                command(text)
                }
            } catch (ex: Exception) {
                // TODO: Implement exception handling
                shutdown()
            } finally {

            }

        }
    }

    suspend fun command(_msg: String) {
        val msg = _msg.split(" ")
        when (msg[0]){
            //Запрос на создание сессии
            "110" -> {
                if (player.status != Player.Status.IDLING)
                    player.write("312")
                server.createSession(player)
                player.write("505 ${player.session.id}")
            }
            //Запрос на подключение к сессии
            "105" -> toLobby(msg[1])
/*            "112" ->{
                ready()
            }*/
            //Общая ошибка, распознать нельзя
            else -> player.write("312")
        }
    }

    fun toLobby(msg: String) {
        if (player.status != Player.Status.IDLING)
            player.write("312")
        if (server.sessions.containsKey(msg.toInt())){
            val code: String = server.sessions[msg.toInt()]!!.addPlayer(player)
            val responce = "${code} ${player.session.ready} ${player.session.playerCount}"
            announce(true, responce)
        }
        else{
            //Лобби не найдено
            player.write("506")
        }
    }

/*    private fun ready(){
        player.status

    }*/

    private fun announce(_everyone: Boolean, _announcement: String){
        if (!_everyone){
            for (x in player.session.players.values){
                if(player != x) {
                    x.write(_announcement)
                }
            }
        }else{
            for (x in player.session.players.values){
                x.write(_announcement)
            }
        }
    }

    private fun shutdown() {
        running = false
        player.socket.close()
        println("${player.socket.inetAddress.hostAddress} with id ${player.id} closed the connection")
        player.session.removePlayer(player)
        if (player.session.playerCount == 0 && player.session.status != Session.Status.IDLING) {
            server.deleteSession(player.session.id)
        }
    }

}

