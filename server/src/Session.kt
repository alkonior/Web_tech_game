

class Session(_state: State, _id: Int?, _player: Player?) {
    var map: Array<Array<Int>>? = null
    var player_cnt: Int = 0
    lateinit var players: Array<Player>
    var id: Int? = null
    var turn: Int? = null
    var state: State

    enum class State{
        IDLING,
        LOBBY,
        INGAME,
        FINISHED
    }

    init{
        state = _state
        if (state != State.IDLING) {
            players[player_cnt] = _player
            player_cnt++
            id = _id
        }
    }
}