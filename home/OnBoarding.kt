package com.saasjohans.deliveryheroes.ui.home

import android.app.Activity
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.saasjohans.deliveryheroes.R
import com.saasjohans.deliveryheroes.data.models.OnBoardingState
import com.saasjohans.deliveryheroes.databinding.LayoutOnboardingControlsBinding
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity
import smartdevelop.ir.eram.showcaseviewlib.config.PointerType

class OnBoarding(
    private val activity: Activity,
    private val onStateUpdate: (OnBoardingState) -> Unit
) {
    private var guideView: GuideView? = null

    private var onNextState: ((OnBoardingState) -> Unit)? = { currentState ->
        val states = OnBoardingState.values()
        val nextIdx = states.indexOf(currentState) + 1
        if (nextIdx < states.size) {
            val nextState = states[nextIdx]
            onStateUpdate(nextState)
        }
    }

    fun setState(onBoardingState: OnBoardingState) {
        guideView?.dismiss()
        guideView = null

        val viewTextIds = when (onBoardingState) {
            OnBoardingState.HOME_PAGE_SECTION -> {
                Pair(R.id.homeFragment, R.string.onboarding_home_page_section)
            }
            OnBoardingState.ORDER_ARCHIVE_SECTION -> {
                Pair(R.id.ordersArchiveFragment, R.string.onboarding_order_archive_section)
            }
            OnBoardingState.PROFILE_SECTION -> {
                Pair(R.id.profileFragment, R.string.onboarding_profile_section)
            }
            OnBoardingState.AVAILABILITY_SWITCH -> {
                Pair(R.id.home_available_for_work_card, R.string.onboarding_availability_switch)
            }
            OnBoardingState.ORDERS_LIST_REFRESH -> {
                Pair(R.id.menu_home_refresh, R.string.onboarding_orders_list_refresh)
            }
            OnBoardingState.ORDER_DETAILS -> {
                Pair(R.id.home_onboarding_order_item, R.string.onboarding_orders_details)
            }
            OnBoardingState.FINISHED -> return
        }

        val view = activity.findViewById<View>(viewTextIds.first)
        val text = activity.getString(viewTextIds.second)

        guideView = GuideView.Builder(activity)
            .setTitle(null)
            .setContentText(text)
            .setGravity(Gravity.auto)
            .setPointerType(PointerType.arrow)
            .setDismissType(DismissType.anywhere)
            .setTargetView(view)
            .setContentTextSize(14)
            .setTitleTextSize(14)
            .setGuideListener { // onDismiss event
                onNextState?.invoke(onBoardingState)
            }
            .build().apply {
                val controlsView =
                    LayoutOnboardingControlsBinding.inflate(activity.layoutInflater)
                controlsView.state = onBoardingState
                controlsView.onboardingSkipBtn.setOnClickListener {
                    onNextState = null
                    onStateUpdate(OnBoardingState.FINISHED)
                }
                controlsView.onboardingNextBtn.setOnClickListener {
                    onNextState?.invoke(onBoardingState)
                }
                post {
                    addView(
                        controlsView.root.apply {
                            val bottom = ViewCompat.getRootWindowInsets(view)
                                ?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: 0
                            updatePadding(bottom = bottom)
                        }
                    )
                }
            }
        guideView?.show()
    }

    fun destroy() {
        onNextState = null
        guideView?.dismiss()
    }
}