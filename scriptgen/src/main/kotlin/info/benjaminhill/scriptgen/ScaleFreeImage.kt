package info.benjaminhill.scriptgen


import info.benjaminhill.utils.NormalVector2D
import mu.KLoggable
import org.imgscalr.Scalr
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * A square scale free image of lum values
 * All access to the input and output images are done in a normalized manner (0.0 until 1.0)
 * Used for converting images into drawing commands.
 * Brighter = more ink (black background)
 */
class ScaleFreeImage
private constructor(
    private val inputImageInk: FloatArray,
) {
    private fun pointToIndex(point: NormalVector2D): Int {
        val x = (point.x * RESOLUTION).toInt()
        val y = (point.y * RESOLUTION).toInt()
        return y * RESOLUTION + x
    }

    /** Scale up by the image dimension then sample from the backing array */
    fun getInk(point: NormalVector2D): Float = inputImageInk[pointToIndex(point)]

    fun erase(p1: NormalVector2D, p2: NormalVector2D) = applyToPixelsOnLine(p1, p2) { inkIndex ->
        inputImageInk[inkIndex] = 0f
    }

    fun getInkRms(p1: NormalVector2D, p2: NormalVector2D): Double {
        var sum = 0.0
        var count = 0
        applyToPixelsOnLine(p1, p2) { inkIndex ->
            sum = (inputImageInk[inkIndex] * inputImageInk[inkIndex]).toDouble()
            count++
        }
        return sqrt(sum / count)
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
        val x1 = p1.x * RESOLUTION
        val y1 = p1.y * RESOLUTION
        val x2 = p2.x * RESOLUTION
        val y2 = p2.y * RESOLUTION
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

            if (iy < RESOLUTION && ix < RESOLUTION) {
                applyToEachPoint(iy * RESOLUTION + ix)
            }
        }
    }

    /** Render a script into a sample image, good for testing */
    fun toImage(): BufferedImage {
        val outputImage = BufferedImage(RESOLUTION, RESOLUTION, BufferedImage.TYPE_INT_RGB)
        for (x in 0 until RESOLUTION) {
            for (y in 0 until RESOLUTION) {
                outputImage.setRGB(x, y, Color.getHSBColor(.5f, 1f, inputImageInk[y * RESOLUTION + x]).rgb)
            }
        }
        return outputImage
    }

    companion object : KLoggable {
        override val logger = logger()

        const val RESOLUTION = 500

        /**
         * Converts from any reachable BufferedImage to a scale free image, backed by a fixed size float array.
         * Black is considered "ink" and is inverted to higher Lum.
         * Centers and squares the image by adding white padding prior to converting.
         */
        fun fileToScaleFree(location: String): ScaleFreeImage =
            locationToBufferedImage(location).toScaleFreeImage()

        /** Centers the image and scales it down uniformly to a RESOLUTION square */
        private fun BufferedImage.toScaleFreeImage(): ScaleFreeImage {
            val resizedBI = this.resize(RESOLUTION).pad(RESOLUTION / 2).centerCrop(RESOLUTION)

            val inputImageInk = FloatArray(resizedBI.width * resizedBI.height) { 0f }
            for (x in 0 until resizedBI.width) {
                for (y in 0 until resizedBI.height) {
                    inputImageInk[y * resizedBI.width + x] = 1 - resizedBI.getLum(x, y)
                }
            }
            return ScaleFreeImage(inputImageInk)
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

        private fun locationToBufferedImage(location: String) =
            ImageIO.read(File(location).toURI().toURL())!!

        fun BufferedImage.resize(
            targetSize: Int,
            method: Scalr.Method = Scalr.Method.ULTRA_QUALITY
        ): BufferedImage = Scalr.resize(this, method, targetSize)

        fun BufferedImage.pad(padding: Int, color: Color = Color.WHITE): BufferedImage =
            Scalr.pad(this, padding, color)

        fun BufferedImage.centerCrop(pixels: Int): BufferedImage =
            Scalr.crop(this, (this.width - pixels) / 2, (this.height - pixels) / 2, pixels, pixels)
    }
}

