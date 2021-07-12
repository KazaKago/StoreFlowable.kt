package com.kazakago.storeflowable.core.pagination

/**
 * TODO
 */
fun AdditionalLoadingState.zip(additionalState2: AdditionalLoadingState): AdditionalLoadingState {
    return when (this) {
        is AdditionalLoadingState.Fixed -> when (additionalState2) {
            is AdditionalLoadingState.Fixed -> AdditionalLoadingState.Fixed(noMoreAdditionalData = noMoreAdditionalData && additionalState2.noMoreAdditionalData)
            is AdditionalLoadingState.Loading -> AdditionalLoadingState.Loading
            is AdditionalLoadingState.Error -> AdditionalLoadingState.Error(additionalState2.exception)
        }
        is AdditionalLoadingState.Loading -> when (additionalState2) {
            is AdditionalLoadingState.Fixed -> AdditionalLoadingState.Loading
            is AdditionalLoadingState.Loading -> AdditionalLoadingState.Loading
            is AdditionalLoadingState.Error -> AdditionalLoadingState.Error(additionalState2.exception)
        }
        is AdditionalLoadingState.Error -> when (additionalState2) {
            is AdditionalLoadingState.Fixed -> AdditionalLoadingState.Error(exception)
            is AdditionalLoadingState.Loading -> AdditionalLoadingState.Error(exception)
            is AdditionalLoadingState.Error -> AdditionalLoadingState.Error(exception)
        }
    }
}
