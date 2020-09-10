package com.kazakago.cacheflowable

sealed class DataState {
    class Fixed(val isReachLast: Boolean = false) : DataState()
    class Loading : DataState()
    class Error(val exception: Exception) : DataState()
}
