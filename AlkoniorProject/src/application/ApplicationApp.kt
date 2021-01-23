package application

import tornadofx.App
import tornadofx.launch

class ApplicationApp : App() {
    override val primaryView = ConnectServer::class
}
