package info.benjaminhill.sketchy

import info.benjaminhill.sketchy.drawing.ImageToSpiral

class SpiralTest {
    @org.junit.Test
    fun runImageToSpiral() = runDrawTest(ImageToSpiral(fileName = "../web/images/liberty.png", numberOfSpins = 100), "liberty")

}