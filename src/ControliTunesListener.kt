import com.leapmotion.leap.Controller
import com.leapmotion.leap.Gesture
import com.leapmotion.leap.SwipeGesture
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class ControliTunesListener : WithCoroutineListener() {
    private var canSwipe = true
    private var canTap = true

    // デバッグ用
    private var tapCnt = 0
    private var swipeCnt = 0

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
                        if (canTap) {
                            canTap = false
                            Runtime.getRuntime().apply {
                                arrayOf("osascript", "-e", "tell app \"iTunes\" to playpause").also { cmd ->
                                    launch { exec(cmd) }
                                    tapCnt += 1
                                    println("タップ: $tapCnt")
                                }
                            }
                            delay(150L)
                            canTap = true
                        }
                    }
                }

                Gesture.Type.TYPE_SWIPE -> {
                    runBlocking {
                        if (canSwipe) {
                            canSwipe = false
                            val swipe = SwipeGesture(gesture)
                            Runtime.getRuntime().apply {
                                if (swipe.direction().x > 0) {
                                    arrayOf("osascript", "-e", "tell app \"iTunes\" back track")
                                } else {
                                    arrayOf("osascript", "-e", "tell app \"iTunes\" to next track")
                                }.also { cmd ->
                                    launch { exec(cmd) }
                                    swipeCnt += 1
                                    println("スワイプ: $swipeCnt")
                                }
                            }
                            delay(640L)
                            canSwipe = true
                        }
                    }
                }
                else -> println("Unknown gesture type.")
            }
        }
    }
}