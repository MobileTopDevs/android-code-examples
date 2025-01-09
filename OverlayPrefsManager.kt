package com.solocator.common

import android.content.SharedPreferences
import com.solocator.manager.OverlayPrefsStorage.Companion.BACKGROUND_GRADIENT
import com.solocator.manager.OverlayPrefsStorage.Companion.CROSSHAIR_FLAG
import com.solocator.manager.OverlayPrefsStorage.Companion.CROSSHAIR_SIZE
import com.solocator.manager.OverlayPrefsStorage.Companion.DATE_FLAG
import com.solocator.manager.OverlayPrefsStorage.Companion.DATE_FORMAT
import com.solocator.manager.OverlayPrefsStorage.Companion.DATE_MODE
import com.solocator.manager.OverlayPrefsStorage.Companion.DATE_SEPARATOR
import com.solocator.manager.OverlayPrefsStorage.Companion.FONT_SIZE
import com.solocator.manager.OverlayPrefsStorage.Companion.HOUR_FORMAT_FLAG
import com.solocator.manager.OverlayPrefsStorage.Companion.ROLL_FLAG
import com.solocator.manager.OverlayPrefsStorage.Companion.TEXT_COLOR
import com.solocator.manager.OverlayPrefsStorage.Companion.TEXT_ROLL_SIZE
import com.solocator.manager.OverlayPrefsStorage.Companion.TILT_FLAG
import com.solocator.manager.OverlayPrefsStorage.Companion.TILT_ROLL_FLAG
import com.solocator.manager.OverlayPrefsStorage.Companion.TIME_ZONE
import com.solocator.manager.OverlayPrefsStorage.Companion.WATERMARK
import com.solocator.manager.OverlayPrefsStorage.Companion.WATERMARK_LOGO_ALIGNMENT
import com.solocator.manager.OverlayPrefsStorage.Companion.WATERMARK_LOGO_ALIGNMENT_LAND
import com.solocator.manager.OverlayPrefsStorage.Companion.WATERMARK_LOGO_ALPHA
import com.solocator.manager.OverlayPrefsStorage.Companion.WATERMARK_LOGO_ALPHA_LAND
import com.solocator.manager.OverlayPrefsStorage.Companion.WATERMARK_LOGO_ENABLED
import com.solocator.manager.OverlayPrefsStorage.Companion.WATERMARK_LOGO_NAME
import com.solocator.manager.OverlayPrefsStorage.Companion.WATERMARK_LOGO_PERCENTAGE
import com.solocator.manager.OverlayPrefsStorage.Companion.WATERMARK_LOGO_PERCENTAGE_LAND
import com.solocator.manager.OverlayPrefsStorage.Companion.WATERMARK_LOGO_X_POSITION_PERCENT
import com.solocator.manager.OverlayPrefsStorage.Companion.WATERMARK_LOGO_X_POSITION_PERCENT_LAND
import com.solocator.manager.OverlayPrefsStorage.Companion.WATERMARK_LOGO_Y_POSITION_PERCENT
import com.solocator.manager.OverlayPrefsStorage.Companion.WATERMARK_LOGO_Y_POSITION_PERCENT_LAND
import com.solocator.manager.OverlayPrefsStorage.Companion.crosshairSizes
import com.solocator.manager.OverlayPrefsStorage.Companion.dateSeparators
import com.solocator.manager.OverlayPrefsStorage.Companion.textColors
import com.solocator.manager.OverlayPrefsStorage.Companion.textRollSizes
import com.solocator.manager.OverlayPrefsStorage.DateFormatModel
import com.solocator.manager.OverlayPrefsStorage.DateMode
import com.solocator.manager.OverlayPrefsStorage.FontSize
import com.solocator.model.camera.OverlaySettings
import com.solocator.util.Constants
import com.solocator.util.booleanFlow
import com.solocator.util.floatFlow
import com.solocator.util.intFlow
import com.solocator.util.stringFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class OverlayPrefsManager(sharedPreferences: SharedPreferences) {
    val isDateEnabled = sharedPreferences.booleanFlow(
        DATE_FLAG,
        true
    )

    val dateMode = sharedPreferences.stringFlow(
        DATE_MODE,
        DateMode.LocalDateTime.name
    ).map { dateModeString ->
        dateModeString?.let { DateMode.valueOf(it) } ?: DateMode.LocalDateTime
    }

    val dateFormat = sharedPreferences.stringFlow(
        DATE_FORMAT,
        DateFormatModel.startingValue.title
    ).map { dateFormatString ->
        dateFormatString?.let { DateFormatModel.values.find { format -> it == format.title } }
            ?: DateFormatModel.startingValue
    }

    val dateSeparator = sharedPreferences.stringFlow(
        DATE_SEPARATOR,
        dateSeparators.first()
    )

    val timeZone = sharedPreferences.booleanFlow(
        TIME_ZONE,
        false
    )

    val hourFormatFlag = sharedPreferences.booleanFlow(
        HOUR_FORMAT_FLAG,
        false
    )

    val isTiltRollEnabled = sharedPreferences.booleanFlow(
        TILT_ROLL_FLAG,
        false
    )

    val rollFlag = sharedPreferences.booleanFlow(
        ROLL_FLAG,
        false
    )

    val crossHairFlag = sharedPreferences.booleanFlow(
        CROSSHAIR_FLAG,
        false
    )

    val tiltFlag = sharedPreferences.booleanFlow(
        TILT_FLAG,
        false
    )

    val crossHairSize = sharedPreferences.stringFlow(
        CROSSHAIR_SIZE,
        crosshairSizes.first()
    )

    val textRollSize = sharedPreferences.stringFlow(
        TEXT_ROLL_SIZE,
        textRollSizes.first()
    )

    val textColor = sharedPreferences.stringFlow(
        TEXT_COLOR,
        textColors.first()
    )

    val backgroundGradient = sharedPreferences.intFlow(
        BACKGROUND_GRADIENT,
        0
    )

    val watermark = sharedPreferences.stringFlow(
        WATERMARK,
        ""
    )

    val fontSize = sharedPreferences.intFlow(
        FONT_SIZE,
        FontSize.startingValue.size
    ).map { FontSize.values.find { size -> it == size.size } ?: FontSize.startingValue }

    val watermarkLogoEnabled = sharedPreferences.booleanFlow(
        WATERMARK_LOGO_ENABLED,
        false
    )

    val watermarkLogoName = sharedPreferences.stringFlow(
        WATERMARK_LOGO_NAME,
        "default_placeholder.jpg"
    )

    val watermarkLogoAlpha = sharedPreferences.floatFlow(
        WATERMARK_LOGO_ALPHA,
        1f
    )

    val watermarkLogoPercentage = sharedPreferences.floatFlow(
        WATERMARK_LOGO_PERCENTAGE,
        30f
    )

    val watermarkLogoXPositionPercent = sharedPreferences.floatFlow(
        WATERMARK_LOGO_X_POSITION_PERCENT,
        0f
    )

    val watermarkLogoYPositionPercent = sharedPreferences.floatFlow(
        WATERMARK_LOGO_Y_POSITION_PERCENT,
        0f
    )

    val watermarkLogoAlphaLand = sharedPreferences.floatFlow(
        WATERMARK_LOGO_ALPHA_LAND,
        1f
    )

    val watermarkLogoPercentageLand = sharedPreferences.floatFlow(
        WATERMARK_LOGO_PERCENTAGE_LAND,
        30f
    )

    val watermarkLogoXPositionPercentLand = sharedPreferences.floatFlow(
        WATERMARK_LOGO_X_POSITION_PERCENT_LAND,
        0f
    )

    val watermarkLogoYPositionPercentLand = sharedPreferences.floatFlow(
        WATERMARK_LOGO_Y_POSITION_PERCENT_LAND,
        0f
    )

    val watermarkLogoAlignment = sharedPreferences.intFlow(
        WATERMARK_LOGO_ALIGNMENT,
        0
    ).map { WatermarkLogoAlignment.values()[it] }

    val watermarkLogoAlignmentLand = sharedPreferences.intFlow(
        WATERMARK_LOGO_ALIGNMENT_LAND,
        0
    ).map { WatermarkLogoAlignment.values()[it] }

    val projectName = sharedPreferences.stringFlow(
        Constants.CURRENT_PROJECT_SP,
        ""
    )
    val photoDescription = sharedPreferences.stringFlow(
        Constants.CURRENT_PROJECT_DESCRIPTION,
        ""
    )

    val nameAndDescEnabled = sharedPreferences.booleanFlow(
        Constants.PROJECT_BTN_TURN
    )

    val overlaySettingsFlow = combine(
        isDateEnabled, // 0
        dateMode, // 1
        dateFormat, // 2
        dateSeparator, // 3
        timeZone, // 4
        hourFormatFlag, // 5
        isTiltRollEnabled, // 6
        rollFlag, // 7
        crossHairFlag, // 8
        tiltFlag, // 9
        crossHairSize, // 10
        textRollSize, // 11
        textColor, // 12
        backgroundGradient, // 13
        watermark, // 14
        fontSize, // 15
        watermarkLogoEnabled, // 16
        watermarkLogoName, // 17
        watermarkLogoAlpha, // 18
        watermarkLogoPercentage, // 19
        watermarkLogoXPositionPercent, // 20
        watermarkLogoYPositionPercent, // 21
        watermarkLogoAlphaLand, // 22
        watermarkLogoPercentageLand, // 23
        watermarkLogoXPositionPercentLand, // 24
        watermarkLogoYPositionPercentLand, // 25
        projectName, // 26
        photoDescription, // 27
        nameAndDescEnabled, // 28
        watermarkLogoAlignment, //29
        watermarkLogoAlignmentLand //30
    ) { settings ->
        OverlaySettings(
            isDateEnabled = settings[0] as Boolean,
            dateMode = settings[1] as DateMode,
            dateFormat = settings[2] as DateFormatModel,
            dateSeparator = settings[3] as String?,
            timeZone = settings[4] as Boolean,
            hourFormatFlag = settings[5] as Boolean,
            isTiltRollEnabled = settings[6] as Boolean,
            rollFlag = settings[7] as Boolean,
            crossHairFlag = settings[8] as Boolean,
            tiltFlag = settings[9] as Boolean,
            crossHairSize = settings[10] as String?,
            textRollSize = settings[11] as String?,
            textColor = settings[12] as String?,
            backgroundGradient = settings[13] as Int,
            watermark = settings[14] as String,
            fontSize = settings[15] as FontSize,
            watermarkLogoEnabled = settings[16] as Boolean,
            watermarkLogoName = settings[17] as String,
            watermarkLogoAlpha = settings[18] as Float,
            watermarkLogoPercentage = settings[19] as Float,
            watermarkLogoXPositionPercent = settings[20] as Float,
            watermarkLogoYPositionPercent = settings[21] as Float,
            watermarkLogoAlphaLand = settings[22] as Float,
            watermarkLogoPercentageLand = settings[23] as Float,
            watermarkLogoXPositionPercentLand = settings[24] as Float,
            watermarkLogoYPositionPercentLand = settings[25] as Float,
            projectName = settings[26] as String,
            photoDescription = settings[27] as String,
            nameAndDescEnabled = settings[28] as Boolean,
            watermarkLogoAlignment = settings[29] as WatermarkLogoAlignment,
            watermarkLogoAlignmentLand = settings[30] as WatermarkLogoAlignment
        )
    }
}