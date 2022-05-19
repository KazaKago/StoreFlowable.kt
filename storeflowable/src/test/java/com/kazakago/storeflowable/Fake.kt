package com.kazakago.storeflowable

import com.kazakago.storeflowable.datastate.AdditionalDataState

internal fun fakeException() = NoSuchElementException()
internal fun fakeAdditionalDataState(): AdditionalDataState = AdditionalDataState.Fixed()
