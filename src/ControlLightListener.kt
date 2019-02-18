import com.leapmotion.leap.Controller
import com.leapmotion.leap.Gesture
import communication.ComTcpClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

// listenerを作成
internal class ControlLightListener(private val connection: ComTcpClient) : WithCoroutineListener() {
    private var isLightON = false

    override fun onConnect(controller: Controller) {
        println("Connected")
        connection.connect()
        controller.enableGesture(Gesture.Type.TYPE_KEY_TAP)
    }

    override fun onFrame(controller: Controller) {
        controller.frame().gestures().forEach { gesture ->

            when (gesture.type()) {
                Gesture.Type.TYPE_KEY_TAP -> {
                    runBlocking {
                        println("タップ")
                        connection.getIO { output, _ ->
                            isLightON = if (isLightON) {
                                output.write("off".toByteArray())
                                false
                            } else {
                                output.write("on".toByteArray())
                                true
                            }
                        }
                        delay(180L)
                    }
                }
                else -> println("Unknown gesture type.")
            }

        }
    }

    override fun onExit(p0: Controller?) {
        println("onExit")
        if (connection.isConnected) {
            connection.getIO { output, _ ->
                output.write("QUIT".toByteArray())
            }
            connection.close()
        }
    }
}