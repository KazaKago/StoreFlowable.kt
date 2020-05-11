package com.kazakago.cachesample.data.cache.state

import kotlinx.coroutines.flow.MutableStateFlow

sealed class DataState {
    object Fixed : DataState()
    object Loading : DataState()
    data class Error(val exception: Exception) : DataState()
}

fun HashMap<String, MutableStateFlow<DataState>>.getOrCreate(key: String): MutableStateFlow<DataState> {
    return getOrPut(key, { MutableStateFlow(DataState.Fixed) })
}
