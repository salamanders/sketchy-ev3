package info.benjaminhill.sketchy

import info.benjaminhill.sketchy.drawing.Hilbert

class HilbertTest {
    @org.junit.Test
    fun runHilbert() = runDrawTest(Hilbert(fileName = "../web/images/liberty.png", maxDepth = 7), "liberty")

}