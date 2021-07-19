package com.kazakago.storeflowable.logic

internal sealed interface KeyedRequestType {
    object Refresh : KeyedRequestType
    data class Next(val requestKey: String) : KeyedRequestType
    data class Prev(val requestKey: String) : KeyedRequestType
}
