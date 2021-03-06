package com.kazakago.storeflowable

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

abstract class FlowableDataStateManager<KEY> : DataStateManager<KEY>, FlowAccessor<KEY> {

    private val dataState = mutableMapOf<KEY, MutableStateFlow<DataState>>()

    override fun getFlow(key: KEY): Flow<DataState> {
        return dataState.getOrCreate(key)
    }

    override fun loadState(key: KEY): DataState {
        return dataState.getOrCreate(key).value
    }

    override fun saveState(key: KEY, state: DataState) {
        dataState.getOrCreate(key).value = state
    }

    private fun <KEY> MutableMap<KEY, MutableStateFlow<DataState>>.getOrCreate(key: KEY): MutableStateFlow<DataState> {
        return getOrPut(key, { MutableStateFlow(DataState.Fixed()) })
    }

    fun clearAll() {
        dataState.clear()
    }
}
