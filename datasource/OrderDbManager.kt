package com.saasjohans.deliveryheroes.data.local.manager

import com.saasjohans.deliveryheroes.data.models.courier.CourierStatus
import com.saasjohans.deliveryheroes.data.models.order.Order
import com.saasjohans.deliveryheroes.data.models.order_details.OrderDetails
import kotlinx.coroutines.flow.Flow

interface OrderDbManager {
    suspend fun saveOrders(orders: List<Order>)

    fun getOrders(): Flow<List<Order>>

    suspend fun updateOrderCourierStatus(id: Long, courierStatus: CourierStatus)

    suspend fun deleteOrders()

    suspend fun deleteOrder(id: Long)

    suspend fun saveOrderDetails(orderDetails: OrderDetails)

    fun getOrderDetails(id: Long): Flow<OrderDetails?>

    suspend fun deleteAllOrdersDetails()

    suspend fun updateOrderDetailsCourierStatus(id: Long, courierStatus: CourierStatus)

    suspend fun deleteOrderDetails(id: Long)
}