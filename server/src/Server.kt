package dk.im2b

import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread
import Player
import Session


fun main(args: Array<String>) {
    var server: Server = Server()
    server.run()
}

class Server{
    private lateinit var waitList: Session
    private var lateinit sessions: Array<Session>
    private var playerCount: Int = 0
    private val server = ServerSocket(2020)

    fun run(){
        println("Server is running on port ${server.localPort}")
        waitList = Session(Session.State.IDLING, null, null)
        while (true) {
            val client = server.accept()
            println("Client connected: ${client.inetAddress.hostAddress}")
            // Отпускаем клиента в отдельный поток обработки
            thread { ClientHandler(client, ++playerCount).run() }
        }
    }
}

class ClientHandler(client: Socket, player_id: Int) {
    private val client: Socket = client
    private val reader: Scanner = Scanner(client.getInputStream())
    private val writer: OutputStream = client.getOutputStream()
    private val calculator: Calculator = Calculator()
    private var running: Boolean = false

    val player = Player(player_id , client)

    fun run() {
        running = true
        // Отсылаем ответ, что сервер жив и готов к работе
        write("100")
        while (running) {
            try {
                val text = reader.nextLine()
                if (text == "EXIT"){
                    shutdown()
                    continue
                }

                val values = text.split(' ')
                val result = calculator.calculate(values[0].toInt(), values[1].toInt(), values[2])
                write(result)
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
        client.close()
        println("${client.inetAddress.hostAddress} closed the connection")
    }

}

class Calculator {

    fun calculate(a: Int, b: Int, operation: String): String {
        when (operation) {
            "add" -> return calc(a, b, ::add).toString()
            "sub" -> return calc(a, b, ::sub).toString()
            "div" -> return calc(a.toDouble(), b.toDouble(), ::div).toString()
            "multi" -> return calc(a, b, ::multi).toString()
            else -> {
                return "Something whent wrong"
            }
        }
    }

    // A Calculator (functional programming)
    private fun <T> calc(a: T, b: T, operation: (T, T) -> T): T {
        return operation(a, b)
    }

    private fun add(a: Int, b: Int): Int = a + b
    private fun sub(a: Int, b: Int): Int = a - b
    private fun div(a: Double, b: Double): Double = a / b
    private fun multi(a: Int, b: Int): Int = a * b


}