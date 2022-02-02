package info.benjaminhill.sketchy.ev3

import au.edu.federation.utils.Vec2f
import lejos.robotics.geometry.Rectangle2D
import mu.KotlinLogging
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import java.awt.Rectangle
import kotlin.math.cos
import kotlin.math.sin

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
