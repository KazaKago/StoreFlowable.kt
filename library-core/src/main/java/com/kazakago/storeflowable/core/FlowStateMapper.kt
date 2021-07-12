package com.kazakago.storeflowable.core

import kotlinx.coroutines.flow.map

/**
 * Use when mapping raw data in [FlowableLoadingState].
 *
 * @param transform This callback that returns the result of transforming the data.
 * @return Return [FlowableLoadingState] containing the transformed data.
 */
fun <A, Z> FlowableLoadingState<A>.mapContent(transform: suspend (content: A) -> Z): FlowableLoadingState<Z> {
    return map {
        when (it) {
            is LoadingState.Loading -> LoadingState.Loading(if (it.content != null) transform(it.content) else null)
            is LoadingState.Completed -> LoadingState.Completed(transform(it.content), it.appending, it.prepending)
            is LoadingState.Error -> LoadingState.Error(it.exception)
        }
    }
}
