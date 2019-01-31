package communication

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.*
import java.net.Socket
import java.net.UnknownHostException

/**
 * Created by shuzotakahashi on 2018/01/13.
 */

class ComTcpClient(private val ip: String, private val port: Int, private val channel: Channel<Enum<ComState>>) {

    enum class ComState {
        MSG_CONNECTION_SUCCESS, MSG_CONNECTION_FAILED, MSG_TCP_IOEXCEPTION
    }

    private var socket: Socket? = null

    var isConnected: Boolean = false
        get() = socket?.isConnected ?: false

    fun connect() {
        GlobalScope.launch(Dispatchers.Default) {
            print("接続開始...")
            try {
                socket = Socket(ip, port)
                channel.send(ComState.MSG_CONNECTION_SUCCESS)

            } catch (e: IOException) {
                print("IOException")
                channel.send(ComState.MSG_TCP_IOEXCEPTION)

            } catch (e: UnknownHostException) {
                print("UnknownHostException")
                channel.send(ComState.MSG_CONNECTION_FAILED)
            }
        }
    }

    // TODO: リネーム
    fun getIO(func: (OutputStream, InputStream) -> Unit) {
        if (socket == null) throw java.lang.IllegalStateException()
        socket?.also { socket ->
            GlobalScope.launch(Dispatchers.Default) {
                try {
                    if (socket.isConnected) {
                        func(socket.outputStream, socket.inputStream)
                    } else {
                        print("接続されていない。")
                        channel.send(ComState.MSG_CONNECTION_FAILED)
                        throw IllegalStateException()
                    }
                } catch (e: IOException) {
                    channel.send(ComState.MSG_TCP_IOEXCEPTION)
                }
            }
        }
    }

    fun close() {
        if (socket == null) throw java.lang.IllegalStateException()
        socket?.also { socket ->
            GlobalScope.launch(Dispatchers.Default) {
                try {
                    if (socket.isConnected) socket.close()

                } catch (e: IOException) {
                    print("close()にてエラー発生")
                    channel.send(ComState.MSG_TCP_IOEXCEPTION)
                }
            }
        }
    }

}