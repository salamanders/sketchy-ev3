package info.benjaminhill.inbound

import info.benjaminhill.utils.printDeepStackTrace
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.runBlocking
import lejos.robotics.geometry.Point2D
import kotlin.time.ExperimentalTime


private const val LISTEN_FOR_SCRIPT = false

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) = try {
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


