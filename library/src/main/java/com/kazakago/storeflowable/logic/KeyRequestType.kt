package com.kazakago.storeflowable.logic

internal sealed interface KeyRequestType {
    object Refresh : KeyRequestType
    data class Next(val requestKey: String) : KeyRequestType
    data class Prev(val requestKey: String) : KeyRequestType
}
