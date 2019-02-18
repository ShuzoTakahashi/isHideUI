import com.leapmotion.leap.Controller
import com.leapmotion.leap.SwipeGesture
import communication.ComTcpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.IllegalStateException

fun main() {
    val channel = Channel<Enum<ComTcpClient.ComState>>().also { channel ->
        GlobalScope.launch(Dispatchers.Default) {
            when (channel.receive()) {

                ComTcpClient.ComState.MSG_IOEXCEPTION -> {
                    // TODO : socketのclose処理
                }

                ComTcpClient.ComState.MSG_CONNECTION_FAILED -> {
                    // TODO : socketのclose処理
                }

                ComTcpClient.ComState.MSG_CONNECTION_SUCCESS -> {
                    print("らずぱいせつぞく")
                }
            }
        }
    }

    val connection = ComTcpClient("172.20.10.4", 55555, channel).apply {
        connect()
    }

    val controller = Controller()

    val lightListener = GestureListener(
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

    val musicLister = GestureListener(
        onSwipe = { gesture ->
            val swipe = SwipeGesture(gesture)
            val runtime = Runtime.getRuntime()
            val args =
                if (swipe.direction().x > 0) {
                    arrayOf("osascript", "-e", "tell app \"iTunes\" back track")
                } else {
                    arrayOf("osascript", "-e", "tell app \"iTunes\" to next track")
                }
            println(args)
            val process = runtime.exec(args)
        },
        onScreenTap = {
            val runtimeTap = Runtime.getRuntime()
            val argsTap = arrayOf("osascript", "-e", "tell app \"iTunes\" to playpause")
            val processTap = runtimeTap.exec(argsTap)
        })

    controller.apply {
        addListener(lightListener)
        addListener(musicLister)
    }

    println("Press Enter to quit... ")
    try {
        System.`in`.read()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    controller.apply {
        removeListener(lightListener)
        removeListener(musicLister)
    }
}
