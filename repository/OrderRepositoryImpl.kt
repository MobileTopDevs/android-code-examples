package com.saasjohans.deliveryheroes.data.repository.implementation

import com.saasjohans.deliveryheroes.data.Result
import com.saasjohans.deliveryheroes.data.local.manager.OrderDbManager
import com.saasjohans.deliveryheroes.data.models.courier.CourierStatus
import com.saasjohans.deliveryheroes.data.models.order.Order
import com.saasjohans.deliveryheroes.data.models.order_details.OrderDetails
import com.saasjohans.deliveryheroes.data.remote.manager.OrderDataSource
import com.saasjohans.deliveryheroes.data.repository.OrderRepository
import com.saasjohans.deliveryheroes.di.IODispatcher
import com.saasjohans.deliveryheroes.util.wrapAsResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDateTime
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val orderDataSource: OrderDataSource,
    private val orderDbManager: OrderDbManager
) : OrderRepository {

    override fun observeLocalOrders(): Flow<List<Order>> {
        return orderDbManager.getOrders()
    }

    override fun fetchOrdersFlow(): Flow<Result<Unit>> = flow {
        fetchOrders()
        emit(Unit)
    }.wrapAsResult()
        .flowOn(ioDispatcher)

    override suspend fun fetchOrders() {
        val orders = orderDataSource.getOrders(date = null)
        orderDbManager.deleteOrders()
        orderDbManager.deleteAllOrdersDetails()
        orderDbManager.saveOrders(orders)
    }

    override fun getArchiveOrders(date: LocalDateTime): Flow<Result<List<Order>>> = flow {
        val orders = orderDataSource.getOrders(date = date)
        emit(orders)
    }.wrapAsResult()
        .flowOn(ioDispatcher)

    override fun observeLocalOrderDetails(id: Long): Flow<OrderDetails?> {
        return orderDbManager.getOrderDetails(id)
    }

    override fun getOrderDetails(id: Long): Flow<Result<OrderDetails>> = flow {
        val orderDetails = orderDataSource.getOrderDetails(id)
        orderDbManager.saveOrderDetails(orderDetails)
        orderDbManager.updateOrderCourierStatus(
            id = orderDetails.id,
            courierStatus = orderDetails.courierStatus
        )
        emit(orderDetails)
    }.wrapAsResult()
        .flowOn(ioDispatcher)

    override fun acceptOrderByCourier(orderId: Long, accepted: Boolean): Flow<Result<Boolean>> =
        flow {
            orderDataSource.acceptOrderByCourier(orderId, accepted)
            if (accepted) { // update to new status in database
                orderDbManager.updateOrderCourierStatus(
                    id = orderId,
                    courierStatus = CourierStatus.ACCEPTED
                )
                orderDbManager.updateOrderDetailsCourierStatus(
                    id = orderId,
                    courierStatus = CourierStatus.ACCEPTED
                )
            } else { // courier declined this order, remove it from database
                orderDbManager.deleteOrder(orderId)
                orderDbManager.deleteOrderDetails(orderId)
            }
            emit(accepted)
        }.wrapAsResult()
            .flowOn(ioDispatcher)


    override fun updateOrderStatusByCourier(
        orderId: Long,
        courierStatus: CourierStatus
    ): Flow<Result<Unit>> = flow {
        orderDataSource.updateOrderStatusByCourier(
            orderId = orderId,
            courierStatus = courierStatus
        )
        orderDbManager.updateOrderCourierStatus(
            id = orderId,
            courierStatus = courierStatus
        )
        orderDbManager.updateOrderDetailsCourierStatus(
            id = orderId,
            courierStatus = courierStatus
        )
        emit(Unit)
    }
        .wrapAsResult()
        .flowOn(ioDispatcher)
}