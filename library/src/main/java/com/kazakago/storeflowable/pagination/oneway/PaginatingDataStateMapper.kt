package com.kazakago.storeflowable.pagination.oneway

import com.kazakago.storeflowable.core.pagination.oneway.AdditionalState
import com.kazakago.storeflowable.core.pagination.oneway.PaginatingState
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.toPaginatingState(content: DATA?): PaginatingState<DATA> {
    return when (this) {
        is DataState.Fixed -> when (appendingDataState) {
            is AdditionalDataState.Fixed, is AdditionalDataState.FixedWithNoMoreData -> if (content != null) {
                PaginatingState.Completed(content)
            } else {
                PaginatingState.Loading()
            }
            is AdditionalDataState.Loading -> if (content != null) {
                PaginatingState.Addition(content, AdditionalState.Loading)
            } else {
                PaginatingState.Loading()
            }
            is AdditionalDataState.Error -> if (content != null) {
                PaginatingState.Addition(content, AdditionalState.Error(appendingDataState.exception))
            } else {
                PaginatingState.Error(appendingDataState.exception)
            }
        }
        is DataState.Loading -> if (content != null) {
            PaginatingState.Refreshing(content)
        } else {
            PaginatingState.Loading()
        }
        is DataState.Error -> PaginatingState.Error(exception)
    }
}
