package dk.im2b

import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread
import Player
import Session

var server: Server = Server()

fun main(args: Array<String>) {
    server.run()
}

class Server{
    private lateinit var waitList: Session
    var lobbyCount: Int = 0
    var sessions: MutableMap<Int, Session> = mutableMapOf(0 to Session(Session.Status.IDLING))
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

    fun run() {
        running = true
        write("500")
        while (running) {
            try {
                val text = reader.nextLine()
                val responce = player.command(text)
                write(responce)
            } catch (ex: Exception) {
                // TODO: Implement exception handling
                shutdown()
            } finally {

            }

        }
    }

    private fun write(message: String) {
        writer.write((message + '\n').toByteArray(Charset.defaultCharset()))
    }

    private fun shutdown() {
        running = false
        player.socket.close()
        println("${player.socket.inetAddress.hostAddress} closed the connection")
        player.session.removePlayer(player)
        if (player.session.playerCount == 0 && player.session.status != Session.Status.IDLING)
            server.deleteSession(player.session.id)
    }

}

