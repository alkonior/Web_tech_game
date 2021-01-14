package application

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import tornadofx.*

class Game : View("Game") {
    override val root: AnchorPane by fxml("/game.fxml")

    val fieldview: GridPane by fxid()

    private var screen_width:Int = 0
    private var screen_height:Int = 0
    private val cellSize = 64

    private val field: GameFieldModel by inject()

    private val voidimg = Image("/void.png")
    private val wallimg = Image("/wall.png")
    private val krisaimg = Image("/krisa.png")
    private val fogimg = Image("/fog.png")
    private val firstplayerimg = Image("/first.png")

    private var firstLayer = mutableListOf<MutableList<ImageView>>()

    private var secondLayer = mutableListOf<MutableList<ImageView>>()

    init {

        screen_height = ((fieldview.height.toInt()+cellSize-1)/cellSize);
        screen_width= ((fieldview.width.toInt()+cellSize-1)/cellSize);

        val field = field.field
        currentStage?.setResizable(false)
        for (i in 1..screen_height) {
            fieldview.columnConstraints.add(ColumnConstraints(40.0))
        }

        for (i in 1..screen_width) {
            fieldview.columnConstraints.add(ColumnConstraints(40.0))
        }

        for (i in 0..(field.width - 1))
        {
            firstLayer.add(mutableListOf())
            for (j in 0..(field.height - 1)) {

                firstLayer[i].add(ImageView())
                secondLayer[i].add(ImageView())

                var image1 = firstLayer[i][j]
                var image2 = secondLayer[i][j]
                image1.fitWidth = cellSize*1.0;
                image1.fitHeight = cellSize*1.0;
                image2.fitWidth = cellSize*1.0;
                image2.fitHeight = cellSize*1.0;

                if ((i + j) % 2 == 0) {
                    image1.image = wallimg
                } else
                    image1.image = voidimg
                if ((i ) % 2 == 0) {
                    image2.image = fogimg
                } else
                    image2.image = null


                fieldview.add(image1, i, j)
                fieldview.add(image2, i, j)
            }
        }
        firstLayer[6][5].image = krisaimg
        secondLayer[6][5].image = firstplayerimg



    }


}
