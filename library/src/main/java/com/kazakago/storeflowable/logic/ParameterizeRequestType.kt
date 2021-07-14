package com.kazakago.storeflowable.logic

internal sealed interface ParameterizeRequestType {
    object Refresh : ParameterizeRequestType
    data class Next(val requestKey: String) : ParameterizeRequestType
    data class Prev(val requestKey: String) : ParameterizeRequestType
}
