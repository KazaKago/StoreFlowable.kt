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
 * @param T Types of data to be included.
 */
sealed interface OneWayLoadingState<out T> {

    /**
     * when data fetch is processing.
     *
     * @param content Indicates the existing or not existing of data.
     */
    data class Loading<out T>(val content: T?) : OneWayLoadingState<T>

    /**
     * When data fetch is successful.
     *
     * @param content Raw data.
     * @param appending appending pagination state of the data.
     */
    data class Completed<out T>(val content: T, val appending: AdditionalLoadingState) : OneWayLoadingState<T>

    /**
     * when data fetch is failure.
     *
     * @param exception Occurred exception.
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
