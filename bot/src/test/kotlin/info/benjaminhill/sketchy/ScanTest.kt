package info.benjaminhill.sketchy

import org.junit.jupiter.api.Test

class ScanTest {
    @Test
    fun runImageToScan() = runDrawTest(ImageToScan(fileName = "images/xwingl.png"), "liberty")

}