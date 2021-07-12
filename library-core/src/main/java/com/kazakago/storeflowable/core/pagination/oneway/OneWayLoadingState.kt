package com.kazakago.storeflowable.core.pagination.oneway

import com.kazakago.storeflowable.core.pagination.AdditionalLoadingState
import com.kazakago.storeflowable.core.pagination.oneway.OneWayLoadingState.*

/**
 * This sealed class that represents the state of the data.
 *
 * The following three states are shown.
 * - [Loading] is acquiring data.
 * - [Completed] has not been processed.
 * - [Error] is an error when processing.
 *
 * The entity of the data is stored in [StateContent] separately from this [OneWayLoadingState].
 *
 * @param T Types of data to be included.
 * @property content Indicates the existing or not existing of data by [StateContent].
 */
sealed interface OneWayLoadingState<out T> {

    /**
     * Acquiring data state.
     *
     * @param content Indicates the existing or not existing of data.
     */
    data class Loading<out T>(val content: T?) : OneWayLoadingState<T>

    /**
     * No processing state.
     *
     * @param content Indicates the existing.
     */
    data class Completed<out T>(val content: T, val appending: AdditionalLoadingState) : OneWayLoadingState<T>

    /**
     * An error when processing state.
     */
    data class Error<out T>(val exception: Exception) : OneWayLoadingState<T>

    /**
     * Provides state-specific callbacks.
     * Same as `when (state) { ... }`.
     *
     * @param onLoading Callback for [Loading].
     * @param onCompleted Callback for [Completed].
     * @param onError Callback for [Error].
     * @return Can return a value of any type.
     */
    fun <V> doAction(onLoading: ((content: T?) -> V), onCompleted: ((content: T, appending: AdditionalLoadingState) -> V), onError: ((exception: Exception) -> V)): V {
        return when (this) {
            is Loading -> onLoading(content)
            is Completed -> onCompleted(content, appending)
            is Error -> onError(exception)
        }
    }
}
