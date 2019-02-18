import com.leapmotion.leap.Controller
import com.leapmotion.leap.SwipeGesture
import communication.ComTcpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.IllegalStateException

fun main(args: Array<String>) {

    val controller = Controller()

    val listener: GestureListener = when (args[0]) {

        "light" -> {
            val connection = ComTcpClient("172.20.10.4", 55555,
                Channel<Enum<ComTcpClient.ComState>>().apply {
                    GlobalScope.launch(Dispatchers.Default) {
                        when (receive()) {
                            ComTcpClient.ComState.MSG_IOEXCEPTION -> {
                                // TODO : socketのclose処理
                            }
                            ComTcpClient.ComState.MSG_CONNECTION_FAILED -> {
                                // TODO : socketのclose処理
                            }
                            ComTcpClient.ComState.MSG_CONNECTION_SUCCESS -> {
                                println("RaspberryPiと接続完了")
                            }
                        }
                    }
                }).apply {
                connect()
            }

            // listenerを作成
            GestureListener(
                onSwipe = {
                    if (!connection.isConnected) throw IllegalStateException()
                    connection.getIO { output, _ ->
                        output.write("on".toByteArray())
                    }
                },
                onScreenTap = {
                    connection.getIO { output, _ ->
                        output.write("on".toByteArray())
                    }
                })
        }

        "music" -> {
            // listenerを作成
            GestureListener(
                onSwipe = { gesture ->
                    val swipe = SwipeGesture(gesture)
                    Runtime.getRuntime().apply {
                        val args =
                            if (swipe.direction().x > 0) arrayOf("osascript", "-e", "tell app \"iTunes\" back track")
                            else arrayOf("osascript", "-e", "tell app \"iTunes\" to next track")
                        exec(args)
                    }
                },
                onScreenTap = {
                    Runtime.getRuntime().apply {
                        exec(arrayOf("osascript", "-e", "tell app \"iTunes\" to playpause"))
                    }
                })
        }

        else -> throw IllegalStateException()
    }

    controller.addListener(listener)

    println("Press Enter to quit... ")
    try {
        System.`in`.read()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    controller.removeListener(listener)

}
