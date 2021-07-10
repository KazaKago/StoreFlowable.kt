package com.kazakago.storeflowable.pagination.twoway

import com.kazakago.storeflowable.core.pagination.AdditionalState
import com.kazakago.storeflowable.core.pagination.twoway.TwoWayPaginatingState
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.toTwoWayPaginatingState(content: DATA?): TwoWayPaginatingState<DATA> {
    return when (this) {
        is DataState.Fixed -> when (appendingDataState) {
            is AdditionalDataState.Fixed -> if (content != null) {
                val appendingState = AdditionalState.Fixed(noMoreAdditionalData = false)
                when (prependingDataState) {
                    is AdditionalDataState.Fixed -> TwoWayPaginatingState.Completed(content, appendingState, AdditionalState.Fixed(noMoreAdditionalData = false))
                    is AdditionalDataState.FixedWithNoMoreData -> TwoWayPaginatingState.Completed(content, appendingState, AdditionalState.Fixed(noMoreAdditionalData = true))
                    is AdditionalDataState.Loading -> TwoWayPaginatingState.Completed(content, appendingState, AdditionalState.Loading)
                    is AdditionalDataState.Error -> TwoWayPaginatingState.Completed(content, appendingState, AdditionalState.Error(prependingDataState.exception))
                }
            } else {
                TwoWayPaginatingState.Loading(content)
            }
            is AdditionalDataState.FixedWithNoMoreData -> if (content != null) {
                val appendingState = AdditionalState.Fixed(noMoreAdditionalData = true)
                when (prependingDataState) {
                    is AdditionalDataState.Fixed -> TwoWayPaginatingState.Completed(content, appendingState, AdditionalState.Fixed(noMoreAdditionalData = false))
                    is AdditionalDataState.FixedWithNoMoreData -> TwoWayPaginatingState.Completed(content, appendingState, AdditionalState.Fixed(noMoreAdditionalData = true))
                    is AdditionalDataState.Loading -> TwoWayPaginatingState.Completed(content, appendingState, AdditionalState.Loading)
                    is AdditionalDataState.Error -> TwoWayPaginatingState.Completed(content, appendingState, AdditionalState.Error(prependingDataState.exception))
                }
            } else {
                TwoWayPaginatingState.Loading(content)
            }
            is AdditionalDataState.Loading -> if (content != null) {
                val appendingState = AdditionalState.Loading
                when (prependingDataState) {
                    is AdditionalDataState.Fixed -> TwoWayPaginatingState.Completed(content, appendingState, AdditionalState.Fixed(noMoreAdditionalData = false))
                    is AdditionalDataState.FixedWithNoMoreData -> TwoWayPaginatingState.Completed(content, appendingState, AdditionalState.Fixed(noMoreAdditionalData = true))
                    is AdditionalDataState.Loading -> TwoWayPaginatingState.Completed(content, appendingState, AdditionalState.Loading)
                    is AdditionalDataState.Error -> TwoWayPaginatingState.Completed(content, appendingState, AdditionalState.Error(prependingDataState.exception))
                }
            } else {
                TwoWayPaginatingState.Loading(content)
            }
            is AdditionalDataState.Error -> if (content != null) {
                val prependingState = AdditionalState.Error(appendingDataState.exception)
                when (prependingDataState) {
                    is AdditionalDataState.Fixed -> TwoWayPaginatingState.Completed(content, prependingState, AdditionalState.Fixed(noMoreAdditionalData = false))
                    is AdditionalDataState.FixedWithNoMoreData -> TwoWayPaginatingState.Completed(content, prependingState, AdditionalState.Fixed(noMoreAdditionalData = true))
                    is AdditionalDataState.Loading -> TwoWayPaginatingState.Completed(content, prependingState, AdditionalState.Loading)
                    is AdditionalDataState.Error -> TwoWayPaginatingState.Completed(content, prependingState, AdditionalState.Error(prependingDataState.exception))
                }
            } else {
                TwoWayPaginatingState.Error(appendingDataState.exception)
            }
        }
        is DataState.Loading -> TwoWayPaginatingState.Loading(content)
        is DataState.Error -> TwoWayPaginatingState.Error(exception)
    }
}
