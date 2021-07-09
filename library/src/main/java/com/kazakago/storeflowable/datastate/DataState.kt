package com.kazakago.storeflowable.datastate

/**
 * Indicates the state of the data.
 *
 * This state is only used inside the [com.kazakago.storeflowable.StoreFlowable].
 */
@Suppress("CanSealedSubClassBeObject")
sealed class DataState {
    internal class Fixed(val appendingState: AdditionalDataState, val prependingState: AdditionalDataState) : DataState()
    internal class Loading : DataState()
    internal class Error(val exception: Exception) : DataState()

    internal fun appendingState(): AdditionalDataState {
        return when (this) {
            is Fixed -> appendingState
            is Loading -> AdditionalDataState.Fixed()
            is Error -> AdditionalDataState.Fixed()
        }
    }

    internal fun prependingState(): AdditionalDataState {
        return when (this) {
            is Fixed -> prependingState
            is Loading -> AdditionalDataState.Fixed()
            is Error -> AdditionalDataState.Fixed()
        }
    }
}
