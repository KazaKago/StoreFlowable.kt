package com.kazakago.storeflowable.core

import kotlinx.coroutines.flow.Flow

/**
 * Type alias of `Flow<LoadingState<T>>`.
 */
typealias FlowableLoadingState<T> = Flow<LoadingState<T>>
