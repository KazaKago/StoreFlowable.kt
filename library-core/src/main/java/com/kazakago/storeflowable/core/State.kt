package com.kazakago.storeflowable.core

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
sealed class State<out T>(val content: StateContent<T>) {

    /**
     * No processing state.
     */
    class Fixed<out T>(content: StateContent<T>) : State<T>(content)

    /**
     * Acquiring data state.
     */
    class Loading<out T>(content: StateContent<T>) : State<T>(content)

    /**
     * An error when processing state.
     */
    class Error<out T>(content: StateContent<T>, val exception: Exception) : State<T>(content)

    /**
     * Provides state-specific callbacks.
     * Same as `when (state) { ... }`.
     *
     * @param onFixed Callback for [Fixed].
     * @param onLoading Callback for [Loading].
     * @param onError Callback for [Error].
     * @return Can return a value of any type.
     */
    fun <V> doAction(onFixed: (() -> V), onLoading: (() -> V), onError: ((exception: Exception) -> V)): V {
        return when (this) {
            is Fixed -> onFixed()
            is Loading -> onLoading()
            is Error -> onError(exception)
        }
    }
}
