package com.saasjohans.deliveryheroes.ui.order_accept

import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.saasjohans.deliveryheroes.R
import com.saasjohans.deliveryheroes.base.BaseFragmentWithViewModel
import com.saasjohans.deliveryheroes.data.Result
import com.saasjohans.deliveryheroes.databinding.FragmentOrderAcceptBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ncorti.slidetoact.SlideToActView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderAcceptFragment :
    BaseFragmentWithViewModel<FragmentOrderAcceptBinding, OrderAcceptViewModel>() {

    override val viewModel: OrderAcceptViewModel by viewModels()

    override fun getFragmentLayoutRedId(): Int = R.layout.fragment_order_accept

    private val args: OrderAcceptFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        initObservers()
        initListeners()

        if (savedInstanceState == null) {
            expandBottomSheet()
        }
    }

    private fun expandBottomSheet() {
        val layoutParams =
            binding.orderAcceptBottomSheet.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior as BottomSheetBehavior
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.acceptOrderResponse.collect { result ->
                        if (result is Result.Error) {
                            showSnackBar(result.message)
                            viewModel.resetOrderDetailsResponse()
                        } else if (result is Result.Success) {
                            val accepted = result.data
                            if (accepted) {
                                findNavController().navigate(
                                    OrderAcceptFragmentDirections.openOrderWayToMerchantFragment(
                                        orderId = args.orderId
                                    )
                                )
                            } else {
                                // close order screen (go back to home screen)
                                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                                    viewModel.resetOrderDetailsResponse()
                                    // TODO: use temporary this instead of navController, need to fix up navigation this previous activity
                                    requireActivity().onBackPressed()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initListeners() {
        binding.orderAcceptAcceptBtn.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
                    viewModel.acceptOrder(accepted = true)
                    view.resetSlider()
                }
            }

        binding.orderAcceptDeclineBtn.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
                    viewModel.acceptOrder(accepted = false)
                    view.resetSlider()
                }
            }
    }
}