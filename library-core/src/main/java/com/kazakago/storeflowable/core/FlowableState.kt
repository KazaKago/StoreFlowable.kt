package com.kazakago.storeflowable.core

import kotlinx.coroutines.flow.Flow

typealias FlowableState<T> = Flow<State<T>>
