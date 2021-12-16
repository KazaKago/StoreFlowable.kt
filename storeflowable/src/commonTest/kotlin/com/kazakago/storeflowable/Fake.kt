package com.kazakago.storeflowable

import com.kazakago.storeflowable.datastate.AdditionalDataState

fun fakeException() = NoSuchElementException()
fun fakeAdditionalDataState(): AdditionalDataState = AdditionalDataState.Fixed("")
