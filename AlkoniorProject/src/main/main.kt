
import application.ApplicationApp
import tornadofx.launch


fun main(args: Array<String>) {
    println("Hello World!")
    println(ApplicationApp::class.java.module.toString())
    println(ApplicationApp::class.java.getResource("/views/connect.fxml").toString())
    launch<ApplicationApp>(args)
}