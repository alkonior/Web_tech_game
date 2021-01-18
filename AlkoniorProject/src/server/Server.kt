package server

import java.net.Socket
import java.net.SocketException
import java.util.*


class Server {

    private lateinit var _socket :Socket
    private lateinit var _scanner :Scanner

    fun connect(ip: String, port: Int): Boolean {
        if (this::_socket.isInitialized) {
            _socket.close();
        }

        _socket = Socket(ip, port);
        _scanner = Scanner(_socket.getInputStream())
        return _socket.isConnected;
    }

    fun sendMess(mess: String): Boolean {
        if(_socket.isConnected)
        {
            try {
                _socket.outputStream.write(mess.toByteArray())
            }catch (exception:Throwable){
                throw SocketException("Socket write error")
            }
        }else{
            throw SocketException("Socket isn't connected")
        }
        return true
    }

    fun getMess():String
    {
        return readLine().toString()
        if (this::_scanner.isInitialized)
            return _scanner.nextLine();
        else
            throw Throwable("Server isn't connected")
    }
}