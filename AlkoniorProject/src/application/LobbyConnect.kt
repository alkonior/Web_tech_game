package application

import gameengine.GameEngine
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import models.GameFieldModel
import tornadofx.*
import java.util.*

class LobbyConnect : View("Mice in lab.") {
    override val root: AnchorPane by fxml("/views/lobbyconnect.fxml")

    val lobyId: TextField by fxid()
    val errorMes: Label by fxid()

    private val gameEngineModel: GameFieldModel by inject()
    private val gameEngine = gameEngineModel.engine


    init {
        currentStage?.setResizable(false)
        currentStage?.width = 640.0
        currentStage?.height = 640.0
        gameEngine.current_stage.addListener(InvalidationListener {
            Platform.runLater {
                if (gameEngine.current_stage.value == GameEngine.GameStage.Lobby) {
                    val model = GameFieldModel(gameEngine);
                    val fragmentScope = Scope()
                    setInScope(model, fragmentScope)
                    val gameview = find<Lobby>(fragmentScope)
                    replaceWith(gameview)
                }
                if (gameEngine.current_stage.value == GameEngine.GameStage.Game) {
                    val model = GameFieldModel(gameEngine);
                    val fragmentScope = Scope()
                    setInScope(model, fragmentScope)
                    val gameview = find<Game>(fragmentScope)
                    replaceWith(gameview)
                }
            }
        })

        gameEngine.lastError.addListener(InvalidationListener {
            Platform.runLater { errorMes.text = gameEngine.lastError.value?.message ?: "" }
        })
    }


    fun onCreateButton() {
        try {
            gameEngine.createLobby()
        } catch (ex: Throwable) {
            println(ex.message)
            errorMes.text = "Server error"
        }
    }

    fun onConnectButton() {
        try {
            gameEngine.connectLobby(lobyId.text)
        } catch (ex: Throwable) {
            println(ex.message)
            errorMes.text = "ex.message"
        }
    }


}
