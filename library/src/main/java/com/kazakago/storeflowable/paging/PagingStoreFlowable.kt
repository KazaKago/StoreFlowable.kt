package com.kazakago.storeflowable.paging

import com.kazakago.storeflowable.AsDataType
import com.kazakago.storeflowable.core.State
import kotlinx.coroutines.flow.Flow

interface PagingStoreFlowable<KEY, DATA> {

    fun asFlow(forceRefresh: Boolean = false): Flow<State<List<DATA>>>

    suspend fun get(type: AsDataType = AsDataType.Mix): List<DATA>

    suspend fun validate()

    suspend fun request(continueWhenError: Boolean = true)

    suspend fun requestAdditional(continueWhenError: Boolean = true)

    suspend fun update(newData: List<DATA>?)
}
