package com.saasjohans.deliveryheroes.data.local.manager.implementation

import com.saasjohans.deliveryheroes.data.local.dao.OrderDao
import com.saasjohans.deliveryheroes.data.local.dao.OrderDetailsDao
import com.saasjohans.deliveryheroes.data.local.manager.OrderDbManager
import com.saasjohans.deliveryheroes.data.local.models.order.DbOrder
import com.saasjohans.deliveryheroes.data.local.models.order.DbOrderUpdate
import com.saasjohans.deliveryheroes.data.local.models.order_details.DbOrderDetails
import com.saasjohans.deliveryheroes.data.local.models.order_details.DbOrderDetailsUpdate
import com.saasjohans.deliveryheroes.data.models.courier.CourierStatus
import com.saasjohans.deliveryheroes.data.models.order.Order
import com.saasjohans.deliveryheroes.data.models.order_details.OrderDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderDbManagerImpl(
    private val orderDao: OrderDao,
    private val orderDetailsDao: OrderDetailsDao
) : OrderDbManager {

    override suspend fun saveOrders(orders: List<Order>) {
        val dbOrders = orders.map { DbOrder.createFromModel(it) }
        orderDao.insertAll(dbOrders)
    }

    override fun getOrders(): Flow<List<Order>> {
        return orderDao.getAll().map { dbOrders ->
            dbOrders.map { dbOrder -> DbOrder.mapToModel(dbOrder) }
        }
    }

    override suspend fun updateOrderCourierStatus(id: Long, courierStatus: CourierStatus) {
        orderDao.update(
            DbOrderUpdate(id = id, courierStatus = courierStatus)
        )
    }

    override suspend fun deleteOrders() {
        orderDao.deleteAll()
    }

    override suspend fun deleteOrder(id: Long) {
        orderDao.deleteById(id)
    }

    override suspend fun saveOrderDetails(orderDetails: OrderDetails) {
        orderDetailsDao.insert(
            DbOrderDetails.createFromModel(orderDetails)
        )
    }

    override fun getOrderDetails(id: Long): Flow<OrderDetails?> {
        return orderDetailsDao.getById(id).map {
            it?.let { DbOrderDetails.mapToModel(it) }
        }
    }

    override suspend fun updateOrderDetailsCourierStatus(id: Long, courierStatus: CourierStatus) {
        orderDetailsDao.update(
            DbOrderDetailsUpdate(id = id, courierStatus = courierStatus)
        )
    }

    override suspend fun deleteAllOrdersDetails() {
        orderDetailsDao.deleteAll()
    }

    override suspend fun deleteOrderDetails(id: Long) {
        orderDetailsDao.deleteById(id)
    }
}