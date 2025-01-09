package com.saasjohans.deliveryheroes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.saasjohans.deliveryheroes.data.local.dao.OrderDao
import com.saasjohans.deliveryheroes.data.local.dao.OrderDetailsDao
import com.saasjohans.deliveryheroes.data.local.models.order.DbOrder
import com.saasjohans.deliveryheroes.data.local.models.order_details.DbOrderDetails
import com.saasjohans.deliveryheroes.data.local.utils.DbTypeConverters

@Database(
    entities = [DbOrder::class, DbOrderDetails::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(DbTypeConverters::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun getOrdersDao(): OrderDao

    abstract fun getOrderDetailsDao(): OrderDetailsDao

    companion object {
        fun getInstance(context: Context) =
            Room.databaseBuilder(context, AppDataBase::class.java, NAME)
                .build()

        fun getTestInstance(context: Context) =
            Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java)
                .fallbackToDestructiveMigration()
                .build()

        private const val NAME = "database"
    }
}