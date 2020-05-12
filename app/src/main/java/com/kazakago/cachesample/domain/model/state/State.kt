package com.kazakago.cachesample.domain.model.state

sealed class State<out T>(
    val content: StateContent<T>
) {
    class Fixed<out T>(content: StateContent<T>) : State<T>(content)
    class Loading<out T>(content: StateContent<T>) : State<T>(content)
    class Error<out T>(content: StateContent<T>, val exception: Exception) : State<T>(content)

    fun separate(fixed: ((state: Fixed<T>) -> Unit), loading: ((state: Loading<T>) -> Unit), error: ((state: Error<T>) -> Unit)) {
        when (this) {
            is Fixed -> fixed(this)
            is Loading -> loading(this)
            is Error -> error(this)
        }
    }

}
