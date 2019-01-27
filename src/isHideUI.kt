import java.io.IOException
import java.lang.Math
import com.leapmotion.leap.*
import com.leapmotion.leap.Gesture.State

class SampleKtListener : Listener() {
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
        println(
            "Frame id: " + frame.id()
                    + ", timestamp: " + frame.timestamp()
                    + ", hands: " + frame.hands().count()
                    + ", fingers: " + frame.fingers().count()
                    + ", tools: " + frame.tools().count()
                    + ", gestures " + frame.gestures().count()
        )

        //Get hands
        for (hand in frame.hands()) {
            val handType = if (hand.isLeft) "Left hand" else "Right hand"
            println(
                "  " + handType + ", id: " + hand.id()
                        + ", palm position: " + hand.palmPosition()
            )


            // Get the hand's normal vector and direction
            val normal = hand.palmNormal()
            val direction = hand.direction()

            // Calculate the hand's pitch, roll, and yaw angles
            println(
                "  pitch: " + Math.toDegrees(direction.pitch().toDouble()) + " degrees, "
                        + "roll: " + Math.toDegrees(normal.roll().toDouble()) + " degrees, "
                        + "yaw: " + Math.toDegrees(direction.yaw().toDouble()) + " degrees"
            )


            // Get arm bone
            val arm = hand.arm()
            println(
                "  Arm direction: " + arm.direction()
                        + ", wrist position: " + arm.wristPosition()
                        + ", elbow position: " + arm.elbowPosition()
            )

            // Get fingers
            for (finger in hand.fingers()) {
                println(
                    "    " + finger.type() + ", id: " + finger.id()
                            + ", length: " + finger.length()
                            + "mm, width: " + finger.width() + "mm"
                )


                //Get Bones
                for (boneType in Bone.Type.values()) {
                    val bone = finger.bone(boneType)
                    println(
                        "      " + bone.type()
                                + " bone, start: " + bone.prevJoint()
                                + ", end: " + bone.nextJoint()
                                + ", direction: " + bone.direction()
                    )

                }
            }
        }

        // Get tools
        for (tool in frame.tools()) {
            println(
                "  Tool id: " + tool.id()
                        + ", position: " + tool.tipPosition()
                        + ", direction: " + tool.direction()
            )
        }

        val gestures = frame.gestures()
        for (i in 0 until gestures.count()) {
            val gesture = gestures.get(i)

            when (gesture.type()) {
                Gesture.Type.TYPE_CIRCLE -> {
                    val circle = CircleGesture(gesture)

                    // Calculate clock direction using the angle between circle normal and pointable
                    val clockwiseness: String =
                        if (circle.pointable().direction().angleTo(circle.normal()) <= Math.PI / 2) {
                            // Clockwise if angle is less than 90 degrees
                            "clockwise"
                        } else {
                            "counterclockwise"
                        }

                    // Calculate angle swept since last frame
                    var sweptAngle = 0.0
                    if (circle.state() != State.STATE_START) {
                        val previousUpdate = CircleGesture(controller.frame(1).gesture(circle.id()))
                        sweptAngle = (circle.progress() - previousUpdate.progress()).toDouble() * 2.0 * Math.PI
                    }

//                    println(
//                        "  Circle id: " + circle.id()
//                                + ", " + circle.state()
//                                + ", progress: " + circle.progress()
//                                + ", radius: " + circle.radius()
//                                + ", angle: " + Math.toDegrees(sweptAngle)
//                                + ", " + clockwiseness
//                    )
                }

                Gesture.Type.TYPE_SWIPE -> {
                    val swipe = SwipeGesture(gesture)
//                    println(
//                        "  Swipe id: " + swipe.id()
//                                + ", " + swipe.state()
//                                + ", position: " + swipe.position()
//                                + ", direction: " + swipe.direction()
//                                + ", speed: " + swipe.speed()
//                    )
                    println(
                        "swipeしたよ"
                    )
                    val runtime = Runtime.getRuntime()
                    val args = arrayOf("osascript", "-e", "tell app \"iTunes\" to play")
                    val process = runtime.exec(args)
                }

                Gesture.Type.TYPE_SCREEN_TAP -> {
                    val screenTap = ScreenTapGesture(gesture)
//                    println(
//                        "  Screen Tap id: " + screenTap.id()
//                                + ", " + screenTap.state()
//                                + ", position: " + screenTap.position()
//                                + ", direction: " + screenTap.direction()
//                    )
                }

                Gesture.Type.TYPE_KEY_TAP -> {
                    val keyTap = KeyTapGesture(gesture)
//                    println(
//                        "  Key Tap id: " + keyTap.id()
//                                + ", " + keyTap.state()
//                                + ", position: " + keyTap.position()
//                                + ", direction: " + keyTap.direction()
//                    )
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
    val listener = SampleKtListener()
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
