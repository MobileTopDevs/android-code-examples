package com.timelimiter.android.features.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.timelimiter.android.features.R

enum class OnBoardingPage(
    @StringRes val titleTextId: Int,
    @StringRes val subtitleTextId: Int,
    @DrawableRes val imageId: Int
) {
    STEP_1(
        titleTextId = R.string.on_boarding_title_text_step_one,
        subtitleTextId = R.string.on_boarding_subtitle_text_step_one,
        imageId = R.drawable.ic_on_boarding_step_1
    ),
    STEP_2(
        titleTextId = R.string.on_boarding_title_text_step_two,
        subtitleTextId = R.string.on_boarding_subtitle_text_step_two,
        imageId = R.drawable.ic_on_boarding_step_2
    ),
    STEP_3(
        titleTextId = R.string.on_boarding_title_text_step_three,
        subtitleTextId = R.string.on_boarding_subtitle_text_step_three,
        imageId = R.drawable.ic_on_boarding_step_3
    )
}