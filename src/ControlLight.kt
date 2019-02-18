import java.io.IOException
import java.lang.Math
import com.leapmotion.leap.*
import com.leapmotion.leap.Gesture.State
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


    override fun onInit(controller: Controller) {
        println("Initialized")
    }

    override fun onConnect(controller: Controller) {
        println("Connected")
        controller.apply {
            enableGesture(Gesture.Type.TYPE_SWIPE)
            enableGesture(Gesture.Type.TYPE_CIRCLE)
            enableGesture(Gesture.Type.TYPE_SCREEN_TAP)
            enableGesture(Gesture.Type.TYPE_KEY_TAP)
        }
        connection = ComTcpClient("172.20.10.4", 55555, channel)
        connection.connect()
    }

    override fun onDisconnect(controller: Controller) {
        //Note: not dispatched when running in a debugger.
        println("Disconnected")
    }

    override fun onExit(controller: Controller) {
        println("Exited")
    }

    override fun onFrame(controller: Controller) {
        // Get the most recent frame and report some basic information
        val frame = controller.frame()
//        println(
//            "Frame id: " + frame.id()
//                    + ", timestamp: " + frame.timestamp()
//                    + ", hands: " + frame.hands().count()
//                    + ", fingers: " + frame.fingers().count()
//                    + ", tools: " + frame.tools().count()
//                    + ", gestures " + frame.gestures().count()
//        )

        //Get hands
        for (hand in frame.hands()) {
            val handType = if (hand.isLeft) "Left hand" else "Right hand"
//            println(
//                "  " + handType + ", id: " + hand.id()
//                        + ", palm position: " + hand.palmPosition()
//            )


            // Get the hand's normal vector and direction
            val normal = hand.palmNormal()
            val direction = hand.direction()

            // Calculate the hand's pitch, roll, and yaw angles
//            println(
//                "  pitch: " + Math.toDegrees(direction.pitch().toDouble()) + " degrees, "
//                        + "roll: " + Math.toDegrees(normal.roll().toDouble()) + " degrees, "
//                        + "yaw: " + Math.toDegrees(direction.yaw().toDouble()) + " degrees"
//            )


            // Get arm bone
            val arm = hand.arm()
//            println(
//                "  Arm direction: " + arm.direction()
//                        + ", wrist position: " + arm.wristPosition()
//                        + ", elbow position: " + arm.elbowPosition()
//            )

            // Get fingers
            for (finger in hand.fingers()) {
//                println(
//                    "    " + finger.type() + ", id: " + finger.id()
//                            + ", length: " + finger.length()
//                            + "mm, width: " + finger.width() + "mm"
//                )


                //Get Bones
                for (boneType in Bone.Type.values()) {
                    val bone = finger.bone(boneType)
//                    println(
//                        "      " + bone.type()
//                                + " bone, start: " + bone.prevJoint()
//                                + ", end: " + bone.nextJoint()
//                                + ", direction: " + bone.direction()
//                    )

                }
            }
        }

        // Get tools
        for (tool in frame.tools()) {
//            println(
//                "  Tool id: " + tool.id()
//                        + ", position: " + tool.tipPosition()
//                        + ", direction: " + tool.direction()
//            )
        }

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

//                    if (lightStateSwipe == true ) {
//                        connection.getIO { output, _ ->
//                            output.write("off".toByteArray())
//                            lightStateSwipe = false
//                        }
//                    } else {
//                        connection.getIO { output, _ ->
//                            output.write("on".toByteArray())
//                            lightStateSwipe = true
//                        }
//                    }
                }

                Gesture.Type.TYPE_SCREEN_TAP -> {
                    println("タップ")
                    val screenTap = ScreenTapGesture(gesture)

                    connection.getIO { output, _ ->
                        output.write("on".toByteArray())
                    }
//                    var lightState = false

//                    if (lightState == true ) {
//                        connection.getIO { output, _ ->
//                            output.write("off".toByteArray())
//                            lightState = false;
//                            println("けした！")
//                        }
//                    } else {
//                        connection.getIO { output, _ ->
//                            output.write("on".toByteArray())
//                            lightState = true;
//                            println("ついた！")
//                        }
//                    }
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


fun main(args: Array<String>) {
    // Create a sample listener and controller
    val listener = ControlLight()
    val controller = Controller()

    // Have the sample listener receive events from the controller
    controller.addListener(listener)

    // Keep this process running until Enter is pressed
    println("Press Enter to quit... ")
    try {
        System.`in`.read()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    // Remove the sample listener when done
    controller.removeListener(listener)
}
