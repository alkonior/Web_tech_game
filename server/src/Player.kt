import java.net.Socket


class Player(_id: Int, _socket: Socket) {
    var session: Int
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
        INGAME,
        FINISHED
    }

    init{
        id = _id
        socket = _socket
        status = Status.IDLING
        session = 0
    }




}