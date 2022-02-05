package info.benjaminhill.scriptgen

import info.benjaminhill.utils.NormalVector2D
import mu.KLoggable
import org.apache.commons.math3.geometry.euclidean.twod.Line
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import org.w3c.dom.Element
import java.awt.BasicStroke
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Wrapper around a list of points.
 * Can simplify the steps for easier drawing, produce a guess of what the bot will draw, and has a variety of output formats.
 */
open class Script(
    private val points: List<NormalVector2D>
) {
    /** Render a script into a sample image, good for testing */
    fun toImage(): BufferedImage {
        val outputImageRes = 2_000

        /** A sample rendering */
        val outputImage = BufferedImage(outputImageRes, outputImageRes, BufferedImage.TYPE_INT_RGB)
        val outputG2d = outputImage.createGraphics()!!.apply {
            color = Color.WHITE
            fillRect(0, 0, outputImage.width, outputImage.height)
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            color = Color.BLACK
            // a few mm wide pen?
            stroke = BasicStroke((1f / 1000) * outputImageRes)
        }

        // TODO: Needs a better way to tell if a point matters or not.
        // distinctBy {(it.x / plotterResolution).roundToInt() to (it.y / plotterResolution).roundToInt() }
        this.points.map {
            Vector2D(it.x, it.y).scalarMultiply(outputImageRes.toDouble())
        }.zipWithNext().forEach { (a, b) ->
            check(a.x.roundToInt() in 0..outputImageRes)
            check(a.y.roundToInt() in 0..outputImageRes)
            outputG2d.drawLine(a.x.roundToInt(), a.y.roundToInt(), b.x.roundToInt(), b.y.roundToInt())
        }

        outputG2d.dispose()
        return outputImage
    }

    /** Render a script into a plain text chunk, good for exporting */
    fun toText() = this.points.joinToString(",\n") { it.toString() }

    fun removeDuplicates(): Script = Script(
        points = this.points.filterIndexed { index, vector2D ->
            if (index == 0) {
                true
            } else {
                vector2D != this.points[index - 1]
            }
        }
    )

    fun ramerDouglasPeucker(maxSize: Int = 1_000): Script {
        logger.info { "ramerDouglasPeucker from ${points.size} to $maxSize" }
        var maxDelta = 0.00001
        var iterations = 0
        val result = points.toMutableList()
        result.addAll(points)
        while (result.size > maxSize) {
            iterations++
            result.clear()
            result.addAll(ramerDouglasPeuckerRecursion(points, maxDelta))
            maxDelta *= 1.05
        }
        logger.info { "maxDelta of $maxDelta is under size cap $maxSize after $iterations passes." }
        return Script(points = result)
    }

    /** Iteratively delete smallest 3-point triangles.  Could do lots of caching, but meh. */
    fun visvalingamWhyatt(maxSize: Int = 1_000): Script {
        val result = points.toMutableList()
        while (result.size > maxSize) {
            if (result.size % 1000 == 0) {
                logger.info { "visvalingamWhyatt ${result.size}" }
            }
            val triangles = result.windowed(3).map { Triple(it[0], it[1], it[2]) }.map {
                it to abs(it.first.x * (it.second.y - it.third.y) + it.second.x * (it.third.y - it.first.y) + it.third.x * (it.first.y - it.second.y)) / 2.0
            }
            val minTriArea = triangles.minByOrNull { it.second }!!
            val idx = triangles.indexOf(minTriArea)
            result.removeAt(idx + 1)
        }
        return Script(points = result)
    }


    companion object : KLoggable {
        override val logger = logger()

        /** Hacky parse of a SVG, like what StippleGen2 produces */
        fun fileToPath(svgXmlFile: File): List<Vector2D> {
            assert(svgXmlFile.canRead())
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgXmlFile)!!
            val pathElement = doc.getElementsByTagName("path")!!.item(0)!! as Element
            val pen = Scanner(pathElement.getAttribute("d"))
            pen.next() // discard the first character
            val points = mutableListOf<Vector2D>()
            while (pen.hasNext()) {
                points.add(Vector2D(pen.nextDouble(), pen.nextDouble()))
            }
            return points
        }

        /**
         * Delete any midpoints that aren't "interesting enough" to fall outside the error bar
         * Recursively subdivides
         * https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
         */
        private fun ramerDouglasPeuckerRecursion(
            points: List<NormalVector2D>,
            maxDistanceAllowed: Double
        ): List<NormalVector2D> {
            if (points.size <= 2) {
                return points
            }

            val line = Line(points.first(), points.last(), 1.0E-10)
            val distances = points.mapIndexed { index, nvector2D ->
                index to nvector2D
            }.filter { (index, _) ->
                index != 0 && index != points.size - 1
            }.map { (index, vector2D) ->
                index to line.distance(vector2D)
            }

            val (maxIdx, maxDist) = distances.maxByOrNull { (_, distance) -> distance }!!
            return if (maxDist < maxDistanceAllowed) {
                listOf(points.first(), points.last())
            } else {
                val leftSide = ramerDouglasPeuckerRecursion(points.subList(0, maxIdx + 1), maxDistanceAllowed)
                val rightSide = ramerDouglasPeuckerRecursion(points.subList(maxIdx, points.size), maxDistanceAllowed)
                leftSide.plus(rightSide.drop(1))
            }
        }
    }
}













