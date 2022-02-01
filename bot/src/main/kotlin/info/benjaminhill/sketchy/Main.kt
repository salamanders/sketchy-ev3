package info.benjaminhill.sketchy

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import lejos.hardware.motor.BaseRegulatedMotor
import lejos.hardware.port.SensorPort
import lejos.hardware.sensor.EV3TouchSensor
import mu.KotlinLogging

internal val logger = KotlinLogging.logger {}

fun main() {
    println("Hello, World")
    TwoMotorArm().use { tmd->
        tmd.pressButtonsUntilPose(tmd.shoulder, tmd.elbow)
    }
    println("Goodbye!")
}


@FlowPreview
fun button(): Flow<Boolean> = flow {
    val sensor = EV3TouchSensor(SensorPort.S2)
    val sampleSize = sensor.sampleSize()
    val samples = FloatArray(sampleSize)
    while (true) {
        sensor.fetchSample(samples, 0)
        emit(samples[0]>.5f)
        delay(100)
    }
}.debounce(200)
    .conflate()
    .distinctUntilChanged()

suspend fun Flow<Boolean>.waitForPress() {
    filter { it }.first()
}

@FlowPreview
suspend fun getRanges(
    leftShoulder: BaseRegulatedMotor,
    rightShoulder: BaseRegulatedMotor
): Pair<ClosedFloatingPointRange<Float>, ClosedFloatingPointRange<Float>> {
    println("SPIN LEFT!  Move left-shoulder to 45° down-left, right-shoulder to 45° up-left, then click.")
    button().waitForPress()
    val leftMin = leftShoulder.position
    val rightMin = rightShoulder.position
    println("SPIN RIGHT! Move left-shoulder to 45° up-right, right-shoulder to 45° down-right, then click.")
    button().waitForPress()
    val leftMax = leftShoulder.position
    val rightMax = rightShoulder.position

    return Pair(leftMin..leftMax, rightMin..rightMax)
}

