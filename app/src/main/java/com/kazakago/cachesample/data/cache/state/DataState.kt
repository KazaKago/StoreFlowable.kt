package com.kazakago.cachesample.data.cache.state

import kotlinx.coroutines.channels.ConflatedBroadcastChannel

sealed class DataState {
    object Fixed : DataState()
    object Loading : DataState()
    data class Error(val exception: Exception) : DataState()
}

fun HashMap<String, ConflatedBroadcastChannel<DataState>>.getOrCreate(key: String): ConflatedBroadcastChannel<DataState> {
    return getOrPut(key, { ConflatedBroadcastChannel(DataState.Fixed) })
}
