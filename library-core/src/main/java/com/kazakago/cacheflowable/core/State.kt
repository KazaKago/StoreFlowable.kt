package com.kazakago.cacheflowable.core

sealed class State<out T>(
    val content: StateContent<T>
) {
    class Fixed<out T>(content: StateContent<T>) : State<T>(content)
    class Loading<out T>(content: StateContent<T>) : State<T>(content)
    class Error<out T>(content: StateContent<T>, val exception: Exception) : State<T>(content)

    fun <V> doAction(onFixed: (() -> V), onLoading: (() -> V), onError: ((exception: Exception) -> V)): V {
        return when (this) {
            is Fixed -> onFixed()
            is Loading -> onLoading()
            is Error -> onError(exception)
        }
    }

}
