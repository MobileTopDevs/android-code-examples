package com.sample.app.viewModel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sample.app.base.BaseViewModel
import com.sample.app.base.Data
import com.sample.app.data.api.models.Transaction
import com.sample.app.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val repository: Repository
) : BaseViewModel() {
    private var transactionsJob: Job? = null

    private val transactionsResponse = MutableLiveData<PagingData<Transaction>>()
    val transactions: LiveData<PagingData<Transaction>> = transactionsResponse

    private val _transactionsStatus = MutableLiveData<Data<Int>>()

    val transactionsStatus: LiveData<Data<Int>> = _transactionsStatus.distinctUntilChanged()

    val transactionsEmpty = Transformations.map(transactionsStatus) {
        it.status == Data.Status.SUCCESS && it.data == 0
    }

    val transactionsLoading = Transformations.map(transactionsStatus) {
        it.status == Data.Status.LOADING && (it.data == null || it.data == 0)
    }

    fun loadTransactions(disputeTransactions: Boolean) {
        transactionsJob?.cancel()
        transactionsJob = viewModelScope.launch {
            repository.getTransactions(disputeTransactions = disputeTransactions)
                .cachedIn(viewModelScope)
                .collectLatest {
                    transactionsResponse.value = it
                }
        }
    }

    fun updateStatus(transactionsStatus: Data<Int>) {
        this._transactionsStatus.value = transactionsStatus
    }
}