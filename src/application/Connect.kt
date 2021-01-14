package application

import field.GameField
import javafx.scene.control.Button
import javafx.scene.layout.AnchorPane
import tornadofx.*

class Connect : View("Mice in lab.") {
    override val root: AnchorPane by fxml("/connect.fxml")

    val Connect:Button by fxid()

    init {
        currentStage?.setResizable(true)
    }

    fun connect(){
        println("Connect")

        val field = GameField(10,10);





        val model = GameFieldModel(field);
        val fragmentScope = Scope()
        setInScope(model, fragmentScope)
        val gameview = find<Game>(fragmentScope)
        replaceWith(gameview)
    }
}
