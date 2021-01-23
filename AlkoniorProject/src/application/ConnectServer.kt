package application

import gameengine.GameEngine
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import models.GameFieldModel
import server.Server
import tornadofx.*

class ConnectServer : View("Mice in lab.") {
    override val root: AnchorPane by fxml("/views/connect.fxml")

    val Connect: Button by fxid()

    val Id: TextField by fxid()
    val Port: TextField by fxid()
    val ErrorMessage: Label by fxid()

    init {
        currentStage?.setResizable(false)
        currentStage?.width = 640.0
        currentStage?.height = 640.0
    }

    fun connect() {
        println("Connect")


        var gameEngine = GameEngine();
        try {
            if (gameEngine.connect(Id.text, Port.text)) {

                gameEngine.current_stage.addListener(InvalidationListener {
                    Platform.runLater {
                        val model = GameFieldModel(gameEngine);
                        val fragmentScope = Scope()
                        setInScope(model, fragmentScope)
                        when (gameEngine.current_stage.value) {
                            GameEngine.GameStage.Lobby -> currentWindow?.scene?.root?.replaceWith(
                                find<Lobby>(fragmentScope).root,
                                null, true, false
                            )
                            GameEngine.GameStage.LobbyConnection -> {
                                currentWindow?.scene?.root?.replaceWith(
                                    find<LobbyConnect>(fragmentScope).root,
                                    null, true, false
                                )
                            }
                            GameEngine.GameStage.Game -> currentWindow?.scene?.root?.replaceWith(
                                find<Game>(fragmentScope).root,
                                null, true, false
                            )
                            GameEngine.GameStage.ServerConnection -> currentWindow?.scene?.root?.replaceWith(
                                find<ConnectServer>(fragmentScope).root,
                                null, true, false
                            )
                            GameEngine.GameStage.Die -> {
                            }
                        }

                    }
                })

                currentWindow?.onCloseRequest = EventHandler {
                    gameEngine.current_stage.value = GameEngine.GameStage.Die
                }

                val model = GameFieldModel(gameEngine);
                val fragmentScope = Scope()
                setInScope(model, fragmentScope)
                val gameview = find<LobbyConnect>(fragmentScope)

                replaceWith(gameview)
            }

        } catch (exe: Throwable) {
            ErrorMessage.text = exe.message
        }
    }


}
