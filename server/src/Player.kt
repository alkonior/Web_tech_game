import dk.im2b.server
import java.net.Socket


class Player(_id: Int, _socket: Socket, _session: Session) {
    var session: Session
    var map = null
    var pos = null
    var id: Int
        private set
    var socket: Socket
        private set

    var status: Status

    enum class Status{
        IDLING,
        LOBBY,
        READY,
        INGAME,
        FINISHED
    }

    init{
        id = _id
        socket = _socket
        status = Status.IDLING
        session = _session
    }

    fun command(_msg: String): String {
        val msg = _msg.split(" ")
        when (msg[0]){
            //Запрос на создание сессии
            "110" -> {
                if (status != Status.IDLING)
                    return "312"
                server.createSession(this)
                return "505 ${session.id}"
            }
            //Запрос на подключение к сессии
            "105" -> {
                if (status != Status.IDLING)
                    return "312"
                if (server.sessions.containsKey(msg[1].toInt())){
                    var responce = server.sessions[msg[1].toInt()]!!.addPlayer(this)
                    return responce
                }
                else{
                    //Лобби не найдено
                    return "506"
                }
            }
            //Общая ошибка, распознать нельзя
            else -> return "312"
        }
    }
}