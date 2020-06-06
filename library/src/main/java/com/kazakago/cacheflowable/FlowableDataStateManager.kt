package com.kazakago.cacheflowable

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

abstract class FlowableDataStateManager<KEY> : DataStateManager<KEY>, FlowAccessor<KEY> {

    @ExperimentalCoroutinesApi
    private val dataState: HashMap<KEY, MutableStateFlow<DataState>> = hashMapOf()

    @ExperimentalCoroutinesApi
    override fun getFlow(key: KEY): Flow<DataState> {
        return dataState.getOrCreate(key)
    }

    @ExperimentalCoroutinesApi
    override fun load(key: KEY): DataState {
        return dataState.getOrCreate(key).value
    }

    @ExperimentalCoroutinesApi
    override fun save(key: KEY, state: DataState) {
        dataState.getOrCreate(key).value = state
    }

    @ExperimentalCoroutinesApi
    private fun <KEY> HashMap<KEY, MutableStateFlow<DataState>>.getOrCreate(key: KEY): MutableStateFlow<DataState> {
        return getOrPut(key, { MutableStateFlow(DataState.Fixed()) })
    }

    @ExperimentalCoroutinesApi
    fun clearAll() {
        dataState.clear()
    }

}