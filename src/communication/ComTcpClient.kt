package communication

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.*
import java.net.Socket
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

/**
 * Created by shuzotakahashi on 2018/01/13.
 */

class ComTcpClient(private val ip: String, private val port: Int, private val channel: Channel<Enum<ComState>>) :
    CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    enum class ComState {
        MSG_CONNECTION_SUCCESS, MSG_CONNECTION_FAILED, MSG_IOEXCEPTION
    }

    private var socket: Socket? = null

    var isConnected: Boolean = false
        get() = socket?.isConnected ?: false

    fun connect() {
        runBlocking {
            println("接続開始...")
            try {
                socket = Socket(ip, port)
                channel.send(ComState.MSG_CONNECTION_SUCCESS)

            } catch (e: IOException) {
                println("IOException")
                channel.send(ComState.MSG_IOEXCEPTION)

            } catch (e: UnknownHostException) {
                println("UnknownHostException")
                channel.send(ComState.MSG_CONNECTION_FAILED)
            }
        }
    }

    // TODO: リネーム
    fun getIO(func: (OutputStream, InputStream) -> Unit) {
        if (socket == null) throw IllegalStateException()
        socket?.also { socket ->
            launch {
                try {
                    if (socket.isConnected) {
                        func(socket.outputStream, socket.inputStream)
                    } else {
                        println("接続されていない")
                        channel.send(ComState.MSG_CONNECTION_FAILED)
                        throw IllegalStateException()
                    }
                } catch (e: IOException) {
                    channel.send(ComState.MSG_IOEXCEPTION)
                }
            }
        }
    }

    fun close() {
        if (socket == null) throw IllegalStateException()
        socket?.also { socket ->
            runBlocking {
                try {
                    if (socket.isConnected) socket.close()

                } catch (e: IOException) {
                    println("close()にてエラー発生")
                    channel.send(ComState.MSG_IOEXCEPTION)
                }
            }
        }
    }

}