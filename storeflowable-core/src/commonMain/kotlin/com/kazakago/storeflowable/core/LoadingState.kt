package com.kazakago.storeflowable.core

import com.kazakago.storeflowable.core.LoadingState.*

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
sealed class LoadingState<out T> {

    /**
     * when data fetch is processing.
     *
     * @param content Indicates the existing or not existing of data.
     */
    data class Loading<out T>(val content: T?) : LoadingState<T>()

    /**
     * When data fetch is successful.
     *
     * @param content Raw data.
     * @param next next pagination state of the data. If not pagination, Always returned [AdditionalLoadingState.Fixed] state.
     * @param prev prev pagination state of the data. If not pagination, Always returned [AdditionalLoadingState.Fixed] state.
     */
    data class Completed<out T>(val content: T, val next: AdditionalLoadingState, val prev: AdditionalLoadingState) : LoadingState<T>()

    /**
     * when data fetch is failure.
     *
     * @param exception Occurred exception.
     */
    data class Error<out T>(val exception: Exception) : LoadingState<T>()

    /**
     * Provides state-specific callbacks.
     * Same as `when (state) { ... }`.
     *
     * @param onLoading Callback for [Loading].
     * @param onCompleted Callback for [Completed].
     * @param onError Callback for [Error].
     * @return Can return a value of any type.
     */
    inline fun <V> doAction(onLoading: ((content: T?) -> V), onCompleted: ((content: T, next: AdditionalLoadingState, prev: AdditionalLoadingState) -> V), onError: ((exception: Exception) -> V)): V {
        return when (this) {
            is Loading -> onLoading(content)
            is Completed -> onCompleted(content, next, prev)
            is Error -> onError(exception)
        }
    }
}
