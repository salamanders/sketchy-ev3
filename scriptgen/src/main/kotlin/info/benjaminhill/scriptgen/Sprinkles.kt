package info.benjaminhill.scriptgen

import info.benjaminhill.scriptgen.ScaleFreeImage.Companion.centerCrop
import info.benjaminhill.scriptgen.ScaleFreeImage.Companion.pad
import info.benjaminhill.scriptgen.ScaleFreeImage.Companion.resize
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.Raster
import java.io.File
import java.util.concurrent.ThreadLocalRandom
import javax.imageio.ImageIO
import kotlin.math.max


class Sprinkles(sourceImage: BufferedImage) {
    private val grayImage: BufferedImage

    init {
        val resizedBI = sourceImage
            .resize(ScaleFreeImage.RESOLUTION)
            .pad(ScaleFreeImage.RESOLUTION / 2)
            .centerCrop(ScaleFreeImage.RESOLUTION)
        grayImage = BufferedImage(
            resizedBI.width, resizedBI.height,
            BufferedImage.TYPE_BYTE_GRAY
        )
        grayImage.graphics.apply {
            drawImage(resizedBI, 0, 0, null)
            dispose()
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun sprinkle() {
        val maxRes = max(grayImage.width, grayImage.height)
        val sprinkleLength = 15
        val iterations = 50_000

        val outputImage = BufferedImage(
            grayImage.width, grayImage.height,
            BufferedImage.TYPE_BYTE_GRAY
        )
        val outputG2d = outputImage.createGraphics().apply {
            color = Color(255, 255, 255, 50)
            stroke = BasicStroke(.005f * maxRes)
        }

        val dummyRaster: Raster = grayImage.getData(
            Rectangle(
                0,
                0,
                sprinkleLength,
                sprinkleLength
            )
        )
        // TODO: Figure out https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html#thread-local-data
        val threadLocalTile: ThreadLocal<Pair<BufferedImage, Graphics2D>> = ThreadLocal.withInitial {
            val bi = BufferedImage(
                grayImage.colorModel,
                dummyRaster.createCompatibleWritableRaster(),
                grayImage.colorModel.isAlphaPremultiplied,
                null
            )
            bi to bi.createGraphics()!!.apply {
                color = Color(255, 255, 255, 50)
                stroke = BasicStroke(.005f * maxRes)
            }

        }
        // Is there enough ink to care? (skip if all white)
        (0..iterations).mapNotNull {
            val leftX = ThreadLocalRandom.current().nextInt(0, maxRes - sprinkleLength)
            val upperY = ThreadLocalRandom.current().nextInt(0, maxRes - sprinkleLength)
            // Copied section
            val rasterUnderTile = grayImage.getData(
                Rectangle(
                    leftX,
                    upperY,
                    sprinkleLength,
                    sprinkleLength
                )
            )
            // Is there enough ink to care? (skip if all white)
            if ((rasterUnderTile.dataBuffer as DataBufferByte).data.toUByteArray().average() < 240u) {
                Triple(leftX, upperY, rasterUnderTile)
            } else {
                null
            }
        }.forEach { (x: Int, y: Int, rasterUnderTile: Raster) ->
            // initialize the local image with the tile that passed the test
            val (writableTile: BufferedImage, graphics: Graphics2D) = threadLocalTile.get()
            writableTile.data = rasterUnderTile
            for (deg in 0 until 360 step 15) {
                // draw a line
                // Test if it is the best line
                // Draw the best line on the parent gray image
                /*
                *
                                val darkestTile: Pair<Int, UByte>? = listOf(
                    Pair(x, y - sprinkleLength),
                    Pair(x - sprinkleLength, y - sprinkleLength),
                    Pair(x - sprinkleLength, y),
                    Pair(x, y),
                ).mapIndexed { idx, upperLeft ->
                    (idx + 1) to (
                            )
                        .dataBuffer as DataBufferByte).data.toUByteArray().average()
                }.minByOrNull { it.second }

                if (darkestTile != null && darkestTile.second < lightness) {
                    val angle = ThreadLocalRandom.current().nextDouble(Math.PI / 2) * darkestTile.first
                    outputG2d.drawLine(x, y, x + (cos(angle) * 15).toInt(), y + (sin(angle) * 15).toInt())
                }
                * */
            }
        }
        outputG2d.dispose()
        ImageIO.write(outputImage, "png", File("output_sprinkle.png"))
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun UByteArray.average(): UByte {
    var sum: ULong = 0u
    var count: UInt = 0u
    for (element in this) {
        sum += element.toULong()
        ++count
    }
    return (sum / count).toUByte()
}

