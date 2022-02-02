package info.benjaminhill.sketchy.brickpi

import mu.KotlinLogging
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.util.*

private val logger = KotlinLogging.logger {}


/** Finds the first brick on the LAN */
fun getBrickIPAddress(): String? {
    val maxDiscoveryTimeMs = 2000
    val maxPacketSize = 32
    val discoveryPort = 3016

    DatagramSocket().use { socket ->
        socket.broadcast = true
        NetworkInterface.getNetworkInterfaces().toList().filterNotNull().filterNot { it.isLoopback }.filter { it.isUp }
            .flatMap { it.interfaceAddresses }.filterNotNull().forEach { interfaceAddress ->
                interfaceAddress.broadcast?.let { broadcast ->
                    val message = "find * ${interfaceAddress.address.hostAddress} ${socket.localPort} 2"
                    val sendData = message.toByteArray()
                    val sendPacket = DatagramPacket(sendData, sendData.size, broadcast, discoveryPort)
                    socket.send(sendPacket)
                }
            }

        socket.soTimeout = maxDiscoveryTimeMs / 4
        val packet = DatagramPacket(ByteArray(maxPacketSize), maxPacketSize)
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < maxDiscoveryTimeMs) {
            try {
                socket.receive(packet)
                // val message = String(packet.data, Charset.defaultCharset()).trim { it <= ' ' }
                return packet.address.hostAddress
            } catch (e: SocketTimeoutException) {
                logger.warn { "No brick found." }
            }
        }
    }
    return null
}

/** Shorter round for the logs */
val Double.str: String
    get() = "%.3f".format(this)

/** Simple REPL keys to commands that always have 'q' to quit */
fun keyboardCommands() = sequence {
    Scanner(System.`in`).useDelimiter("\\s*")!!.use { sc ->
        while (sc.hasNext()) {
            val ch = sc.next()!!
            if (ch == "q") {
                break
            }
            yield(ch[0])
        }
    }
}

