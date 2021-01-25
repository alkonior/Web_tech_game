package application

import gameengine.GameEngine
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import models.GameFieldModel
import tornadofx.*
import java.util.*

class LobbyConnect : View("Mice in lab.") {
    override val root: AnchorPane by fxml("/views/lobbyconnect.fxml")

    val lobyId: TextField by fxid()
    val errorMes: Label by fxid()
    val createButton: Button by fxid()
    val spTriger: CheckBox by fxid()

    private val gameEngineModel: GameFieldModel by inject()
    private val gameEngine = gameEngineModel.engine


    init {
        currentStage?.setResizable(false)
        currentStage?.width = 640.0
        currentStage?.height = 640.0
        lobyId.text = gameEngine.sessionId
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

    fun onSpMod()
    {
        createButton.isDisable = spTriger.isSelected
        gameEngine.sp_mod = spTriger.isSelected
    }


}
