package com.example.vincente_buenaventura_myruns2

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import androidx.lifecycle.LiveData
import android.app.Application
import androidx.lifecycle.viewmodel.viewModelFactory

class HistoryViewModel(private val repo: HistoryRepo) : ViewModel() {
    val allHistoryLiveData: LiveData<List<HistoryEntry>> = repo.allEntries.asLiveData()


    fun insert(entry: HistoryEntry) {
        repo.insert(entry)
    }

    fun delete(id: Long) {
        repo.delete(id)
    }

}


class HistoryViewModelFactory(private val repo: HistoryRepo): ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        if(modelClass.isAssignableFrom(HistoryViewModel::class.java)){
            return HistoryViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}