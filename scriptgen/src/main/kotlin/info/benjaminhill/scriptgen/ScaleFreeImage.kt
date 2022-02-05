package info.benjaminhill.scriptgen


import info.benjaminhill.utils.NormalVector2D
import mu.KLoggable
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs

/**
 * A square scale free image of lum values
 * All access to the input and output images are done in a normalized manner (0.0 until 1.0)
 * Used for converting images into drawing commands.
 * Resolution is maintained from the original image.
 * @param inputDimension the max res (width or height) of input image
 */
class ScaleFreeImage
private constructor(
    private val inputDimension: Int,
    private val inputImageInk: FloatArray,
) {
    private fun pointToIndex(point: NormalVector2D): Int {
        val x = (point.x * inputDimension).toInt()
        val y = (point.y * inputDimension).toInt()
        return y * inputDimension + x
    }

    /** Scale up by the image dimension then sample from the backing array */
    fun getInk(point: NormalVector2D): Float = inputImageInk[pointToIndex(point)]

    fun whiteout(p1: NormalVector2D, p2: NormalVector2D) = applyToPixelsOnLine(p1, p2) { inkIndex ->
        inputImageInk[inkIndex] = 0f
    }

    fun getInkAvgSqs(p1: NormalVector2D, p2: NormalVector2D): Double {
        var sum = 0f
        var count = 0
        applyToPixelsOnLine(p1, p2) { inkIndex ->
            sum = inputImageInk[inkIndex]
            count++
        }
        return (sum / count).toDouble()
    }

    /**
     * Apply a function to each pixel that a line covers
     * Bresenham's algorithm to find all pixels on a Line2D. Assuming pixel grid is every unit.
     * @author nes
     * from https://snippets.siftie.com/nikoschwarz/iterate-all-points-on-a-line-using-bresenhams-algorithm/
     */
    private inline fun applyToPixelsOnLine(
        p1: NormalVector2D,
        p2: NormalVector2D,
        applyToEachPoint: (inkIndex: Int) -> Unit
    ) {
        val precision = 1.0
        val x1 = p1.x * inputDimension
        val y1 = p1.y * inputDimension
        val x2 = p2.x * inputDimension
        val y2 = p2.y * inputDimension
        val sx: Double = if (x1 < x2) precision else -1 * precision
        val sy: Double = if (y1 < y2) precision else -1 * precision
        val dx: Double = abs(x2 - x1)
        val dy: Double = abs(y2 - y1)

        var x: Double = x1
        var y: Double = y1
        var ix: Int
        var iy: Int
        var error: Double = dx - dy

        while (abs(x - x2) > 0.9 || abs(y - y2) > 0.9) {
            val e2 = 2 * error
            if (e2 > -dy) {
                error -= dy
                x += sx
            }
            if (e2 < dx) {
                error += dx
                y += sy
            }
            ix = x.toInt()
            iy = y.toInt()

            if (iy < inputDimension && ix < inputDimension) {
                applyToEachPoint(iy * inputDimension + ix)
            }
        }
    }

    companion object : KLoggable {
        override val logger = logger()

        fun fileToScaleFree(location: String): ScaleFreeImage =
            ImageIO.read(File(location).toURI().toURL())!!.toScaleFreeImage()

        /** Centers the image and scales it down uniformly to a 1x1 max */
        fun BufferedImage.toScaleFreeImage(): ScaleFreeImage {
            val inputDimension: Int

            val (xScoot, yScoot) = if (this.width > this.height) {
                inputDimension = this.width
                Pair(0, (this.width - this.height) / 2)
            } else {
                inputDimension = this.height
                Pair((this.height - this.width) / 2, 0)
            }
            val inputImageInk = FloatArray(inputDimension * inputDimension) { 0f }
            for (x in 0 until this.width) {
                for (y in 0 until this.height) {
                    val actualX = x + xScoot
                    val actualY = y + yScoot
                    inputImageInk[actualY * inputDimension + actualX] = 1 - this.getLum(x, y)
                }
            }
            return ScaleFreeImage(inputDimension, inputImageInk)
        }

        /** Grabs the lum of any point (scales RGB) from 0.0..1.0 */
        private fun BufferedImage.getLum(x: Int, y: Int): Float {
            require(x in 0 until width) { "x:$x outside of $width x $height" }
            require(y in 0 until height) { "y:$y outside of $width x $height" }
            val color = getRGB(x, y)
            val red = color.ushr(16) and 0xFF
            val green = color.ushr(8) and 0xFF
            val blue = color.ushr(0) and 0xFF
            return (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255
        }
    }
}

