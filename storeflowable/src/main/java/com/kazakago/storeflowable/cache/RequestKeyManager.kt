package com.kazakago.storeflowable.cache

internal interface RequestKeyManager {

    suspend fun loadNext(): String?

    suspend fun saveNext(requestKey: String?)

    suspend fun loadPrev(): String?

    suspend fun savePrev(requestKey: String?)
}
