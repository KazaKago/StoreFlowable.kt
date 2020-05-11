package com.kazakago.cachesample.data.cache.state

import kotlinx.coroutines.flow.MutableStateFlow

sealed class PagingDataState {
    data class Fixed(val isReachLast: Boolean) : PagingDataState()
    object Loading : PagingDataState()
    data class Error(val exception: Exception) : PagingDataState()
}

fun HashMap<String, MutableStateFlow<PagingDataState>>.getOrCreate(key: String): MutableStateFlow<PagingDataState> {
    return getOrPut(key, { MutableStateFlow(PagingDataState.Fixed(false)) })
}
