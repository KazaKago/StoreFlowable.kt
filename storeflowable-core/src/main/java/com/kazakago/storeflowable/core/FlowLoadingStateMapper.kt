package com.kazakago.storeflowable.core

import kotlinx.coroutines.flow.map

/**
 * Use when mapping raw data in [FlowLoadingState].
 *
 * @param transform This callback that returns the result of transforming the data.
 * @return Return [FlowLoadingState] containing the transformed data.
 */
public fun <A, Z> FlowLoadingState<A>.mapContent(transform: suspend (content: A) -> Z): FlowLoadingState<Z> {
    return map {
        when (it) {
            is LoadingState.Loading -> LoadingState.Loading(if (it.content != null) transform(it.content) else null)
            is LoadingState.Completed -> LoadingState.Completed(transform(it.content), it.next, it.prev)
            is LoadingState.Error -> LoadingState.Error(it.exception)
        }
    }
}
