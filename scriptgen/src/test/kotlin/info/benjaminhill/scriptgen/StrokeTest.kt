package info.benjaminhill.scriptgen

class StrokeTest {
    @org.junit.Test
    fun runImageToStrokesLiberty() = runDrawTest(ImageToStrokes(fileName = "../web/images/liberty.png"), "liberty")

    @org.junit.Test
    fun runImageToStrokesSW() = runDrawTest(ImageToStrokes(fileName = "../web/images/sw.png"), "sw")

    @org.junit.Test
    fun runImageToStrokesShark() = runDrawTest(ImageToStrokes(fileName = "../web/images/shark.png"), "shark")
}