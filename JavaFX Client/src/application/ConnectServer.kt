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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class ConnectServer : View("Mice in lab.") {
    override val root: AnchorPane by fxml("/views/connect.fxml")



    private val Id: TextField by fxid()
    private val Port: TextField by fxid()
    private val ErrorMessage: Label by fxid()

    private val field_: GameFieldModel by inject()
    lateinit var gameEngine : GameEngine
    private lateinit var connect_thread:Thread


    init {
        try {
            currentStage?.setResizable(false)
            currentStage?.width = 640.0
            currentStage?.height = 640.0
            gameEngine = field_.engine
            Id.text = gameEngine.serverIp.toString()
            Port.text = gameEngine.serverPort.toString()
            ErrorMessage.text = gameEngine.lastError.value?.message
        }catch (ex:Throwable)
        {
            gameEngine =  GameEngine()
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
                        else -> {}
                    }

                }
            })

            currentWindow?.onCloseRequest = EventHandler {
                gameEngine.current_stage.value = GameEngine.GameStage.Die
                exitProcess(0)
            }
        }
    }

    var can_run_connect= AtomicBoolean(true)

    fun connect() {
        println("Connect")
        if (can_run_connect.get()) {
            if (this::connect_thread.isInitialized) {
                connect_thread.join()
            }
            connect_thread = thread {
                try {
                    can_run_connect.set(false)
                    gameEngine.connect(Id.text, Port.text)

                } catch (exe: Throwable) {
                    Platform.runLater {
                        ErrorMessage.text = exe.message
                    }
                } finally {
                    can_run_connect.set(true)
                }
            }
        }

    }


}
