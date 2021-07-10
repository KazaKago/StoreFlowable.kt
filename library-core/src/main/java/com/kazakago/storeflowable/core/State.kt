package com.kazakago.storeflowable.core

import com.kazakago.storeflowable.core.State.Error
import com.kazakago.storeflowable.core.State.Loading
import java.io.Serializable

/**
 * This sealed class that represents the state of the data.
 *
 * The following three states are shown.
 * - [Fixed] has not been processed.
 * - [Loading] is acquiring data.
 * - [Error] is an error when processing.
 *
 * The entity of the data is stored in [StateContent] separately from this [State].
 *
 * @param T Types of data to be included.
 * @property content Indicates the existing or not existing of data by [StateContent].
 */
sealed interface State<out T> : Serializable {

    /**
     * Acquiring data state.
     *
     * @param content Indicates the existing or not existing of data by [StateContent].
     */
    data class Loading<out T>(val content: T?) : State<T>

    /**
     * No processing state.
     *
     * @param content Indicates the existing or not existing of data.
     */
    data class Completed<out T>(val content: T, val appending: AdditionalState, val prepending: AdditionalState) : State<T>

    /**
     * An error when processing state.
     */
    data class Error<out T>(val exception: Exception) : State<T>

    /**
     * Provides state-specific callbacks.
     * Same as `when (state) { ... }`.
     *
     * @param onLoading Callback for [Loading].
     * @param onCompleted Callback for [Completed].
     * @param onError Callback for [Error].
     * @return Can return a value of any type.
     */
    fun <V> doAction(onLoading: ((content: T?) -> V), onCompleted: ((content: T, appending: AdditionalState, prepending: AdditionalState) -> V), onError: ((exception: Exception) -> V)): V {
        return when (this) {
            is Loading -> onLoading(content)
            is Completed -> onCompleted(content, appending, prepending)
            is Error -> onError(exception)
        }
    }
}
