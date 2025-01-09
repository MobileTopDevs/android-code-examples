package com.solocator.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.OrientationEventListener
import com.solocator.cameraUtils.DisplayOrientation
import com.solocator.common.SharedPrefsManager
import com.solocator.util.DisplayOrientationUtils
import com.solocator.util.round
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.sample

class OrientationManager(
    context: Context,
    sharedPrefsManager: SharedPrefsManager
) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationVectorFlow = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    trySend(event.values.clone())
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.also { sensor ->
            sensorManager.registerListener(
                listener,
                sensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    private val displayOrientationFlow: Flow<DisplayOrientation> = callbackFlow {
        val listener = object : OrientationEventListener(context) {

            private var lastDisplayOrientation = DisplayOrientation.PORTRAIT_NORMAL

            override fun onOrientationChanged(orientation: Int) {
                val current = DisplayOrientationUtils.getDisplayOrientationWithHysteresis(
                    orientation = orientation,
                    lastDisplayOrientation = lastDisplayOrientation
                )
                if (current != lastDisplayOrientation) {
                    lastDisplayOrientation = current
                    trySend(current)
                }
            }
        }
        trySend(DisplayOrientation.PORTRAIT_NORMAL)

        if (listener.canDetectOrientation()) {
            listener.enable()
        }
        awaitClose {
            listener.disable()
        }
    }

    val fixedDisplayOrientation = combine(
        displayOrientationFlow,
        sharedPrefsManager.isLandscapeLocked
    ) { displayOrientation, isLandscapeLocked ->
        DisplayOrientationUtils.fixDisplayOrientation(
            displayOrientation = displayOrientation,
            isLandscapeLocked = isLandscapeLocked
        )
    }

    val orientationAnglesFlow = combine(
        rotationVectorFlow,
        fixedDisplayOrientation
    )
    { rotationVector, displayOrientation ->
        val worldAxisForDeviceAxisX: Int
        val worldAxisForDeviceAxisY: Int
        when (displayOrientation) {
            DisplayOrientation.LANDSCAPE_INVERTED -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z
                worldAxisForDeviceAxisY = SensorManager.AXIS_X
            }

            DisplayOrientation.PORTRAIT_INVERTED -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z
            }

            DisplayOrientation.LANDSCAPE_NORMAL -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_Z
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X
            }

            else -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_X
                worldAxisForDeviceAxisY = SensorManager.AXIS_Z
            }
        }

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        val adjustedRotationMatrix = FloatArray(9)

        SensorManager.getRotationMatrixFromVector(
            rotationMatrix,
            rotationVector
        )
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            worldAxisForDeviceAxisX,
            worldAxisForDeviceAxisY,
            adjustedRotationMatrix
        )
        SensorManager.getOrientation(adjustedRotationMatrix, orientationAngles)

        orientationAngles
            .mapIndexed { index, fl ->
                if (index == 1) {
                    Math.toDegrees(-fl.toDouble()).toFloat() // pitch fix
                } else {
                    Math.toDegrees(fl.toDouble()).toFloat()
                }
            }
            .map { it.round(1) }.toFloatArray()
    }.flowOn(Dispatchers.Default)
}
