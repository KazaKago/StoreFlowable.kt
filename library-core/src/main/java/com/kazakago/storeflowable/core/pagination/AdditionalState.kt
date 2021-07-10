package com.kazakago.storeflowable.core.pagination

sealed interface AdditionalState {
    data class Fixed(val noMoreAdditionalData: Boolean) : AdditionalState
    object Loading : AdditionalState
    data class Error(val exception: Exception) : AdditionalState

    fun <V> doAction(onFixed: ((noMoreAdditionalData: Boolean) -> V), onLoading: (() -> V), onError: ((exception: Exception) -> V)): V {
        return when (this) {
            is Fixed -> onFixed(noMoreAdditionalData)
            is Loading -> onLoading()
            is Error -> onError(exception)
        }
    }
}
