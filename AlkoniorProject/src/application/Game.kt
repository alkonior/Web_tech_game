package application

import field.CellValue
import field.GameField
import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import models.GameFieldModel
import tornadofx.ChangeListener
import tornadofx.View

class Game : View("Mice in lab.") {
    override val root: AnchorPane by fxml("/game.fxml")

    val fieldview: GridPane by fxid()
    val fieldcontaner: AnchorPane by fxid()
    val SessionId: Label by fxid()

    private var screen_width: Int = 0
    private var screen_height: Int = 0
    private val cellSize = 64

    private var max_screen_width: Int = 100
    private var max_screen_height: Int = 100


    var border = 3;

    private var current_pos_x: Int = 0;
    private var current_pos_y: Int = 0;


    private val field_: GameFieldModel by inject()
    private val field = field_.field.field

    private val voidimg = Image("/void.png")
    private val wallimg = Image("/wall.png")
    private val krisaimg = Image("/krisa.png")
    private val fogimg = Image("/fog.png")
    private val firstplayerimg = Image("/first.png")
    private val nothingimg = Image("/nothing.png")

    private var clicked_point_x = 0;
    private var clicked_point_y = 0;

    var last_point = Point2D(0.0, 0.0);

    var field_margine_x = current_pos_x*cellSize*1.0;
    var field_margine_y = current_pos_y*cellSize*1.0;



    private var firstLayer = mutableListOf<MutableList<ImageView>>()
    private var secondLayer = mutableListOf<MutableList<ImageView>>()
    private var textLayer = mutableListOf<MutableList<Label>>()

    init {
    /*
        screen_height = ((fieldview.prefHeight.toInt() + cellSize - 1) / cellSize)+2*border;
        screen_width = ((fieldview.prefWidth.toInt() + cellSize - 1) / cellSize)+2*border;
    */

        screen_height = field.height;
        screen_width = field.width;

        max_screen_height = ((fieldview.prefHeight.toInt() + cellSize - 1) / cellSize)
        max_screen_width = ((fieldview.prefWidth.toInt() + cellSize - 1) / cellSize)

        currentStage?.setResizable(false)
        for (i in 1..screen_height) {
            fieldview.columnConstraints.add(ColumnConstraints(cellSize * 1.0))
        }

        for (i in 1..screen_width) {
            fieldview.columnConstraints.add(ColumnConstraints(cellSize * 1.0))
        }

        for (i in 0..(screen_width - 1)) {
            firstLayer.add(mutableListOf())
            secondLayer.add(mutableListOf())
            textLayer.add(mutableListOf())
            for (j in 0..(screen_height - 1)) {

                firstLayer[i].add(ImageView())
                secondLayer[i].add(ImageView())
                textLayer[i].add(Label(""))

                var image1 = firstLayer[i][j]
                var image2 = secondLayer[i][j]
                var label = textLayer[i][j]

                image1.fitWidth = cellSize * 1.0;
                image1.fitHeight = cellSize * 1.0;
                image2.fitWidth = cellSize * 1.0;
                image2.fitHeight = cellSize * 1.0;

                label.minWidth = cellSize * 1.0;
                label.maxWidth = cellSize * 1.0;
                label.setStyle("-fx-border-color: rgba(0, 0, 0, 0.0);");
                label.alignment = (Pos.CENTER);

                fieldview.add(image1, i, j)
                fieldview.add(image2, i, j)
                fieldview.add(label, i, j)

                image1.isDisable = true;
                image2.isDisable = true;
                label.isDisable = true;

            }
        }
        firstLayer[6][5].image = krisaimg
        secondLayer[6][5].image = firstplayerimg

        field.addListener(InvalidationListener { field ->
            upade_view(field as GameField)
        })


        fieldcontaner.onMouseDragged = EventHandler<MouseEvent> { event -> dragNdrop(event) }
        fieldcontaner.onMousePressed = EventHandler<MouseEvent> { event ->
            run {
                last_point = Point2D(event.getSceneX(), event.getSceneY());
            }
        }
        fieldcontaner.onMouseReleased = EventHandler<MouseEvent> { event ->
            run {
                last_point = Point2D(event.getSceneX(), event.getSceneY());
                draging = false;
            }
        }


        val stageSizeListener: ChangeListener<Number> =
            ChangeListener<Number> { observable, oldValue, newValue ->
                run {
                    max_screen_height = (((currentStage?.getHeight()?.toInt() ?: 0) + cellSize - 1) / cellSize);
                    max_screen_width = (((currentStage?.getWidth()?.toInt() ?: 0) + cellSize - 1) / cellSize);
                    println(
                        "Height: " + max_screen_height + " Width: " + max_screen_width
                    )
                    fixmagrine(0.0,0.0);
                }
            }

        currentStage?.widthProperty()?.addListener(stageSizeListener)
        currentStage?.heightProperty()?.addListener(stageSizeListener)

        currentStage?.setResizable(true)
        upade_view(field)
        fieldcontaner.onMouseClicked = EventHandler<MouseEvent> { event ->
            run {
                val point = Point2D(event.getSceneX(), event.getSceneY())
                println(
                    "x = ${(((event.getSceneX() - field_margine_x).toInt()) / cellSize) + current_pos_x};" +
                            " y = ${(((event.getSceneY() - field_margine_y).toInt()) / cellSize) + current_pos_y}\n"+
                            "field_margine_x = ${field_margine_x}, field_margine_y = ${field_margine_y}\n" +
                            "current_pos_x = ${current_pos_x}, current_pos_y = ${current_pos_y}," +
                            ""
                );
                clicked_point_x = (((event.getSceneX() - field_margine_x).toInt()) / cellSize) + current_pos_x
                clicked_point_y = (((event.getSceneY() - field_margine_y).toInt()) / cellSize) + current_pos_y
                mouse_clicked(clicked_point_x,clicked_point_y);

            }
        }
        AnchorPane.setLeftAnchor(fieldview, field_margine_x);
        AnchorPane.setTopAnchor(fieldview, field_margine_y);

        SessionId.text = field_.field.sessionId
    }

