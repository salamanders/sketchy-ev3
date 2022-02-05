package info.benjaminhill.scriptgen

import info.benjaminhill.utils.NormalVector2D
import info.benjaminhill.utils.NormalVector2D.Companion.normalOrNull
import info.benjaminhill.utils.mapConcurrently
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mu.KLoggable
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
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

    override fun generateScript(): List<NormalVector2D> = runBlocking(Dispatchers.Default) {
        // Seed at center
        val points = mutableListOf(NormalVector2D(.5, .5))
        for (i in 0..strokes) {
            if (i % 250 == 0) {
                logger.info { "$i of $strokes" }
            }
            val currentLocation = points.last()
            val allPossibleNext: List<Pair<NormalVector2D, Double>> = (0..searchSteps).toList()
                .asFlow().mapConcurrently {
                    getRandomLocation(currentLocation)
                }.filterNotNull().toList()

            if (allPossibleNext.size.toDouble() / searchSteps < .1) {
                logger.warn { "Excluded too many possible next steps: ${allPossibleNext.size}" }
            }
            if (allPossibleNext.isEmpty()) {
                logger.warn { "Had a fully empty possible next step list, likely a bug." }
            }
            val (bestPt: NormalVector2D, _) = allPossibleNext.maxByOrNull { it.second }!!
            // White-out the current move
            sfi.whiteout(currentLocation, bestPt)
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
        val avgInk = sfi.getInkAvgSqs(origin, potentialNextPoint)
        return Pair(potentialNextPoint, avgInk)
    }

    companion object : KLoggable {
        override val logger = logger()
    }
}

