package info.benjaminhill.scriptgen

import java.io.File

fun main() {
    val image = "falcon"
    val method = "particle"
    val outputFolder = File("scriptgen/output/").apply { mkdirs() }

    val inputImageLocation = "scriptgen/images/$image.png"
    val outputImageFile = outputFolder.resolve("${method}_${image}.png")
    val outputScriptFile = outputFolder.resolve("${method}_${image}.txt")
    val dt: DrawingTechnique = ParticleSquiggles(
        fileName = inputImageLocation,
    )

    dt.exportToImage(outputImageFile)
    dt.exportToScript(outputScriptFile)
}

