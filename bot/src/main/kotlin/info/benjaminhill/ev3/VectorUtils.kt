package info.benjaminhill.ev3

import lejos.robotics.geometry.Rectangle2D
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import java.awt.Rectangle
import kotlin.math.cos
import kotlin.math.sin
import au.edu.federation.utils.Vec2f
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import mu.KotlinLogging
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import java.io.InputStreamReader
import java.net.*
import java.util.*
import java.util.concurrent.Executors
import java.util.zip.GZIPInputStream

private val logger = KotlinLogging.logger {}

operator fun Vector2D.component1(): Double = this.x
operator fun Vector2D.component2(): Double = this.y

fun angleToVector2D(rad: Double) = Vector2D(cos(rad), sin(rad))

fun Rectangle.contains(v: Vector2D): Boolean = contains(v.x.toInt(), v.y.toInt())

internal fun Vec2f.scaleUnitTo(drawingArea: Rectangle2D): Vec2f {
    require(x in 0f..1f) { "Unit Vec2f had out of bounds x:$x" }
    require(y in 0f..1f) { "Unit Vec2f had out of bounds y:$y" }
    val scaledX = drawingArea.x + (drawingArea.width * x)
    val scaledY = drawingArea.y + (drawingArea.height * y)
    return Vec2f(scaledX.toFloat(), scaledY.toFloat())
}

/**
 * Bresenham's algorithm to find all pixels on a Line2D.
 * @author nes
 * from https://snippets.siftie.com/nikoschwarz/iterate-all-points-on-a-line-using-bresenhams-algorithm/
 */

fun Line2D.points(): List<Point2D> {
    val precision = 1.0
    val sx: Double = if (x1 < x2) precision else -1 * precision
    val sy: Double = if (y1 < y2) precision else -1 * precision
    val dx: Double = Math.abs(x2 - x1)
    val dy: Double = Math.abs(y2 - y1)

    var x: Double = x1
    var y: Double = y1
    var error: Double = dx - dy

    val result = mutableListOf<Point2D>()

    while (Math.abs(x - x2) > 0.9 || Math.abs(y - y2) > 0.9) {
        val ret = Point2D.Double(x, y)

        val e2 = 2 * error
        if (e2 > -dy) {
            error -= dy
            x += sx
        }
        if (e2 < dx) {
            error += dx
            y += sy
        }

        result.add(ret)
    }
    return result
}
