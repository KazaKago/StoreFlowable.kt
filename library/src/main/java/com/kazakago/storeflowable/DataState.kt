package com.kazakago.storeflowable

sealed class DataState {
    class Fixed(val noMoreAdditionalData: Boolean = false) : DataState()
    class Loading : DataState()
    class Error(val exception: Exception) : DataState()
}
