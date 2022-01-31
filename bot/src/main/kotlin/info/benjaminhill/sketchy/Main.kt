package info.benjaminhill.sketchy

import mu.KotlinLogging

internal val logger = KotlinLogging.logger {}

fun main() {
    println("Hello, World")
    TwoMotorArm().use { tmd->
        tmd.pressButtonsUntilPose(tmd.shoulder, tmd.elbow)
    }
    println("Goodbye!")
}

