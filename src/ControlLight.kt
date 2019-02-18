import java.io.IOException
import com.leapmotion.leap.*
import communication.ComTcpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class ControlLight(val channel: Channel<Enum<ComTcpClient.ComState>>) : Listener() {

    private lateinit var connection: ComTcpClient

    override fun onInit(controller: Controller) = println("Initialized")

    override fun onConnect(controller: Controller) {
        println("Connected")
        controller.enableGesture(Gesture.Type.TYPE_SWIPE)
        connection = ComTcpClient("172.20.10.4", 55555, channel)
        connection.connect()
    }

    override fun onDisconnect(controller: Controller) = println("Disconnected")

    override fun onExit(controller: Controller) = println("Exited")

    override fun onFrame(controller: Controller) {

        controller.frame().also { frame ->
            frame.gestures().also { gestures ->
                gestures.forEach { gesture ->
                    when (gesture.type()) {
                        Gesture.Type.TYPE_SWIPE -> {
                            val swipe = SwipeGesture(gesture)
                            var lightStateSwipe = false
                            println("スワイプ")

                            connection.getIO { output, _ ->
                                output.write("on".toByteArray())
                            }
                        }

                        Gesture.Type.TYPE_SCREEN_TAP -> {
                            println("タップ")
                            val screenTap = ScreenTapGesture(gesture)

                            connection.getIO { output, _ ->
                                output.write("on".toByteArray())
                            }
                        }
                        else -> println("Unknown gesture type.")
                    }
                }
            }
        }
    }
}


fun main() {

    val channel by lazy {
        Channel<Enum<ComTcpClient.ComState>>().also { channel ->
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
    }

    val listener = ControlLight(channel)
    val controller = Controller()

    controller.addListener(listener)

    println("Press Enter to quit... ")
    try {
        System.`in`.read()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    controller.removeListener(listener)
}
