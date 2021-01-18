package application

import javafx.scene.layout.AnchorPane
import tornadofx.*

class LobbyConnect : View("Mice in lab.") {
    override val root: AnchorPane by fxml("/views/lobbylist.fxml")

    init {
        currentStage?.setResizable(false)
    }

}
