package info.benjaminhill.scriptgen

class ScanTest {
    @org.junit.Test
    fun runImageToScan() = runDrawTest(ImageToScan(fileName = "../web/images/xwingl.png"), "liberty")

}