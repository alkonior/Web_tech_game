package server

import java.net.Socket
import java.net.SocketException
import java.nio.charset.Charset
import java.util.*


class Server {

    private lateinit var _socket :Socket
    private lateinit var _scanner :Scanner

    fun connect(ip: String, port: Int): Boolean {
        try {
            if (this::_socket.isInitialized) {
                _socket.close();
            }

            _socket = Socket(ip, port);
            _scanner = Scanner(_socket.getInputStream())

            var mess = getMess()
            if (mess == "500")
                return _socket.isConnected
            else
                return false
        }catch (ex:Throwable)
        {
            throw Throwable("Couldn't connect to server.")
        }
    }

    fun sendMess(mess: String): Boolean {
        if(_socket.isConnected)
        {
            try {
                _socket.getOutputStream().write((mess + '\n').toByteArray(Charset.defaultCharset()))
                println(mess)
            } catch (exception:Throwable){

            }
        }else{

        }
        return true
    }

    fun getMess():String
    {

        if (this::_scanner.isInitialized){
            val mes = _scanner.nextLine();
            println(mes)
            return mes
        }
        else
            throw Throwable("Server isn't connected")
    }

    fun close() {
        _socket.close()
    }

}