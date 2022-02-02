package info.benjaminhill.sketchy

import junit.framework.TestCase.assertTrue
import java.io.File

fun runDrawTest(wrappedDrawer: info.benjaminhill.sketchy.drawing.DrawingTechnique, sourceImageName: String) {
    val name = wrappedDrawer.javaClass.simpleName

    File("scriptgen/output_${name}_$sourceImageName.png").also {
        it.delete() // cleanup leftovers, but we want the output to stick around, so we can look at it
        wrappedDrawer.exportToImage(it)
        assertTrue(it.length() > 0)
        //Assertions.assertTrue(, "Output image file empty or missing: '${it.canonicalPath}'")
    }

    File("scriptgen/output_${name}_$sourceImageName.txt").also {
        it.delete()
        wrappedDrawer.exportToScript(it)
        assertTrue(it.length() > 0)
        //, "Output text file empty or missing: '${it.canonicalPath}'")
    }
}