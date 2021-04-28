package com.kazakago.storeflowable.core

import kotlinx.coroutines.flow.Flow

/**
 * Type alias of `Flow<State<T>>`.
 */
typealias FlowableState<T> = Flow<State<T>>
