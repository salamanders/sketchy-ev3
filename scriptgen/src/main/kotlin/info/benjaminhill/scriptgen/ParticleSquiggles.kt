package info.benjaminhill.scriptgen

import info.benjaminhill.stats.pso.OptimizableFunction
import info.benjaminhill.stats.pso.PSOSwarm
import info.benjaminhill.utils.LogInfrequently
import info.benjaminhill.utils.NormalVector2D
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import mu.KLoggable

/**
 * Decimate an image by drawing white lines over it.
 * Each white line is the "most beneficial" next step (based on dark luminosity removed)
 * Is input image scale dependent
 * Can be trapped in a local minimum, but that is ok.
 */
class ParticleSquiggles(
    fileName: String
) : DrawingTechnique(fileName) {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun generateScript(): List<NormalVector2D> = runBlocking(Dispatchers.IO.limitedParallelism(8)) {
        val points: MutableList<NormalVector2D> = mutableListOf()
        points.add(NormalVector2D(.5, .5)) // starting point
        // val howEachStrokeIsDoing = ExponentialMovingAverage(alpha=0.05)
        val timedLogger = LogInfrequently()
        val pixelNormalSize = 1.0 / ScaleFreeImage.RESOLUTION

        val psoDirection = PSOSwarm(
            function = OptimizableFunction(
                parameterBounds = arrayOf(
                    (0.0).rangeTo(Math.PI * 2), // angle
                    (3 * pixelNormalSize).rangeTo(15 * pixelNormalSize) // hopSize
                )
            ) { (angle, hopSize) ->
                points.last().let { previousLocation->
                    val nextPoint = previousLocation.addDirection(hopSize, angle) ?: return@OptimizableFunction 1000.0
                    // Try to minimize, so opposite of ink
                    -1 * sfi.getInkRms(previousLocation, nextPoint)
                }
            },
            numOfParticles = 10,
            parallelism = 8,
        )

        for (i in 0..5_000) {
            timedLogger.hit()
            /*
            val psoOffset = PSOSwarm(function = OptimizableFunction(
                // TODO small delta hop from current location?
                parameterBounds = arrayOf(
                    (-0.3).rangeTo(0.3),
                    (-0.3).rangeTo(0.3)
                )
            ) { delta ->
                val nextNormalPoint =
                    NormalVector2D.normalOrNull(currentLocation.x + delta.x, currentLocation.y + delta.y)

                if (nextNormalPoint != null) {
                    val distSq = currentLocation.distanceSq(nextNormalPoint)
                    if (distSq > 0.0) {
                        val pushAway = (1 / ScaleFreeImage.RESOLUTION) / distSq
                        // Try to minimize, so opposite of ink
                        pushAway + -1 * sfi.getInkRms(currentLocation, nextNormalPoint)
                    } else {
                        1000.0
                    }
                } else {
                    1000.0
                }
            })
           */
            psoDirection.run()
            val (angle, hopSize) = psoDirection.getBest()
            val nextLocation = points.last().addDirection(hopSize, angle)!!
            // howEachStrokeIsDoing.average(pso.globalLeastError)
            if (i % 250 == 0) {
                logger.info { "$i of 5_000" }
            }
            // White-out the current move
            sfi.erase(points.last(), nextLocation)
            points.add(nextLocation)
        }
        return@runBlocking points
    }


    companion object : KLoggable {
        override val logger = logger()
    }
}

