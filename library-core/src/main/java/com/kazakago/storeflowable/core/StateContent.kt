package com.kazakago.storeflowable.core

sealed class StateContent<out T> {

    data class Exist<out T>(val rawContent: T) : StateContent<T>()

    class NotExist<out T> : StateContent<T>()

    fun <V> doAction(onExist: ((rawContent: T) -> V), onNotExist: (() -> V)): V {
        return when (this) {
            is Exist -> onExist(this.rawContent)
            is NotExist -> onNotExist()
        }
    }

    companion object {
        fun <T> wrap(rawContent: T?): StateContent<T> {
            return if (rawContent == null) {
                NotExist<T>()
            } else {
                Exist(rawContent)
            }
        }
    }
}
