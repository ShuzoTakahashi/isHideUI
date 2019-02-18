import com.leapmotion.leap.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal class GestureListener(private val onSwipe: (Gesture) -> Unit, private val onScreenTap: (Gesture) -> Unit) :
    Listener(), CoroutineScope {

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
        controller.frame().also { frame ->
            runBlocking {
                frame.gestures().also { gestures ->
                    gestures.forEach { gesture ->
                        when (gesture.type()) {
                            Gesture.Type.TYPE_SWIPE -> {
                                println("スワイプ")
                                onSwipe(gesture)
                                delay(800L)
                            }

                            Gesture.Type.TYPE_SCREEN_TAP -> {
                                println("タップ")
                                onScreenTap(gesture)
                                delay(800L)
                            }
                            else -> println("Unknown gesture type.")
                        }
                    }
                }
            }
        }
    }
}