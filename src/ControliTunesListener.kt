import com.leapmotion.leap.Controller
import com.leapmotion.leap.Gesture
import com.leapmotion.leap.SwipeGesture
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class ControliTunesListener : WithCoroutineListener() {
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
                        delay(180L)
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
                        delay(370L)
                    }
                }
                else -> println("Unknown gesture type.")
            }
        }
    }

}