package com.kazakago.storeflowable.datastate

/**
 * Indicates the state of the data.
 *
 * This state is only used inside this library.
 */
@Suppress("CanSealedSubClassBeObject")
public sealed class DataState {
    internal class Fixed(override val nextDataState: AdditionalDataState, override val prevDataState: AdditionalDataState) : DataState()
    internal class Loading : DataState()
    internal class Error(val exception: Exception) : DataState()

    internal open val nextDataState: AdditionalDataState = AdditionalDataState.Fixed()
    internal open val prevDataState: AdditionalDataState = AdditionalDataState.Fixed()
}
