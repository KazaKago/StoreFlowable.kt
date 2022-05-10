package com.kazakago.storeflowable.core

import kotlinx.coroutines.flow.Flow

/**
 * Type alias of `Flow<LoadingState<T>>`.
 */
public typealias FlowLoadingState<T> = Flow<LoadingState<T>>
