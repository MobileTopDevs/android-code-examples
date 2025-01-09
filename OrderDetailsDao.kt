package com.saasjohans.deliveryheroes.data.local.dao

import androidx.room.*
import com.saasjohans.deliveryheroes.data.local.models.order_details.DbOrderDetails
import com.saasjohans.deliveryheroes.data.local.models.order_details.DbOrderDetailsUpdate
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDetailsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dbOrderDetails: DbOrderDetails): Long

    @Query("SELECT * FROM ${DbOrderDetails.TABLE_NAME} WHERE ${DbOrderDetails.COLUMN_ID}=:id")
    fun getById(id: Long): Flow<DbOrderDetails?>

    @Query("DELETE FROM ${DbOrderDetails.TABLE_NAME}")
    suspend fun deleteAll(): Int

    @Update(entity = DbOrderDetails::class)
    suspend fun update(dbOrderDetailsUpdate: DbOrderDetailsUpdate)

    @Query("DELETE FROM ${DbOrderDetails.TABLE_NAME} WHERE ${DbOrderDetails.COLUMN_ID}=:id")
    suspend fun deleteById(id: Long)
}