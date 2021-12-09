package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.core.AdditionalLoadingState
import com.kazakago.storeflowable.core.LoadingState
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.toLoadingState(content: DATA?): LoadingState<DATA> {
    return when (this) {
        is DataState.Fixed -> if (content != null) {
            when (nextDataState) {
                is AdditionalDataState.Fixed -> {
                    val nextState = AdditionalLoadingState.Fixed(canRequestAdditionalData = true)
                    when (prevDataState) {
                        is AdditionalDataState.Fixed -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(canRequestAdditionalData = true))
                        is AdditionalDataState.FixedWithNoMoreAdditionalData -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(canRequestAdditionalData = false))
                        is AdditionalDataState.Loading -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Error(prevDataState.exception))
                    }
                }
                is AdditionalDataState.FixedWithNoMoreAdditionalData -> {
                    val nextState = AdditionalLoadingState.Fixed(canRequestAdditionalData = false)
                    when (prevDataState) {
                        is AdditionalDataState.Fixed -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(canRequestAdditionalData = true))
                        is AdditionalDataState.FixedWithNoMoreAdditionalData -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(canRequestAdditionalData = false))
                        is AdditionalDataState.Loading -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Error(prevDataState.exception))
                    }
                }
                is AdditionalDataState.Loading -> {
                    val nextState = AdditionalLoadingState.Loading
                    when (prevDataState) {
                        is AdditionalDataState.Fixed -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(canRequestAdditionalData = true))
                        is AdditionalDataState.FixedWithNoMoreAdditionalData -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(canRequestAdditionalData = false))
                        is AdditionalDataState.Loading -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Error(prevDataState.exception))
                    }
                }
                is AdditionalDataState.Error -> {
                    val nextState = AdditionalLoadingState.Error(nextDataState.exception)
                    when (prevDataState) {
                        is AdditionalDataState.Fixed -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(canRequestAdditionalData = true))
                        is AdditionalDataState.FixedWithNoMoreAdditionalData -> LoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(canRequestAdditionalData = false))
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
