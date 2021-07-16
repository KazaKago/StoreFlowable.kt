package com.kazakago.storeflowable.core

import com.kazakago.storeflowable.core.AdditionalLoadingState.*
import java.io.Serializable

/**
 * This sealed class that represents the state of the additional pagination data.
 *
 * The following three states are shown.
 * - [Fixed] has not been processed.
 * - [Loading] is acquiring data.
 * - [Error] is an error when processing.
 */
sealed interface AdditionalLoadingState : Serializable {

    /**
     * No processing.
     *
     * @param canRequestAdditionalData Whether additional fetching is possible from the origin.
     */
    data class Fixed(val canRequestAdditionalData: Boolean) : AdditionalLoadingState

    /**
     * when data fetch is processing.
     */
    object Loading : AdditionalLoadingState

    /**
     * when data fetch is failure.
     *
     * @param exception Occurred exception.
     */
    data class Error(val exception: Exception) : AdditionalLoadingState

    /**
     * Provides state-specific callbacks.
     * Same as `when (state) { ... }`.
     *
     * @param onFixed Callback for [onFixed].
     * @param onLoading Callback for [Loading].
     * @param onError Callback for [Error].
     * @return Can return a value of any type.
     */
    fun <V> doAction(onFixed: ((canRequestAdditionalData: Boolean) -> V), onLoading: (() -> V), onError: ((exception: Exception) -> V)): V {
        return when (this) {
            is Fixed -> onFixed(canRequestAdditionalData)
            is Loading -> onLoading()
            is Error -> onError(exception)
        }
    }
}
