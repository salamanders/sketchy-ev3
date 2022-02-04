package info.benjaminhill.scriptgen

import info.benjaminhill.scriptgen.ScaleFreeImage.Companion.toScaleFreeImage
import info.benjaminhill.utils.getFile
import java.io.File
import javax.imageio.ImageIO

/**
 * Load up an image, convert it to a scale free image, and (lazy) create and export a script
 */
abstract class DrawingTechnique(fileName: String) {
    protected val sfi: ScaleFreeImage = ImageIO.read(getFile(fileName)).toScaleFreeImage()

    private val script: Script by lazy {
        Script(points = generateScript()).removeDuplicates()
    }

    internal abstract fun generateScript(): List<NormalVector2D>

    fun exportToImage(outputFile: File) {
        ImageIO.write(script.toImage(), "png", outputFile)
    }

    fun exportToScript(outputFile: File) {
        outputFile.writeText(script.toText())
    }
}