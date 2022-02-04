package info.benjaminhill.scriptgen

class HilbertTest {
    @org.junit.Test
    fun runHilbert() = runDrawTest(Hilbert(fileName = "../web/images/liberty.png", maxDepth = 7), "liberty")

}