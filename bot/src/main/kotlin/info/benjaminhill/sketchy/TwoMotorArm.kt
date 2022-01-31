package info.benjaminhill.sketchy

import info.benjaminhill.ev3.Motors
import lejos.robotics.RegulatedMotor
import kotlin.time.ExperimentalTime

/** Two bones in a row, like your upper and lower arm, with a pen at the end */
@ExperimentalTime

class TwoMotorArm : Motors() {
    val shoulder: RegulatedMotor by motorADelegate
    val elbow: RegulatedMotor by motorDDelegate
}