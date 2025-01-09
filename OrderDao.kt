package com.saasjohans.deliveryheroes.data.local.dao

import androidx.room.*
import com.saasjohans.deliveryheroes.data.local.models.order.DbOrder
import com.saasjohans.deliveryheroes.data.local.models.order.DbOrderUpdate
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<DbOrder>): List<Long>

    @Query("SELECT * FROM ${DbOrder.TABLE_NAME} ORDER BY ${DbOrder.COLUMN_ID} DESC")
    fun getAll(): Flow<List<DbOrder>>

    @Query("DELETE FROM ${DbOrder.TABLE_NAME}")
    suspend fun deleteAll(): Int // or return nothing if no need to know count of deleted rows

    @Update(entity = DbOrder::class)
    suspend fun update(dbOrderUpdate: DbOrderUpdate)

    @Query("DELETE FROM ${DbOrder.TABLE_NAME} WHERE ${DbOrder.COLUMN_ID}=:id")
    suspend fun deleteById(id: Long)
}