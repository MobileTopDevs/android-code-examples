package com.solocator.util.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Rational
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import com.solocator.camera.CameraDescription
import com.solocator.camera.CameraResolutionX
import com.solocator.camera.CameraSettings
import kotlin.math.abs

class CameraHelper(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun getCameraIds(): Array<String> = cameraManager.cameraIdList

    fun getCameraCount(): Int = getCameraIds().size

    fun getCameraCharacteristics(cameraId: String) =
        cameraManager.getCameraCharacteristics(cameraId)

    fun getSupportedCameraResolutions(cameraId: String): List<CameraResolutionX> {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val outputSizes = configMap?.getOutputSizes(ImageFormat.JPEG) ?: return emptyList()

        return outputSizes
            .map { CameraResolutionX(it) }
            .filter { it.rational.isSupportedAspectRatio() }
    }

    fun getAvailableCameraDescriptions() = getCameraIds().map {
        CameraDescription(
            it,
            getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING)
                ?: CameraSelector.LENS_FACING_UNKNOWN
        )
    }

    fun getResolution(cameraSettings: CameraSettings): CameraResolutionX {
        val cameraId = getCameraIds()[cameraSettings.cameraIndex]
        return getSupportedCameraResolutions(cameraId)
            .getOrNull(cameraSettings.resolutionIndex) ?: CameraResolutionX()
    }
}

fun Camera?.isLensFacingBack(): Boolean {
    return this?.cameraInfo?.lensFacing == CameraSelector.LENS_FACING_BACK
}

fun Rational.isSupportedAspectRatio(): Boolean {
    return when {
        abs(this.toFloat() - 4f / 3f) <= 0.01 -> true
        abs(this.toFloat() - 16f / 9f) <= 0.01 -> true
        abs(this.toFloat() - 1f / 1f) <= 0.01 -> true
        else -> false
    }
}

fun Rational.getAspectRatio(): Int {
    return when {
        abs(this.toFloat() - 4f / 3f) <= 0.01 -> AspectRatio.RATIO_4_3
        abs(this.toFloat() - 16f / 9f) <= 0.01 -> AspectRatio.RATIO_16_9
        else -> AspectRatio.RATIO_DEFAULT
    }
}