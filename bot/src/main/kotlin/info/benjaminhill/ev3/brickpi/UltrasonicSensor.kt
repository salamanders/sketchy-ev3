package info.benjaminhill.ev3.brickpi

import java.io.File


open class UltrasonicSensor(port: Port) : Sensor(portToDir(port, ROOT_DIR)) {
    companion object {
        val ROOT_DIR = File("TODO")
    }
}
