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
        playersCount.text = "${gameEngine.playersReadyLobby.value} / ${gameEngine.playersInLobby.value}"
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
