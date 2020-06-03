package com.kazakago.cachesample.data.cache.state

sealed class PagingDataState {
    data class Fixed(val isReachLast: Boolean) : PagingDataState()
    object Loading : PagingDataState()
    data class Error(val exception: Exception) : PagingDataState()
}
