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
sealed interface State<out T> {

    /**
     * Indicates the existing or not existing of data by [StateContent].
     */
    val content: StateContent<T>

    /**
     * No processing state.
     *
     * @param content Indicates the existing or not existing of data by [StateContent].
     */
    data class Fixed<out T>(override val content: StateContent<T>) : State<T>

    /**
     * Acquiring data state.
     *
     * @param content Indicates the existing or not existing of data by [StateContent].
     */
    data class Loading<out T>(override val content: StateContent<T>) : State<T>

    /**
     * An error when processing state.
     *
     * @param content Indicates the existing or not existing of data by [StateContent].
     */
    data class Error<out T>(override val content: StateContent<T>, val exception: Exception) : State<T>

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
