package info.benjaminhill.sketchy

import info.benjaminhill.sketchy.drawing.ImageToScan

class ScanTest {
    @org.junit.Test
    fun runImageToScan() = runDrawTest(ImageToScan(fileName = "../web/images/xwingl.png"), "liberty")

}