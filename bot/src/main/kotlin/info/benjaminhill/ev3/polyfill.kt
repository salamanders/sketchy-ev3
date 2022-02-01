
import lejos.robotics.geometry.Rectangle2D
import mu.KotlinLogging
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

private val logger = KotlinLogging.logger {}

fun URL.readTextSupportGZIP(): String {
    try {
        val con = openConnection() as HttpURLConnection
        con.setRequestProperty("Accept-Encoding", "gzip")
        if ("gzip" == con.contentEncoding) {
            logger.debug { "Able to read GZIP content" }
            InputStreamReader(GZIPInputStream(con.inputStream))
        } else {
            logger.debug { "No GZIP, fallback to plain content" }
            InputStreamReader(con.inputStream)
        }.use { reader ->
            return reader.readText()
        }
    } catch (e: Exception) {
        logger.error { e.toString() }
        return readText()
    }
}

val Rectangle2D.str
    get() = "{x:$x, y:$y, w:$width, h:$height}"

/** Shorter round for the logs */
val Double.str: String
    get() = "%.4f".format(this)
