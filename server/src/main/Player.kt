package main

import java.net.Socket
import java.nio.charset.Charset


class Player(_id: Int, _socket: Socket, _session: Session) {
    var session: Session
    var pos = arrayOf(1, 1)
    var transPos = arrayOf(1, 1)
    var color = 0
    var ready: Boolean = false
    var id: Int
        private set
    var socket: Socket
        private set

    var status: Status

    enum class Status {
        IDLING,
        LOBBY,
        READY,
        INGAME,
        SPECTATING,
        FINISHED
    }

    init {
        id = _id
        socket = _socket
        status = Status.IDLING
        session = _session
    }

    //Функция передачи сообщения клиенту
    fun write(_msg: String) {
        socket.getOutputStream().write((_msg + '\n').toByteArray(Charset.defaultCharset()))
    }
}