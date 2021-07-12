package com.kazakago.storeflowable.datastate

/**
 * Indicates the state of the data.
 *
 * This state is only used inside this library.
 */
@Suppress("CanSealedSubClassBeObject")
sealed class DataState {
    internal class Fixed(val appendingDataState: AdditionalDataState, val prependingDataState: AdditionalDataState) : DataState()
    internal class Loading : DataState()
    internal class Error(val exception: Exception) : DataState()

    internal fun appendingDataState(): AdditionalDataState {
        return when (this) {
            is Fixed -> appendingDataState
            is Loading -> AdditionalDataState.Fixed()
            is Error -> AdditionalDataState.Fixed()
        }
    }

    internal fun prependingDataState(): AdditionalDataState {
        return when (this) {
            is Fixed -> prependingDataState
            is Loading -> AdditionalDataState.Fixed()
            is Error -> AdditionalDataState.Fixed()
        }
    }
}
