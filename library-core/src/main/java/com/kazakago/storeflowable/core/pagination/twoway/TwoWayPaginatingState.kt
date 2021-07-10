package com.kazakago.storeflowable.core.pagination.twoway

import com.kazakago.storeflowable.core.pagination.twoway.TwoWayPaginatingState.Error
import com.kazakago.storeflowable.core.pagination.twoway.TwoWayPaginatingState.Loading

/**
 * This sealed class that represents the state of the data.
 *
 * The following three states are shown.
 * - [Loading] is acquiring data.
 * - [Completed] has not been processed.
 * - [Error] is an error when processing.
 *
 * The entity of the data is stored in [StateContent] separately from this [TwoWayPaginatingState].
 *
 * @param T Types of data to be included.
 * @property content Indicates the existing or not existing of data by [StateContent].
 */
sealed interface TwoWayPaginatingState<out T> {

    /**
     * Acquiring data state.
     *
     * @param content Indicates the existing or not existing of data by [StateContent].
     */
    class Loading<out T> : TwoWayPaginatingState<T>

    /**
     * TODO
     */
    data class Refreshing<out T>(val content: T) : TwoWayPaginatingState<T>

    /**
     * No processing state.
     *
     * @param content Indicates the existing or not existing of data by [StateContent].
     */
    data class Completed<out T>(val content: T) : TwoWayPaginatingState<T>

    /**
     * An error when processing state.
     *
     * @param content Indicates the existing or not existing of data by [StateContent].
     */
    data class Error<out T>(val exception: Exception) : TwoWayPaginatingState<T>

    /**
     * TODO
     */
    data class Addition<out T>(val content: T, val appending: TwoWayAdditionalState, val prepending: TwoWayAdditionalState) : TwoWayPaginatingState<T>

    /**
     * Provides state-specific callbacks.
     * Same as `when (state) { ... }`.
     *
     * @param onLoading Callback for [Loading].
     * @param onCompleted Callback for [Completed].
     * @param onError Callback for [Error].
     * @return Can return a value of any type.
     */
    fun <V> doAction(onLoading: (() -> V), onRefreshing: ((content: T) -> V), onCompleted: ((content: T) -> V), onError: ((exception: Exception) -> V), onAddition: (content: T, appending: TwoWayAdditionalState, prepending: TwoWayAdditionalState) -> V): V {
        return when (this) {
            is Loading -> onLoading()
            is Refreshing -> onRefreshing(content)
            is Completed -> onCompleted(content)
            is Error -> onError(exception)
            is Addition -> onAddition(content, appending, prepending)
        }
    }
}
