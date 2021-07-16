package com.kazakago.storeflowable.logic

internal sealed interface KeyDataRequestType<DATA> {
    data class Refresh<DATA>(val cachedData: DATA?) : KeyDataRequestType<DATA>
    data class Next<DATA>(val requestKey: String, val cachedData: DATA) : KeyDataRequestType<DATA>
    data class Prev<DATA>(val requestKey: String, val cachedData: DATA) : KeyDataRequestType<DATA>
}
