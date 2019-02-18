import com.leapmotion.leap.Controller
import com.leapmotion.leap.Gesture
import com.leapmotion.leap.SwipeGesture
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
                            }
                        }
                    }
                }).apply {
                connect()
            }

            // listenerを作成
            object : WithCoroutineListener() {
                private var isLightON = false

                override fun onConnect(controller: Controller) {
                    println("Connected")
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
                                    delay(800L)
                                }
                            }
                            else -> println("Unknown gesture type.")
                        }

                    }
                }

                override fun onDisconnect(p0: Controller?) {
                    if (connection.isConnected) {
                        connection.getIO { output, _ ->
                            output.write(0)
                        }
                        connection.close()
                    }
                }
            }

        }

        "music" -> {
            // listenerを作成
            object : WithCoroutineListener() {
                override fun onConnect(controller: Controller) {
                    println("Connected")
                    controller.apply {
                        enableGesture(Gesture.Type.TYPE_KEY_TAP)
                        enableGesture(Gesture.Type.TYPE_SWIPE)
                    }
                }

                override fun onFrame(controller: Controller) {
                    controller.frame().gestures().forEach { gesture ->
                        when (gesture.type()) {
                            Gesture.Type.TYPE_KEY_TAP -> {
                                runBlocking {
                                    println("タップ")
                                    Runtime.getRuntime().apply {
                                        val args = arrayOf("osascript", "-e", "tell app \"iTunes\" to playpause")
                                        launch {
                                            exec(args)
                                        }
                                    }
                                    delay(800L)
                                }
                            }

                            Gesture.Type.TYPE_SWIPE -> {
                                runBlocking {
                                    println("スワイプ")
                                    val swipe = SwipeGesture(gesture)
                                    Runtime.getRuntime().apply {
                                        val args =
                                            if (swipe.direction().x > 0) {
                                                arrayOf("osascript", "-e", "tell app \"iTunes\" back track")
                                            } else {
                                                arrayOf("osascript", "-e", "tell app \"iTunes\" to next track")
                                            }
                                        launch {
                                            exec(args)
                                        }
                                    }
                                    delay(800L)
                                }
                            }
                            else -> println("Unknown gesture type.")
                        }
                    }
                }

            }
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