    var draging = false;

    fun fixmagrine(deltaX: Double,
                   deltaY: Double){
        if (( -(screen_width - border -1)*cellSize <= field_margine_x+deltaX) and (field_margine_x+deltaX <= (max_screen_width-border-1)*cellSize))
        {
            field_margine_x += deltaX;
        }else
        {
            if ( -(screen_width - border -1)*cellSize > field_margine_x+deltaX)
            {
                field_margine_x = -(screen_width - border -1)*cellSize*1.0
            }
            if(field_margine_x+deltaX > (max_screen_width-border-1)*cellSize)
            {
                field_margine_x = (max_screen_width-border-1)*cellSize*1.0
            }
        }

        if (( -(screen_height - border -1)*cellSize+40 <= field_margine_y+deltaY) and (field_margine_y+deltaY <= (max_screen_height-border-1)*cellSize-30)){
            field_margine_y += deltaY;
        }else
        {
            if (field_margine_y+deltaY > (max_screen_height-border-1)*cellSize-30)
            {
                field_margine_y = (max_screen_height-border-1)*cellSize-30*1.0
            }
            if( -(screen_height - border -1)*cellSize+40 > field_margine_y+deltaY)
            {
                field_margine_y = -(screen_height-border-1)*cellSize*1.0+40
            }
        }
        AnchorPane.setLeftAnchor(fieldview, field_margine_x);
        AnchorPane.setTopAnchor(fieldview, field_margine_y);
    }

    fun dragNdrop(event: MouseEvent) {
        if (event.button == MouseButton.PRIMARY) {
            var current_point = Point2D(event.sceneX, event.sceneY);

            val deltaX: Double = event.sceneX - last_point.x
            val deltaY: Double = event.sceneY - last_point.y


            if (draging) {


                fixmagrine(deltaX,deltaY);


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
        for (i in 0..(screen_width-1)) {
            for (j in 0..(screen_height-1)) {


                val image1 = firstLayer[i][j]
                val image2 = secondLayer[i][j]
                val label = textLayer[i][j]

                val cell = field[i + current_pos_x, j + current_pos_y];


                var i2 =
                when (cell.shadow) {
                    0 -> nothingimg
                    else -> fogimg
                }
                var i1 =
                when (cell.value) {
                    CellValue.VOID -> fogimg;
                    CellValue.FLOOR -> voidimg;
                    CellValue.WALL -> wallimg;
                    CellValue.EXIT ->  krisaimg;
                }

                if (image1.image!=i1)
                {
                    image1.image = i1
                }
                if (image2.image!=i2)
                {
                    image2.image = i2
                }
                if ( label.text != cell.text)
                {
                    label.text = cell.text
                }

                image1.isDisable = false;
                image2.isDisable = false;
                label.isDisable = false;
            }
        }
    }


    fun mouse_clicked(x:Int,y:Int)
    {

    }

}



