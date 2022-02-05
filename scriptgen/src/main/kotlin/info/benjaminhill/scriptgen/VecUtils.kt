package info.benjaminhill.scriptgen

import au.edu.federation.utils.Vec2f
import mu.KotlinLogging
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import java.awt.Rectangle
import java.awt.geom.Rectangle2D
import kotlin.math.cos
import kotlin.math.sin

private val logger = KotlinLogging.logger {}

operator fun Vector2D.component1(): Double = this.x
operator fun Vector2D.component2(): Double = this.y

fun angleToVector2D(rad: Double) = Vector2D(cos(rad), sin(rad))

fun Rectangle.contains(v: Vector2D): Boolean = contains(v.x.toInt(), v.y.toInt())

