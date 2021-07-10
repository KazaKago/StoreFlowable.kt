package com.kazakago.storeflowable.core

import java.io.Serializable

/**
 * TODO
 */
sealed interface AdditionalState : Serializable {

    /**
     * TODO
     */
    data class Fixed(val noMoreAdditionalData: Boolean) : AdditionalState

    /**
     * TODO
     */
    object Loading : AdditionalState

    /**
     * TODO
     */
    data class Error(val exception: Exception) : AdditionalState

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
