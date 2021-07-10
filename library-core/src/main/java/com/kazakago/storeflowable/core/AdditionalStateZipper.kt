package com.kazakago.storeflowable.core

fun AdditionalState.zip(additionalState2: AdditionalState): AdditionalState {
    return when (this) {
        is AdditionalState.Fixed -> when (additionalState2) {
            is AdditionalState.Fixed -> AdditionalState.Fixed(noMoreAdditionalData = noMoreAdditionalData && additionalState2.noMoreAdditionalData)
            is AdditionalState.Loading -> AdditionalState.Loading
            is AdditionalState.Error -> AdditionalState.Error(additionalState2.exception)
        }
        is AdditionalState.Loading -> when (additionalState2) {
            is AdditionalState.Fixed -> AdditionalState.Loading
            is AdditionalState.Loading -> AdditionalState.Loading
            is AdditionalState.Error -> AdditionalState.Error(additionalState2.exception)
        }
        is AdditionalState.Error -> when (additionalState2) {
            is AdditionalState.Fixed -> AdditionalState.Error(exception)
            is AdditionalState.Loading -> AdditionalState.Error(exception)
            is AdditionalState.Error -> AdditionalState.Error(exception)
        }
    }
}
