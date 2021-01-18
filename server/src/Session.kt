class Session(_status: Status) {
    var map: Array<Array<Int>>? = null
    var playerCount: Int = 0
    var players: MutableMap<Int, Player> = mutableMapOf()
    var id: Int = 0
    var turn: Int = 0
    var status: Status
    var ready: Int = 0

    enum class Status{
        IDLING,
        LOBBY,
        INGAME,
        FINISHED
    }

    init{
        status = _status
    }

    constructor(_status: Status, _id: Int, _player: Player) : this(_status){
        id = _id
        _player.session = this
        _player.status = Player.Status.valueOf(status.name)
        playerCount++
        players[_player.id] = _player
    }

    fun addPlayer(_player: Player): String {
        //Проверка на заполненность сессии, если она игровая
        if(status != Status.IDLING && playerCount == 4)
            return "507"
        _player.session = this
        _player.status = Player.Status.valueOf(status.name)
        playerCount++
        players.put(_player.id, _player)
        return "509"
    }

    fun removePlayer(_player: Player){
        playerCount--
        players.remove(_player.id)
    }
}