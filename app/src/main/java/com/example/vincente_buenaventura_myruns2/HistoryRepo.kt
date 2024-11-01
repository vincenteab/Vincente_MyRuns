package com.example.vincente_buenaventura_myruns2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.LiveData

class HistoryRepo(private val historyDatabaseDao: HistoryDatabaseDao) {
    val allEntries: Flow<List<HistoryEntry>> = historyDatabaseDao.getAllHistory()

    fun insert(entry: HistoryEntry) {
        CoroutineScope(IO).launch {
            historyDatabaseDao.insert(entry)
        }

    }

    fun delete(id: Long) {
        CoroutineScope(IO).launch {
            historyDatabaseDao.deleteHistory(id)
        }

    }



}