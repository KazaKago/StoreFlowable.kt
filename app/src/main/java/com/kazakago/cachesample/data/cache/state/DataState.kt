package com.kazakago.cachesample.data.cache.state

internal sealed class DataState {
    data class Fixed(val isReachLast: Boolean = false) : DataState()
    object Loading : DataState()
    data class Error(val exception: Exception) : DataState()
}
