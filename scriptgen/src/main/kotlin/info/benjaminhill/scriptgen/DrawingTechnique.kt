package info.benjaminhill.scriptgen

import info.benjaminhill.scriptgen.ScaleFreeImage.Companion.fileToScaleFree
import info.benjaminhill.utils.NormalVector2D
import java.io.File
import javax.imageio.ImageIO

/**
 * Load up an image, convert it to a scale free image, and (lazy) create and export a script
 */
abstract class DrawingTechnique(fileName: String) {
    protected val sfi: ScaleFreeImage = fileToScaleFree(fileName)

    private val script: Script by lazy {
        Script(points = generateScript()).removeDuplicates()
    }

    internal abstract fun generateScript(): List<NormalVector2D>

    fun exportToImage(outputFile: File) {
        ImageIO.write(script.toImage(sfi.toImage()), "png", outputFile)
    }

    fun exportToScript(outputFile: File) {
        outputFile.writeText(script.toText())
    }
}