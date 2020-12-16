package com.kazakago.storeflowable

sealed class DataState {

    class Fixed(val isReachLast: Boolean = false) : DataState()

    class Loading : DataState()

    class Error(val exception: Exception) : DataState()
}
