package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.AdditionalState
import com.kazakago.storeflowable.core.State
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.toState(content: DATA?): State<DATA> {
    return when (this) {
        is DataState.Fixed -> when (appendingDataState) {
            is AdditionalDataState.Fixed -> if (content != null) {
                val appendingState = AdditionalState.Fixed(noMoreAdditionalData = false)
                when (prependingDataState) {
                    is AdditionalDataState.Fixed -> State.Completed(content, appendingState, AdditionalState.Fixed(noMoreAdditionalData = false))
                    is AdditionalDataState.FixedWithNoMoreAdditionalData -> State.Completed(content, appendingState, AdditionalState.Fixed(noMoreAdditionalData = true))
                    is AdditionalDataState.Loading -> State.Completed(content, appendingState, AdditionalState.Loading)
                    is AdditionalDataState.Error -> State.Completed(content, appendingState, AdditionalState.Error(prependingDataState.exception))
                }
            } else {
                State.Loading(content)
            }
            is AdditionalDataState.FixedWithNoMoreAdditionalData -> if (content != null) {
                val appendingState = AdditionalState.Fixed(noMoreAdditionalData = true)
                when (prependingDataState) {
                    is AdditionalDataState.Fixed -> State.Completed(content, appendingState, AdditionalState.Fixed(noMoreAdditionalData = false))
                    is AdditionalDataState.FixedWithNoMoreAdditionalData -> State.Completed(content, appendingState, AdditionalState.Fixed(noMoreAdditionalData = true))
                    is AdditionalDataState.Loading -> State.Completed(content, appendingState, AdditionalState.Loading)
                    is AdditionalDataState.Error -> State.Completed(content, appendingState, AdditionalState.Error(prependingDataState.exception))
                }
            } else {
                State.Loading(content)
            }
            is AdditionalDataState.Loading -> if (content != null) {
                val appendingState = AdditionalState.Loading
                when (prependingDataState) {
                    is AdditionalDataState.Fixed -> State.Completed(content, appendingState, AdditionalState.Fixed(noMoreAdditionalData = false))
                    is AdditionalDataState.FixedWithNoMoreAdditionalData -> State.Completed(content, appendingState, AdditionalState.Fixed(noMoreAdditionalData = true))
                    is AdditionalDataState.Loading -> State.Completed(content, appendingState, AdditionalState.Loading)
                    is AdditionalDataState.Error -> State.Completed(content, appendingState, AdditionalState.Error(prependingDataState.exception))
                }
            } else {
                State.Loading(content)
            }
            is AdditionalDataState.Error -> if (content != null) {
                val prependingState = AdditionalState.Error(appendingDataState.exception)
                when (prependingDataState) {
                    is AdditionalDataState.Fixed -> State.Completed(content, prependingState, AdditionalState.Fixed(noMoreAdditionalData = false))
                    is AdditionalDataState.FixedWithNoMoreAdditionalData -> State.Completed(content, prependingState, AdditionalState.Fixed(noMoreAdditionalData = true))
                    is AdditionalDataState.Loading -> State.Completed(content, prependingState, AdditionalState.Loading)
                    is AdditionalDataState.Error -> State.Completed(content, prependingState, AdditionalState.Error(prependingDataState.exception))
                }
            } else {
                State.Error(appendingDataState.exception)
            }
        }
        is DataState.Loading -> State.Loading(content)
        is DataState.Error -> State.Error(exception)
    }
}
