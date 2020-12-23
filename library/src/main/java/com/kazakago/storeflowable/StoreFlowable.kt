package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.State
import kotlinx.coroutines.flow.Flow

interface StoreFlowable<KEY, DATA> {

    fun asFlow(forceRefresh: Boolean = false): Flow<State<DATA>>

    suspend fun get(type: AsDataType = AsDataType.Mix): DATA

    suspend fun validate()

    suspend fun request()

    suspend fun update(newData: DATA?)
}
