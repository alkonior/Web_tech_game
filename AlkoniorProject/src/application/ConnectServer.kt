package application

import gameengine.GameEngine
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.WeakInvalidationListener
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import models.GameFieldModel
import tornadofx.*
import java.lang.Thread.sleep
import kotlin.system.exitProcess

class ConnectServer : View("Mice in lab.") {
    override val root: AnchorPane by fxml("/views/connect.fxml")



    private val Id: TextField by fxid()
    private val Port: TextField by fxid()
    private val ErrorMessage: Label by fxid()

    init {
        currentStage?.setResizable(false)
        currentStage?.width = 640.0
        currentStage?.height = 640.0
    }

    fun connect() {
        println("Connect")


        val gameEngine = GameEngine();
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
                                null, sizeToScene = true, centerOnScreen = false
                            )
                            GameEngine.GameStage.LobbyConnection -> {
                                currentWindow?.scene?.root?.replaceWith(
                                    find<LobbyConnect>(fragmentScope).root,
                                    null, sizeToScene = true, centerOnScreen = false
                                )
                            }
                            GameEngine.GameStage.Game -> currentWindow?.scene?.root?.replaceWith(
                                find<Game>(fragmentScope).root,
                                null, sizeToScene = false, centerOnScreen = false
                            )
                            GameEngine.GameStage.ServerConnection -> currentWindow?.scene?.root?.replaceWith(
                                find<ConnectServer>(fragmentScope).root,
                                null, sizeToScene = true, centerOnScreen = false
                            )
                            GameEngine.GameStage.WinScreen -> currentWindow?.scene?.root?.replaceWith(
                                find<WinnerScreen>(fragmentScope).root,
                                null, sizeToScene = true, centerOnScreen = false
                            )
                            GameEngine.GameStage.Die -> {
                            }
                        }

                    }
                })

                currentWindow?.onCloseRequest = EventHandler {
                    gameEngine.current_stage.value = GameEngine.GameStage.Die
                    exitProcess(0)
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
