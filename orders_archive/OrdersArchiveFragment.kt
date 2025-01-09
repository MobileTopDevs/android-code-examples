package com.saasjohans.deliveryheroes.ui.orders_archive

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.saasjohans.deliveryheroes.R
import com.saasjohans.deliveryheroes.base.BaseFragmentWithViewModel
import com.saasjohans.deliveryheroes.data.Result
import com.saasjohans.deliveryheroes.data.models.courier.CourierStatus
import com.saasjohans.deliveryheroes.databinding.FragmentOrdersArchiveBinding
import com.saasjohans.deliveryheroes.ui.adapters.OrdersAdapter
import com.saasjohans.deliveryheroes.util.Formats
import com.saasjohans.deliveryheroes.util.date
import com.saasjohans.deliveryheroes.util.localDateTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*


@AndroidEntryPoint
class OrdersArchiveFragment :
    BaseFragmentWithViewModel<FragmentOrdersArchiveBinding, OrdersArchiveViewModel>(),
    MenuProvider {

    override val viewModel: OrdersArchiveViewModel by viewModels()

    override fun getFragmentLayoutRedId(): Int = R.layout.fragment_orders_archive

    private lateinit var ordersAdapter: OrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        setupOrdersRecyclerView()
        initObservers()
    }

    private fun setupOrdersRecyclerView() {
        ordersAdapter = OrdersAdapter(withTime = true) { order ->
            val title = getString(R.string.order_title, order.id)
            findNavController().navigate(
                if (order.courierStatus in setOf(CourierStatus.FAILED, CourierStatus.DONE)) {
                    OrdersArchiveFragmentDirections.openOrderDetails(
                        orderId = order.id,
                        title = title
                    )
                } else {
                    OrdersArchiveFragmentDirections.openOrderActivity(
                        orderId = order.id,
                        title = title
                    )
                }
            )
        }
        binding.ordersArchiveRecyclerView.adapter = ordersAdapter
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.ordersResponse.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                ordersAdapter.submitList(result.data)
                            }
                            is Result.Error -> {
                                showSnackBarWithAction(result.message) {
                                    viewModel.reloadOrders()
                                }
                                viewModel.resetOrdersResponse()
                            }
                            else -> {
                                /* ignore */
                            }
                        }
                    }
                }
                launch {
                    viewModel.dateFilter.collect {
                        interactionListener?.setToolbarTitle(
                            Formats.dateFormat(activityContext, it, Formats.DATE_FORMAT_DD_MM_YYYY)
                        )
                    }
                }
            }
        }
    }

    private fun selectDate() {
        val calendar = Calendar.getInstance().apply {
            time = viewModel.dateFilter.value.date
        }
        DatePickerDialog(
            activityContext,
            { _, year, month, dayOfMonth ->
                val date = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.MONTH, month)
                    set(Calendar.YEAR, year)
                }.time.localDateTime

                if (date.isAfter(LocalDateTime.now())) {
                    showSnackBar(getString(R.string.orders_archive_date_cannot_be_set_in_future))
                    return@DatePickerDialog
                }

                viewModel.setDate(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.orders_archive_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menu_orders_archive_date -> {
                selectDate()
                true
            }
            else -> false
        }
    }
}