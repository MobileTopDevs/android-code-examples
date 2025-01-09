package com.sample.app.fragment.transactions

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sample.app.R
import com.sample.app.base.BaseFragmentViewModel
import com.sample.app.base.Data
import com.sample.app.databinding.FragmentListTransactionBinding
import com.sample.app.fragment.adapters.LoadingStateAdapter
import com.sample.app.fragment.adapters.TransactionsAdapter
import com.sample.app.util.toData
import com.sample.app.viewModel.TransactionListViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransactionListFragment :
    BaseFragmentViewModel<FragmentListTransactionBinding, TransactionListViewModel>() {

    override val viewModel: TransactionListViewModel by viewModels()

    @Inject
    lateinit var transactionsAdapter: TransactionsAdapter

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_list_transaction
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        initAdapter()
        initListeners()
        initObservers()

        if (viewModel.transactions.value == null) {
            viewModel.loadTransactions(disputeTransactions = false)
        }
    }

    private fun initListeners() {
        binding.transactionListSwipeRefresh.setOnRefreshListener {
            transactionsAdapter.refresh()
        }
        transactionsAdapter.itemClick = { transaction ->
            findNavController().navigate(
                TransactionListFragmentDirections.actionTransactionListFragmentToTransactionDetailsFragment(
                    transaction.id
                )
            )
        }
        binding.transactionListNewTransactionBtn.setOnClickListener {
            findNavController().navigate(
                TransactionListFragmentDirections.actionTransactionListFragmentToNewTransactionFragment()
            )
        }
    }

    private fun initAdapter() {
        binding.transactionListTransactionsRv.adapter = transactionsAdapter.let {
            it.withLoadStateHeaderAndFooter(
                header = LoadingStateAdapter { it.retry() },
                footer = LoadingStateAdapter { it.retry() }
            )
        }
    }

    private fun initObservers() {
        with(viewModel) {
            transactions.observe(viewLifecycleOwner) { pagingData ->
                transactionsAdapter.submitData(lifecycle, pagingData)
            }
            viewModel.transactionsStatus.observe(viewLifecycleOwner) {
                when (it.status) {
                    Data.Status.LOADING -> {
                        dismissSnackBar()
                    }
                    Data.Status.SUCCESS -> {
                        dismissSnackBar()
                    }
                    Data.Status.ERROR -> {
                        showSnackBarWithAction(it.getErrorMessage(), Snackbar.LENGTH_INDEFINITE) {
                            transactionsAdapter.refresh()
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            transactionsAdapter.loadStateFlow.map { it.refresh }
                .collect { loadState ->
                    viewModel.updateStatus(loadState.toData(transactionsAdapter.itemCount))
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.filter_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.filter_menu_item) {
            findNavController().navigate(
                TransactionListFragmentDirections.actionTransactionListFragmentToTransactionsFiltersFragment()
            )
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}