package info.benjaminhill.sketchy

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import lejos.hardware.motor.BaseRegulatedMotor
import lejos.hardware.port.SensorPort
import lejos.hardware.sensor.EV3TouchSensor
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() {
    println("Hello, World")
    TwoMotorArm().use { tmd ->
        tmd.pressButtonsUntilPose(tmd.shoulder, tmd.elbow)
    }
    println("Goodbye!")

    // TODO
    /*
        RemoteControl("https://whiteboardbot.firebaseapp.com/config.json").use {
        it.run()
        LOG.info { "Finished Script" }
    }
       logger.info { "Hello kev3bot!" }
    if (args.isNotEmpty()) {
        logger.info { "args: ${args.joinToString()}" }
    }

    DrawingArms().use { arms ->
        // arms.calibrate()

        // center, then trace the boundaries
        arms.moveTo(.5f, .5f)
        arms.debugSVG()
        arms.debugLog()
        //arms.moveTo(0f, 0f)
        //arms.moveTo(0f, 1f)
        //arms.moveTo(1f, 1f)
        //arms.moveTo(1f, 0f)

        if (LISTEN_FOR_SCRIPT) {
            runBlocking {
                getLatestScript().collectIndexed { _, point: Point2D ->
                    try {
                        arms.moveTo(x = point.x.toFloat(), y = point.y.toFloat())
                    } catch (e: IllegalArgumentException) {
                        logger.warn { "Ignoring illegal point: $point" }
                    }
                }
            }
            logger.error { "Should never be done with script! How did we get here?" }
        } else {
            logger.info { "Skipping remote script, all done." }
        }
    }
} catch (t: Throwable) {
    logger.error { "Caught top level exception" }
    t.printDeepStackTrace()
}
     */
}


@FlowPreview
fun button(): Flow<Boolean> = flow {
    val sensor = EV3TouchSensor(SensorPort.S2)
    val sampleSize = sensor.sampleSize()
    val samples = FloatArray(sampleSize)
    while (true) {
        sensor.fetchSample(samples, 0)
        emit(samples[0] > .5f)
        delay(100)
    }
}.debounce(200)
    .conflate()
    .distinctUntilChanged()

suspend fun Flow<Boolean>.waitForPress() {
    filter { it }.first()
}

@FlowPreview
suspend fun getRanges(
    leftShoulder: BaseRegulatedMotor,
    rightShoulder: BaseRegulatedMotor
): Pair<ClosedFloatingPointRange<Float>, ClosedFloatingPointRange<Float>> {
    println("SPIN LEFT!  Move left-shoulder to 45° down-left, right-shoulder to 45° up-left, then click.")
    button().waitForPress()
    val leftMin = leftShoulder.position
    val rightMin = rightShoulder.position
    println("SPIN RIGHT! Move left-shoulder to 45° up-right, right-shoulder to 45° down-right, then click.")
    button().waitForPress()
    val leftMax = leftShoulder.position
    val rightMax = rightShoulder.position

    return Pair(leftMin..leftMax, rightMin..rightMax)
}

