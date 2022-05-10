package com.kazakago.storeflowable.cache

internal interface RequestKeyManager<PARAM> {

    fun loadNext(param: PARAM): String?

    fun saveNext(param: PARAM, requestKey: String?)

    fun loadPrev(param: PARAM): String?

    fun savePrev(param: PARAM, requestKey: String?)
}
