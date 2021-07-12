package com.kazakago.storeflowable.core

import java.io.Serializable

/**
 * TODO
 */
sealed interface AdditionalLoadingState : Serializable {

    /**
     * TODO
     */
    data class Fixed(val noMoreAdditionalData: Boolean) : AdditionalLoadingState

    /**
     * TODO
     */
    object Loading : AdditionalLoadingState

    /**
     * TODO
     */
    data class Error(val exception: Exception) : AdditionalLoadingState

    /**
     * TODO
     */
    fun <V> doAction(onFixed: ((noMoreAdditionalData: Boolean) -> V), onLoading: (() -> V), onError: ((exception: Exception) -> V)): V {
        return when (this) {
            is Fixed -> onFixed(noMoreAdditionalData)
            is Loading -> onLoading()
            is Error -> onError(exception)
        }
    }
}
