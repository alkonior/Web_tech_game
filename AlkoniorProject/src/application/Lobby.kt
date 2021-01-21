package application

import gameengine.GameEngine
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import models.GameFieldModel
import tornadofx.*

class Lobby : View("Mice in lab.") {
    override val root: AnchorPane by fxml("/views/lobby.fxml")

    val lobyId: Label by fxid()
    val playersCount: Label by fxid()

    private val gameEngineModel: GameFieldModel by inject()
    private val gameEngine = gameEngineModel.engine

    init {
        currentStage?.setResizable(false)
        currentStage?.width = 640.0
        currentStage?.height = 640.0
        lobyId.text = gameEngine.sessionId
        lobyId.text = "0/1"
        gameEngine.playersReadyLobby.addListener(InvalidationListener {
            Platform.runLater {
                playersCount.text = "${gameEngine.playersReadyLobby.value} / ${gameEngine.playersInLobby.value}"
            }
        })
        gameEngine.playersInLobby.addListener(InvalidationListener {
            Platform.runLater {
                playersCount.text = "${gameEngine.playersReadyLobby.value} / ${gameEngine.playersInLobby.value}"
            }
        })
        gameEngine.current_stage.addListener(InvalidationListener {
            Platform.runLater {
                if (gameEngine.current_stage.value == GameEngine.GameStage.Lobby) {
                    val model = GameFieldModel(gameEngine);
                    val fragmentScope = Scope()
                    setInScope(model, fragmentScope)
                    val gameview = find<LobbyConnect>(fragmentScope)
                    replaceWith(gameview)
                }
            }
        })
    }


    fun onReady()
    {
        gameEngine.playerReady()
    }

    fun onBack()
    {
        gameEngine.playerBack()
    }
}
