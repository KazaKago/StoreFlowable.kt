package com.kazakago.storeflowable.core

/**
 * This sealed class that indicates existing or not existing of data.
 *
 * @param T Types of data to be included.
 */
sealed interface StateContent<out T> {

    /**
     * Data is exists.
     *
     * @property rawContent Included raw content.
     */
    data class Exist<out T>(val rawContent: T) : StateContent<T>

    /**
     * Data does not exist.
     */
    class NotExist<out T> : StateContent<T>

    /**
     * Provides callbacks existing or not existing data.
     * Same as `when (stateContent) { ... }`.
     *
     * @param onExist Callback for [Exist].
     * @param onNotExist Callback for [NotExist].
     * @return Can return a value of any type.
     */
    fun <V> doAction(onExist: ((rawContent: T) -> V), onNotExist: (() -> V)): V {
        return when (this) {
            is Exist -> onExist(this.rawContent)
            is NotExist -> onNotExist()
        }
    }

    companion object {
        /**
         * Create [StateContent] based on nullable data.
         *
         * @param rawContent Raw entity of data.
         * @return Created [StateContent].
         */
        fun <T> wrap(rawContent: T?): StateContent<T> {
            return if (rawContent == null) {
                NotExist<T>()
            } else {
                Exist(rawContent)
            }
        }
    }
}
