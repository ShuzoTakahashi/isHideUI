import java.io.IOException
import com.leapmotion.leap.*
import communication.ComTcpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import communication.ComTcpClient.ComState.*

class ControlLight : Listener() {

    private val channel by lazy {
        Channel<Enum<ComTcpClient.ComState>>().also { channel ->
            GlobalScope.launch(Dispatchers.Default) {
                when (channel.receive()) {

                    MSG_IOEXCEPTION -> {
                        // TODO : socketのclose処理
                    }

                    MSG_CONNECTION_FAILED -> {
                        // TODO : socketのclose処理
                    }

                    MSG_CONNECTION_SUCCESS -> {
                        print("らずぱいせつぞく")
                    }
                }
            }
        }
    }
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
        val frame = controller.frame()


        val gestures = frame.gestures()
        for (i in 0 until gestures.count()) {
            val gesture = gestures.get(i)

            when (gesture.type()) {

                Gesture.Type.TYPE_SWIPE -> {
                    println("スワイプ")
                    val swipe = SwipeGesture(gesture)
                    var lightStateSwipe = false

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

                Gesture.Type.TYPE_KEY_TAP -> {
                    println("key tap！")
                    val keyTap = KeyTapGesture(gesture)
                    println(
                        "  Key Tap id: " + keyTap.id()
                                + ", " + keyTap.state()
                                + ", position: " + keyTap.position()
                                + ", direction: " + keyTap.direction()
                    )
                }

                else -> println("Unknown gesture type.")
            }
        }

        if (!frame.hands().isEmpty || !gestures.isEmpty) {
            println()
        }
    }
}


fun main() {
    val listener = ControlLight()
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
