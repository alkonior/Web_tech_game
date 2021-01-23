package main

import application.ApplicationApp
import kotlin.jvm.JvmStatic
import tornadofx.launch

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        launch<ApplicationApp>(args)
    }
}