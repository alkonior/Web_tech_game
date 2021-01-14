package gameengine

import field.GameField
import server.Server

class GameEngine {
    private enum class GameStage{
        ServerConnection,
        LobbyConnection,
        Game
    }

    private var current_stage:GameStage = GameStage.ServerConnection;

    public  var field  = GameField();
    private var server = Server();

    fun connect(ip: String, port:String)
    {
        if (current_stage  == GameStage.ServerConnection) {
            if (server.connect(ip, port.toInt())) {
                current_stage = GameStage.LobbyConnection
            }
        }
    }

    fun create_new_lobby()
    {
        if (current_stage == GameStage.LobbyConnection)
        {
            TODO("Server.send(\"что-то то там\")")
        }
    }

    fun connect_this_lobby(string: String)
    {
        if (current_stage == GameStage.LobbyConnection)
        {
            TODO("Server.send(\"что-то то там\")")
        }
    }


}