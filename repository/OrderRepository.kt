package com.saasjohans.deliveryheroes.data.repository

import com.saasjohans.deliveryheroes.data.Result
import com.saasjohans.deliveryheroes.data.models.courier.CourierStatus
import com.saasjohans.deliveryheroes.data.models.order.Order
import com.saasjohans.deliveryheroes.data.models.order_details.OrderDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface OrderRepository {

    fun observeLocalOrders(): Flow<List<Order>>

    fun fetchOrdersFlow(): Flow<Result<Unit>>

    suspend fun fetchOrders()

    fun getArchiveOrders(date: LocalDateTime): Flow<Result<List<Order>>>

    fun observeLocalOrderDetails(id: Long): Flow<OrderDetails?>

    fun getOrderDetails(id: Long): Flow<Result<OrderDetails>>

    fun acceptOrderByCourier(orderId: Long, accepted: Boolean): Flow<Result<Boolean>>

    fun updateOrderStatusByCourier(orderId: Long, courierStatus: CourierStatus): Flow<Result<Unit>>

}