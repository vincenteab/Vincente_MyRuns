package com.example.vincente_buenaventura_myruns2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntry::class], version = 1)
abstract class HistoryDatabase: RoomDatabase() {
    abstract val historyDatabaseDao: HistoryDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE: HistoryDatabase? = null

        fun getInstance(context: Context): HistoryDatabase {
            synchronized(this){
                var instance = INSTANCE

                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        HistoryDatabase::class.java,
                        "history_database"
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}