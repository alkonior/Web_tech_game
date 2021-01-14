package application

import field.CellValue
import field.GameField
import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import models.GameFieldModel
import tornadofx.ChangeListener
import tornadofx.View

class Game : View("Mice in lab.") {
    override val root: AnchorPane by fxml("/game.fxml")

    val fieldview: GridPane by fxid()

    private var screen_width: Int = 0
    private var screen_height: Int = 0
    private val cellSize = 64

    private var max_screen_width: Int = 100
    private var max_screen_height: Int = 100

    private var current_pos_x: Int = 0;
    private var current_pos_y: Int = 0;


    private val field: GameFieldModel by inject()

    private val voidimg = Image("/void.png")
    private val wallimg = Image("/wall.png")
    private val krisaimg = Image("/krisa.png")
    private val fogimg = Image("/fog.png")
    private val firstplayerimg = Image("/first.png")
    private val nothingimg = Image("/nothing.png")

    private var clicked_point_x = -1;
    private var clicked_point_y = -1;

    private var firstLayer = mutableListOf<MutableList<ImageView>>()

    private var secondLayer = mutableListOf<MutableList<ImageView>>()

    init {

        screen_height = ((fieldview.prefHeight.toInt() + cellSize - 1) / cellSize);
        screen_width = ((fieldview.prefWidth.toInt() + cellSize - 1) / cellSize);

        currentStage?.setResizable(false)
        for (i in 1..max_screen_height) {
            fieldview.columnConstraints.add(ColumnConstraints(cellSize * 1.0))
        }

        for (i in 1..max_screen_width) {
            fieldview.columnConstraints.add(ColumnConstraints(cellSize * 1.0))
        }

        for (i in 0..(max_screen_width - 1)) {
            firstLayer.add(mutableListOf())
            secondLayer.add(mutableListOf())
            for (j in 0..(max_screen_height - 1)) {

                firstLayer[i].add(ImageView())
                secondLayer[i].add(ImageView())

                var image1 = firstLayer[i][j]
                var image2 = secondLayer[i][j]
                image1.fitWidth = cellSize * 1.0;
                image1.fitHeight = cellSize * 1.0;
                image2.fitWidth = cellSize * 1.0;
                image2.fitHeight = cellSize * 1.0;

                fieldview.add(image1, i, j)
                fieldview.add(image2, i, j)


            }
        }
        firstLayer[6][5].image = krisaimg
        secondLayer[6][5].image = firstplayerimg

        field.field.field.addListener(InvalidationListener { field ->
            upade_view(field as GameField)
        })


        fieldview.onMouseDragged = EventHandler<MouseEvent> { event -> dragNdrop(event) }
        fieldview.onMousePressed = EventHandler<MouseEvent> { event ->
            run {
                last_point = Point2D(event.getSceneX(), event.getSceneY());
            }
        }
        fieldview.onMouseReleased = EventHandler<MouseEvent> { event ->
            run {
                last_point = Point2D(event.getSceneX(), event.getSceneY());
                draging = false;
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
        upade_view(field.field.field)
        fieldview.onMouseClicked = EventHandler<MouseEvent> { event ->
            run {
                val point = Point2D(event.getSceneX(), event.getSceneY())
                println(
                    "x = ${(((event.getSceneX() - field_margine_x).toInt()) / cellSize) + current_pos_x};" +
                            " y = ${(((event.getSceneY() - field_margine_y).toInt()) / cellSize) + current_pos_y}"
                );
                clicked_point_x = (((event.getSceneX() - field_margine_x).toInt()) / cellSize) + current_pos_x
                clicked_point_y = (((event.getSceneY() - field_margine_y).toInt()) / cellSize) + current_pos_y
                mouse_clicked(clicked_point_x,clicked_point_y);

            }
        }
    }

    var last_point = Point2D(0.0, 0.0);

    var field_margine_x = 0.0;
    var field_margine_y = 0.0;

    var draging = false;

    fun dragNdrop(event: MouseEvent) {
        if (event.button == MouseButton.PRIMARY) {
            var current_point = Point2D(event.sceneX, event.sceneY);

            val deltaX: Double = event.sceneX - last_point.x
            val deltaY: Double = event.sceneY - last_point.y


            if (draging) {

                field_margine_x += deltaX;
                field_margine_y += deltaY;

                run {
                    while (field_margine_x > 0) {
                        field_margine_x -= cellSize;
                        current_pos_x--
                        upade_view(field.field.field);
                    }
                    while (field_margine_y > 0) {
                        field_margine_y -= cellSize;
                        current_pos_y--
                        upade_view(field.field.field);
                    }
                    while (field_margine_x < -cellSize) {
                        field_margine_x += cellSize;
                        current_pos_x++
                        upade_view(field.field.field);
                    }
                    while (field_margine_y < -cellSize) {
                        field_margine_y += cellSize;
                        current_pos_y++
                        upade_view(field.field.field);
                    }

                }

                AnchorPane.setLeftAnchor(fieldview, field_margine_x);
                AnchorPane.setTopAnchor(fieldview, field_margine_y);
                last_point = current_point
            }
            if (Math.abs(deltaX)+Math.abs(deltaY)>5) {
                draging = true;
                last_point = current_point
            }
        }
        event.consume()
    }

    fun upade_view(field: GameField) {


        for (i in 0..(screen_width)) {
            for (j in 0..(screen_height)) {


                val image1 = firstLayer[i][j]
                val image2 = secondLayer[i][j]

                val cell = field[i + current_pos_x, j + current_pos_y];


                var i2 =
                when (cell.shadow) {
                    0 -> nothingimg
                    1 ->  fogimg
                    else -> nothingimg
                }
                var i1 =
                when (cell.value) {
                    CellValue.VOID -> fogimg;
                    CellValue.FLOOR -> voidimg;
                    CellValue.WALL -> wallimg;
                    CellValue.RED -> {
                        if (cell.shadow == 0) {
                            i2 = firstplayerimg
                            krisaimg
                        } else {
                            voidimg
                        }
                    }

                    CellValue.EXIT ->  krisaimg;
                    else -> nothingimg
                }

                if (image1.image!=i1)
                {
                    image1.image = i1
                }
                if (image2.image!=i2)
                {
                    image2.image = i2
                }
            }
        }
    }


    fun mouse_clicked(x:Int,y:Int)
    {

    }

}



