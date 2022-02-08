package info.benjaminhill.scriptgen

import java.io.File

fun main() {
    val image = "xwingl"
    val outputFolder = File("scriptgen/")

    println("Scripting an image.")
    val algo: DrawingTechnique = ImageToStrokes(
        fileName = "scriptgen/images/$image.png",
        strokes = 8_000,
        searchSteps = 25_000,
    )
    val name = "strokes"

    outputFolder.mkdirs()
    algo.exportToImage(outputFolder.resolve("output_${name}_$image.png"))
    algo.exportToScript(outputFolder.resolve("output_${name}_$image.txt"))
}

