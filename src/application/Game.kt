package application

import field.CellValue
import field.GameField
import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.geometry.Point3D
import tornadofx.ChangeListener
import tornadofx.View
import tornadofx.anchorpane
import tornadofx.minus

class Game : View("Game") {
    override val root: AnchorPane by fxml("/game.fxml")

    val fieldview: GridPane by fxid()

    private var screen_width:Int = 0
    private var screen_height:Int = 0
    private val cellSize = 64

    private var max_screen_width:Int = 100
    private var max_screen_height:Int = 100

    private var current_pos_x:Int = 0;
    private var current_pos_y:Int = 0;


    private val field: GameFieldModel by inject()

    private val voidimg = Image("/void.png")
    private val wallimg = Image("/wall.png")
    private val krisaimg = Image("/krisa.png")
    private val fogimg = Image("/fog.png")
    private val firstplayerimg = Image("/first.png")

    private var firstLayer = mutableListOf<MutableList<ImageView>>()

    private var secondLayer = mutableListOf<MutableList<ImageView>>()

    init {

        screen_height = ((fieldview.prefHeight.toInt()+cellSize-1)/cellSize);
        screen_width= ((fieldview.prefWidth.toInt()+cellSize-1)/cellSize);

        currentStage?.setResizable(false)
        for (i in 1..max_screen_height) {
            fieldview.columnConstraints.add(ColumnConstraints(cellSize*1.0))
        }

        for (i in 1..max_screen_width) {
            fieldview.columnConstraints.add(ColumnConstraints(cellSize*1.0))
        }

        for (i in 0..(max_screen_width - 1))
        {
            firstLayer.add(mutableListOf())
            secondLayer.add(mutableListOf())
            for (j in 0..(max_screen_height - 1)) {

                firstLayer[i].add(ImageView())
                secondLayer[i].add(ImageView())

                var image1 = firstLayer[i][j]
                var image2 = secondLayer[i][j]
                image1.fitWidth = cellSize*1.0;
                image1.fitHeight = cellSize*1.0;
                image2.fitWidth = cellSize*1.0;
                image2.fitHeight = cellSize*1.0;

                fieldview.add(image1, i, j)
                fieldview.add(image2, i, j)


            }
        }
        firstLayer[6][5].image = krisaimg
        secondLayer[6][5].image = firstplayerimg

        field.field.addListener(InvalidationListener  { field ->
            upade_view(field as GameField)
        })

        field.field[0,0]= GameField.CellInfo(CellValue.RED,0);

        fieldview.onMouseDragged = EventHandler<MouseEvent>{event -> dragNdrop(event)}
        fieldview.onMousePressed = EventHandler<MouseEvent>{
           event ->
            run {
                last_point = Point2D(event.getSceneX(), event.getSceneY());
            }
        }


        val stageSizeListener: ChangeListener<Number> =
            ChangeListener<Number> { observable, oldValue, newValue ->
                run {
                    screen_height = (((currentStage?.getHeight()?.toInt() ?: 0) + cellSize - 1) / cellSize);
                    screen_width = (((currentStage?.getWidth()?.toInt() ?: 0) + cellSize - 1) / cellSize);
                    println(
                        "Height: " + screen_height + " Width: " + screen_width
                    )
                }
            }

        currentStage?.widthProperty()?.addListener(stageSizeListener)
        currentStage?.heightProperty()?.addListener(stageSizeListener)

        currentStage?.setResizable(true)
    }

    var last_point = Point2D(0.0,0.0);

    var field_margine_x = 0.0;
    var field_margine_y = 0.0;

    fun dragNdrop(event :MouseEvent)
    {
        if (event.button == MouseButton.PRIMARY) {
            var current_point = Point2D(event.sceneX,event.sceneY);

            val deltaX: Double = event.sceneX - last_point.x
            val deltaY: Double = event.sceneY - last_point.y
            field_margine_x += deltaX;
            field_margine_y += deltaY;
                run {
                   while (field_margine_x > 0)
                   {
                       field_margine_x -=cellSize;
                       current_pos_x++
                       upade_view(field.field);
                   }
                   while (field_margine_y > 0)
                   {
                       field_margine_y -=cellSize;
                       current_pos_y++
                       upade_view(field.field);
                   }
                   while (field_margine_x < -cellSize)
                   {
                       field_margine_x +=cellSize;
                       current_pos_x--
                       upade_view(field.field);
                   }
                   while (field_margine_y <-cellSize)
                   {
                       field_margine_y +=cellSize;
                       current_pos_y--
                       upade_view(field.field);
                   }
                }

            AnchorPane.setLeftAnchor(fieldview, field_margine_x);
            AnchorPane.setTopAnchor(fieldview, field_margine_y);
            last_point = current_point
        }
        event.consume()
    }

    fun upade_view(field:GameField)
    {


        for (i in 0..(screen_width  - 1))
        {
            for (j in 0..(screen_height - 1)) {


                val image1 = firstLayer[i][j]
                val image2 = secondLayer[i][j]

                val cell = field[i+current_pos_x,j+current_pos_y];

                when(cell.value){
                    CellValue.VOID -> image1.image = voidimg;
                    CellValue.FLOOR -> image1.image = voidimg;
                    CellValue.WALL -> image1.image = wallimg;
                    CellValue.RED -> {image1.image = krisaimg; image2.image = firstplayerimg }
                    CellValue.EXIT -> {image1.image = krisaimg; }

                }


            }
        }
    }


}
