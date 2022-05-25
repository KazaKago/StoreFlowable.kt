package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.core.AdditionalLoadingState
import com.kazakago.storeflowable.core.LoadingState
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.toLoadingState(content: DATA?, canNextRequest: Boolean, canPrevRequest: Boolean): LoadingState<DATA> {
    return when (this) {
        is DataState.Fixed -> if (content != null) {
            when (val nextDataState = nextDataState) {
                is AdditionalDataState.Fixed -> {
                    val nextState = AdditionalLoadingState.Fixed(canRequestAdditionalData = canNextRequest)
                    when (val prevDataState = prevDataState) {
                        is AdditionalDataState.Fixed -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(canRequestAdditionalData = canPrevRequest))
                        is AdditionalDataState.Loading -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Error(prevDataState.exception))
                    }
                }
                is AdditionalDataState.Loading -> {
                    val nextState = AdditionalLoadingState.Loading
                    when (val prevDataState = prevDataState) {
                        is AdditionalDataState.Fixed -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(canRequestAdditionalData = canPrevRequest))
                        is AdditionalDataState.Loading -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Error(prevDataState.exception))
                    }
                }
                is AdditionalDataState.Error -> {
                    val nextState = AdditionalLoadingState.Error(nextDataState.exception)
                    when (val prevDataState = prevDataState) {
                        is AdditionalDataState.Fixed -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(canRequestAdditionalData = canPrevRequest))
                        is AdditionalDataState.Loading -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Error(prevDataState.exception))
                    }
                }
            }
        } else {
            LoadingState.Loading(content)
        }
        is DataState.Loading -> LoadingState.Loading(content)
        is DataState.Error -> LoadingState.Error(exception)
    }
}
