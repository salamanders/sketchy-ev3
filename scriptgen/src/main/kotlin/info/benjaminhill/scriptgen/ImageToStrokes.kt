package info.benjaminhill.scriptgen

import info.benjaminhill.utils.LogInfrequently
import info.benjaminhill.utils.NormalVector2D
import info.benjaminhill.utils.NormalVector2D.Companion.normalOrNull
import info.benjaminhill.utils.r
import kotlinx.coroutines.*
import mu.KLoggable
import org.apache.commons.math4.geometry.euclidean.twod.Vector2D
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

/**
 * Decimate an image by drawing white lines over it.
 * Each white line is the "most beneficial" next step (based on dark luminosity removed)
 * Is input image scale dependent
 * Can be trapped in a local minimum, but that is ok.
 */
class ImageToStrokes(
    fileName: String,
    private val strokes: Int = 2_000,
    private val searchSteps: Int = 5_000,
    private val maxPctHop: Double = 0.3,
    private val minPctHop: Double = 0.0001,
) : DrawingTechnique(fileName) {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun generateScript(): List<NormalVector2D> = runBlocking(Dispatchers.IO) {
        val points = mutableListOf(NormalVector2D(.5, .5))
        val runSpeedLogger = LogInfrequently()
        val ema = ExponentialMovingAverage(alpha = 0.05)
        for (i in 0..strokes) {
            runSpeedLogger.hit()
            val currentLocation = points.last()

            val launched: List<Deferred<Pair<NormalVector2D, Double>?>> =
                (0..searchSteps).map { async { getRandomLocation(currentLocation) } }
            val allPossibleNext: List<Pair<NormalVector2D, Double>> = launched.awaitAll().filterNotNull()

            if (allPossibleNext.size.toDouble() / searchSteps < .2) {
                logger.warn { "Excluded too many possible next steps: ${allPossibleNext.size}" }
            }
            if (allPossibleNext.isEmpty()) {
                logger.warn { "Had a fully empty possible next step list, likely a bug." }
            }
            val (bestPt: NormalVector2D, bestLineScore) = allPossibleNext.maxByOrNull { it.second }!!
            val emaNext = ema.average(bestLineScore)
            if (i % 250 == 0) {
                logger.info { "$i of $strokes, best score=${bestLineScore.r}, ema=${emaNext.r}" }
            }

            // White-out the current move
            sfi.erase(currentLocation, bestPt)
            points.add(bestPt)
        }
        return@runBlocking points
    }


    private fun getRandomLocation(origin: NormalVector2D): Pair<NormalVector2D, Double>? {
        // Gaussian random hops.  This would be a good swarm-optimizer!
        val r1 = ThreadLocalRandom.current().nextGaussian() * maxPctHop
        val r2 = ThreadLocalRandom.current().nextGaussian() * maxPctHop
        if (abs(r1) + abs(r2) < minPctHop) {
            //LOG.info { "Very small hop, bailing. $r1 $r2"}
            return null
        }
        val nextStroke = Vector2D(r1, r2)
        val potentialNextPoint: NormalVector2D = origin.add(nextStroke).normalOrNull() ?: return null

        // too short a hop
        if (origin.distance(potentialNextPoint) < minPctHop) {
            //LOG.info { "Too small a hop, not great. (${origin.distance(potentialNextPoint)})" }
            return null
        }
        val avgInk = sfi.getInkRms(origin, potentialNextPoint)
        return Pair(potentialNextPoint, avgInk)
    }

    companion object : KLoggable {
        override val logger = logger()
    }
}

