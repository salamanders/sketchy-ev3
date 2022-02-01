package info.benjaminhill.ev3

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.measureTime

import kotlinx.coroutines.runBlocking
import lejos.hardware.Button
import lejos.hardware.lcd.LCD
import lejos.hardware.motor.EV3LargeRegulatedMotor
import lejos.hardware.motor.EV3MediumRegulatedMotor
import lejos.hardware.port.MotorPort
import lejos.robotics.RegulatedMotor
import mu.KLoggable
import mu.KotlinLogging
import kotlin.time.ExperimentalTime


/**
 * Convenience wrapper for the EV3 motors
 * A and D are large motors, B and C are medium.
 * Lazy: Won't attempt to connect to a motor until needed.
 * Once connected, will make a best-effort (Shutdown hook, and AutoCloseable) to gracefully close the motor port.
 * To use: `private val shoulder: RegulatedMotor by motorADelegate`
 */
open class Motors : AutoCloseable {
    init {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = runBlocking {
                close()
            }
        })
        logger.info { "MotorWrapper shutdown hook registered." }
    }

    protected val motorADelegate: Lazy<RegulatedMotor> = lazy {
        EV3LargeRegulatedMotor(MotorPort.A).also {
            logger.info { "Connected to motor:A" }
        }
    }

    protected val motorBDelegate: Lazy<RegulatedMotor> = lazy {
        EV3MediumRegulatedMotor(MotorPort.B).also {
            logger.info { "Connected to motor:B" }
        }
    }

    protected val motorCDelegate: Lazy<RegulatedMotor> = lazy {
        EV3MediumRegulatedMotor(MotorPort.C).also {
            logger.info { "Connected to motor:C" }
        }
    }

    protected val motorDDelegate: Lazy<RegulatedMotor> = lazy {
        EV3LargeRegulatedMotor(MotorPort.D).also {
            logger.info { "Connected to motor:D" }
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun close() {
        if (requiresSmoothClose.compareAndSet(true, false)) {
            val shutdownTime = measureTime {
                for (motorDel in listOf(motorADelegate, motorBDelegate, motorCDelegate, motorDDelegate)) {
                    if (motorDel.isInitialized()) {
                        logger.warn { "Closing ${motorDel.value}" }
                        motorDel.value.flt(true)
                        motorDel.value.close()
                    } else {
                        logger.info { "Shutdown skipping motor ${motorDel}." }
                    }
                }
            }
            println("Graceful shut down:end in $shutdownTime")
        } else {
            logger.debug { "Already closed." }
        }
    }

     fun pressButtonsUntilPose(motor0: RegulatedMotor, motor1: RegulatedMotor) {
        while (true) {
            when (Button.waitForAnyPress()) {
                Button.ID_ENTER -> {
                    motor0.stop(false)
                    motor1.stop(false)
                    "Motor0: ${motor0.tachoCount}".let {
                        println(it)
                        LCD.drawString(it, 0, 0 * LCD.FONT_HEIGHT)
                    }
                    "Motor1: ${motor1.tachoCount}".let {
                        println(it)
                        LCD.drawString(it, 0, 1*LCD.FONT_HEIGHT)
                    }
                    return
                }
                Button.ID_UP -> motor0.rotate(1)
                Button.ID_DOWN -> motor0.rotate(-1)
                Button.ID_LEFT -> motor1.rotate(1)
                Button.ID_RIGHT -> motor1.rotate(-1)
                Button.ID_ESCAPE -> {
                    // Allows emergency bail
                    motor0.flt(true)
                    motor1.flt(true)
                    throw Exception("Emergency exit from calibration.")
                }
            }
        }
    }

    companion object: KLoggable {
        override val logger = logger()
        private val requiresSmoothClose = AtomicBoolean(true)
    }


}