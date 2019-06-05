import com.leapmotion.leap.Controller
import communication.ComTcpClient
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.IOException
import java.lang.IllegalStateException

fun main(args: Array<String>) {

    val controller = Controller()

    val listener: WithCoroutineListener = when (args[0]) {
        "light" -> {
            val connection = ComTcpClient("10.0.1.10", 55555,
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
                                // TODO : 本来はここで
                            }
                        }
                    }
                })
            ControlLightListener(connection)
        }

        "music" -> ControliTunesListener()

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