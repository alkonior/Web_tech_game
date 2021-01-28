package application

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import models.GameFieldModel
import tornadofx.*

class WinnerScreen : View("My View") {
    override val root: AnchorPane by fxml("/views/winnerscren.fxml")

    val winnerMessage: Label by fxid()
    val winnerList: VBox by fxid()

    private val gameEngineModel: GameFieldModel by inject()
    private val gameEngine = gameEngineModel.engine

    private val playersimgs = listOf(
        Image("/img/first.png"),
        Image("/img/second.png"),
        Image("/img/third.png"),
        Image("/img/forth.png")
    )

    init {
        when (gameEngine.didPlayrWin) {
            -1 -> winnerMessage.text = "Вам не повезло.\nКто-то прибежал к выходу."
            1 -> winnerMessage.text = "Поздравляем с победой!!!"
            0 -> winnerMessage.text = "Игра закончилась."
        }
        for (i in 0 until gameEngine.playerWinId.size) {

            run {
                val label = Label()
                label.alignment = Pos.CENTER
                label.font = Font.font("System", 20.0)
                label.text = when (gameEngine.playerWinColor[i]) {
                    3 -> "\nКрасная мышь"
                    4 -> "\nСиняя мышь"
                    5 -> "\nЗеленая мышь"
                    6 -> "\nЖелтая мышь"
                    else -> ""
                }
                winnerList.add(label)
            }
            run {
                val img = ImageView()
                img.fitWidth = 80 * 1.0;
                img.fitHeight = 50 * 1.0;
                img.image = when (gameEngine.playerWinColor[i]) {
                    3 -> playersimgs[0]
                    4 -> playersimgs[1]
                    5 -> playersimgs[2]
                    6 -> playersimgs[3]
                    else -> Image("/img/nothing.png")
                }
                winnerList.add(img)
            }
        }
    }

    fun onBack() {
        gameEngine.backToLobbyConnect()
    }

}
