package info.benjaminhill.scriptgen

import info.benjaminhill.utils.NormalVector2D
import info.benjaminhill.utils.NormalVector2D.Companion.normalOrNull
import org.apache.commons.math4.geometry.euclidean.twod.Vector2D
import kotlin.math.cos
import kotlin.math.sin


internal class ExponentialMovingAverage(private val alpha: Double = .1) {
    private var oldValue: Double? = null
    fun average(value: Double): Double {
        if (oldValue == null) {
            oldValue = value
            return value
        }
        val newValue = oldValue!! + alpha * (value - oldValue!!)
        oldValue = newValue
        return newValue
    }
}

fun NormalVector2D.addDirection(distance:Double, angle:Double):NormalVector2D? =
    this.add(distance, Vector2D(cos(angle), sin(angle))).normalOrNull()