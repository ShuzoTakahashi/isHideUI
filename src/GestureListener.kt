import java.io.IOException
import com.leapmotion.leap.*
import communication.ComTcpClient
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.CoroutineContext

internal class GestureListener(private val connection: ComTcpClient) : Listener(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job

    override fun onInit(controller: Controller) = println("Initialized")

    override fun onConnect(controller: Controller) {
        println("Connected")
        controller.apply {
            enableGesture(Gesture.Type.TYPE_SWIPE)
            enableGesture(Gesture.Type.TYPE_SCREEN_TAP)
        }
    }

    override fun onDisconnect(controller: Controller) = println("Disconnected")

    override fun onExit(controller: Controller) = println("Exited")

    override fun onFrame(controller: Controller) {
        if (!connection.isConnected) return

        controller.frame().also { frame ->
            runBlocking{
                frame.gestures().also { gestures ->
                    gestures.forEach { gesture ->
                        when (gesture.type()) {
                            Gesture.Type.TYPE_SWIPE -> {
                                println("スワイプ")

                                val swipe = SwipeGesture(gesture)
                                var lightStateSwipe = false

                                connection.getIO { output, _ ->
                                    output.write("on".toByteArray())
                                }
                                delay(800L)
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
}


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

    val connection = ComTcpClient("172.20.10.4", 55555, channel)
    connection.connect()

    val controller = Controller()
    val listener = GestureListener(connection)

    controller.addListener(listener)

    println("Press Enter to quit... ")
    try {
        System.`in`.read()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    controller.removeListener(listener)
}
