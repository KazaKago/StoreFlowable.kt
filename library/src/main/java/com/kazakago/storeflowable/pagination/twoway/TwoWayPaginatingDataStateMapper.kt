package com.kazakago.storeflowable.pagination.twoway

import com.kazakago.storeflowable.core.pagination.twoway.TwoWayAdditionalState
import com.kazakago.storeflowable.core.pagination.twoway.TwoWayPaginatingState
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.toTwoWayPaginatingState(content: DATA?): TwoWayPaginatingState<DATA> {
    return when (this) {
        is DataState.Fixed -> when (appendingDataState) {
            is AdditionalDataState.Fixed, is AdditionalDataState.FixedWithNoMoreData -> if (content != null) {
                TwoWayPaginatingState.Completed(content)
            } else {
                TwoWayPaginatingState.Loading()
            }
            is AdditionalDataState.Loading -> if (content != null) {
                val appendingState = TwoWayAdditionalState.Loading
                when (prependingDataState) {
                    is AdditionalDataState.Fixed, is AdditionalDataState.FixedWithNoMoreData -> TwoWayPaginatingState.Addition(content, appendingState, TwoWayAdditionalState.Fixed)
                    is AdditionalDataState.Loading -> TwoWayPaginatingState.Addition(content, appendingState, TwoWayAdditionalState.Loading)
                    is AdditionalDataState.Error -> TwoWayPaginatingState.Addition(content, appendingState, TwoWayAdditionalState.Error(prependingDataState.exception))
                }
            } else {
                TwoWayPaginatingState.Loading()
            }
            is AdditionalDataState.Error -> if (content != null) {
                val prependingState = TwoWayAdditionalState.Error(appendingDataState.exception)
                when (prependingDataState) {
                    is AdditionalDataState.Fixed, is AdditionalDataState.FixedWithNoMoreData -> TwoWayPaginatingState.Addition(content, prependingState, TwoWayAdditionalState.Fixed)
                    is AdditionalDataState.Loading -> TwoWayPaginatingState.Addition(content, prependingState, TwoWayAdditionalState.Loading)
                    is AdditionalDataState.Error -> TwoWayPaginatingState.Addition(content, prependingState, TwoWayAdditionalState.Error(prependingDataState.exception))
                }
            } else {
                TwoWayPaginatingState.Error(appendingDataState.exception)
            }
        }
        is DataState.Loading -> if (content != null) {
            TwoWayPaginatingState.Refreshing(content)
        } else {
            TwoWayPaginatingState.Loading()
        }
        is DataState.Error -> TwoWayPaginatingState.Error(exception)
    }
}
