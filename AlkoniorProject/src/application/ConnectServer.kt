package application

import field.GameField
import gameengine.GameEngine
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import javafx.scene.text.TextFlow
import models.GameFieldModel
import tornadofx.*

class ConnectServer : View("Mice in lab.") {
    override val root: AnchorPane by fxml("/views/connect.fxml")

    val Connect:Button by fxid()

    val Id: TextField by fxid()
    val Port: TextField by fxid()
    val ErrorMessage: Label by fxid()

    init {
        currentStage?.setResizable(false)
        currentStage?.width  = 640.0
        currentStage?.height  = 640.0
    }

    fun connect(){
        println("Connect")



        var game = GameEngine();
        try {
            if (
            game.connect(Id.text,Port.text)){


            val model = GameFieldModel(game);
            val fragmentScope = Scope()
            setInScope(model, fragmentScope)
            val gameview = find<LobbyConnect>(fragmentScope)

            replaceWith(gameview)
            }

        }catch (exe:Throwable){
            ErrorMessage.text = exe.message
        }


    }
}
