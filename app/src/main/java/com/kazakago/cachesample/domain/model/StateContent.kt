package com.kazakago.cachesample.domain.model

sealed class StateContent<out T> {
    data class Exist<out T>(val rawContent: T) : StateContent<T>()
    class NotExist<out T> : StateContent<T>()
}
