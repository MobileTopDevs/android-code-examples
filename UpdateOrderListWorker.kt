package com.saasjohans.deliveryheroes.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.saasjohans.deliveryheroes.data.repository.OrderRepository
import com.saasjohans.deliveryheroes.di.IODispatcher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
class UpdateOrderListWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val orderRepository: OrderRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        try {
            orderRepository.fetchOrders()
            Result.success()
        } catch (error: Throwable) {
            Result.failure()
        }
    }
}