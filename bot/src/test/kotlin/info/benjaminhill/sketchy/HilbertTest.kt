package info.benjaminhill.sketchy

import org.junit.jupiter.api.Test

class HilbertTest {
    @Test
    fun runHilbert() = runDrawTest(Hilbert(fileName = "images/liberty.png", maxDepth = 7), "liberty")

}