package com.saasjohans.deliveryheroes.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.saasjohans.deliveryheroes.R
import com.saasjohans.deliveryheroes.base.BaseFragmentWithViewModel
import com.saasjohans.deliveryheroes.data.Result
import com.saasjohans.deliveryheroes.data.models.OnBoardingState
import com.saasjohans.deliveryheroes.data.models.courier.CourierStatus
import com.saasjohans.deliveryheroes.data.models.order.Order
import com.saasjohans.deliveryheroes.databinding.FragmentHomeBinding
import com.saasjohans.deliveryheroes.service.LocationUpdatesService
import com.saasjohans.deliveryheroes.ui.adapters.OrdersAdapter
import com.saasjohans.deliveryheroes.util.LocationManager
import com.saasjohans.deliveryheroes.util.PermissionsUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset


@AndroidEntryPoint
class HomeFragment : BaseFragmentWithViewModel<FragmentHomeBinding, HomeViewModel>() {

    override val viewModel: HomeViewModel by viewModels()

    override fun getFragmentLayoutRedId(): Int = R.layout.fragment_home

    private lateinit var ordersAdapter: OrdersAdapter

    private val notificationsRequestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // ignore
    }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.entries.any { it.value }) {
                startLocationService()
                LocationManager.checkSystemLocationSettings(requireActivity())
                PermissionsUtil.requestBackgroundLocationIfNeeded(activityContext)
            }
        }

    private lateinit var onBoarding: OnBoarding

    private val locationPermissionsGranted = ObservableBoolean(false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.locationPermissionsGranted = locationPermissionsGranted

        initUI()
        setupOrdersRecyclerView()
        initHomeToolbar()
        initObservers()

        if (viewModel.ordersResponse.value.initial) {
            viewModel.loadOrders()
        }

        if (viewModel.userResponse.value.initial) {
            viewModel.fetchUser()
        }

        requestNotificationsPermissionIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        locationPermissionsGranted.set(
            PermissionsUtil.areLocationAndBackgroundLocationGranted(
                activityContext
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBoarding.destroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initUI() {
        // disable swipe in switch, because isPressed returns false when swiping
        binding.homeAvailableForWorkSwitch.setOnTouchListener { _, event ->
            event.actionMasked == MotionEvent.ACTION_MOVE
        }
        onBoarding = OnBoarding(requireActivity()) {
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.setOnBoardingState(it)
            }
        }
    }

    private fun setupOrdersRecyclerView() {
        ordersAdapter = OrdersAdapter { order ->
            val title = getString(R.string.order_title, order.id)
            findNavController().navigate(
                if (order.courierStatus in setOf(CourierStatus.FAILED, CourierStatus.DONE)) {
                    HomeFragmentDirections.openOrderDetails(
                        orderId = order.id,
                        title = title
                    )
                } else {
                    HomeFragmentDirections.openOrderActivity(
                        orderId = order.id,
                        title = title
                    )
                }
            )
        }
        binding.ordersRecyclerView.adapter = ordersAdapter
    }

    private fun initHomeToolbar() {
        binding.homeToolbar.apply {
            inflateMenu(R.menu.home_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_home_refresh -> {
                        viewModel.refreshAllData()
                    }
                }
                true
            }
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.orders.collect { orders ->
                        ordersAdapter.submitList(orders)
                    }
                }
                launch {
                    viewModel.ordersResponse.collect { result ->
                        if (result is Result.Error) {
                            showSnackBarWithAction(result.message) {
                                viewModel.refreshAllData()
                            }
                            viewModel.resetOrdersResponse()
                        }
                    }
                }
                launch {
                    viewModel.userResponse.collect { result ->
                        if (result is Result.Error) {
                            showSnackBarWithAction(result.message) {
                                viewModel.refreshAllData()
                            }
                            viewModel.resetUserFetchResponse()
                        }
                    }
                }
                launch {
                    viewModel.changeCourierStatusResponse.collect { result ->
                        if (result is Result.Error) {
                            showSnackBar(result.message)
                            binding.homeAvailableForWorkSwitch.isChecked =
                                viewModel.availableForWork.value ?: false
                            viewModel.resetChangeCourierStatusResponse()
                        } else if (result is Result.Success) {
                            viewModel.resetChangeCourierStatusResponse()
                        }
                    }
                }
                launch {
                    viewModel.availableForWork.filterNotNull().collect { available ->
                        if (available) {
                            PermissionsUtil.requestLocationPermission(
                                requireActivity(), requestLocationPermissionLauncher
                            ) {
                                startLocationService()
                            }
                        } else {
                            stopLocationService()
                        }
                    }
                }
                launch {
                    viewModel.onBoardingState.filterNotNull().collect {
                        if (it != OnBoardingState.FINISHED
                            && binding.homeOnboardingOrderItem.order == null
                        ) {
                            binding.homeOnboardingOrderItem.order = Order(
                                id = 2436L,
                                customerAddress = getString(R.string.onboarding_order_item_address),
                                courierStatus = CourierStatus.NEW,
                                updatedAt = LocalDateTime.now(ZoneOffset.UTC)
                            )
                        }
                        onBoarding.setState(it)
                    }
                }
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun requestNotificationsPermissionIfNeeded() {
        if (PermissionsUtil.isNotificationsGranted(activityContext)) {
            notificationsRequestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun startLocationService() {
        LocationUpdatesService.startService(activityContext)
    }

    private fun stopLocationService() {
        LocationUpdatesService.stopService(activityContext)
    }
}