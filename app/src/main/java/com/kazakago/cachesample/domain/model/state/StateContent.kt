package com.kazakago.cachesample.domain.model.state

sealed class StateContent<out T> {
    data class Exist<out T>(val rawContent: T) : StateContent<T>()
    class NotExist<out T> : StateContent<T>()

    fun separate(exist: ((state: Exist<T>) -> Unit), notExist: ((state: NotExist<T>) -> Unit)) {
        when (this) {
            is Exist -> exist(this)
            is NotExist -> notExist(this)
        }
    }

}
