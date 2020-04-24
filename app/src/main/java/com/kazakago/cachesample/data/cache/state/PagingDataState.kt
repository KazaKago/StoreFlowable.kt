package com.kazakago.cachesample.data.cache.state

import kotlinx.coroutines.channels.ConflatedBroadcastChannel

sealed class PagingDataState {
    data class Fixed(val isReachLast: Boolean) : PagingDataState()
    object Loading : PagingDataState()
    data class Error(val exception: Exception) : PagingDataState()
}

fun HashMap<String, ConflatedBroadcastChannel<PagingDataState>>.getOrCreate(key: String): ConflatedBroadcastChannel<PagingDataState> {
    return getOrPut(key, { ConflatedBroadcastChannel(PagingDataState.Fixed(false)) })
}
