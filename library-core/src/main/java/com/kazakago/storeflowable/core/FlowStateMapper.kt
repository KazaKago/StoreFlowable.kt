package com.kazakago.storeflowable.core

import kotlinx.coroutines.flow.map

/**
 * Use when mapping raw data in [FlowableState].
 *
 * @param transform This callback that returns the result of transforming the data.
 * @return Return [FlowableState] containing the transformed data.
 */
fun <A, Z> FlowableState<A>.mapContent(transform: suspend (content: A) -> Z): FlowableState<Z> {
    return map {
        when (it) {
            is State.Loading -> State.Loading(if (it.content != null) transform(it.content) else null)
            is State.Completed -> State.Completed(transform(it.content), it.appending, it.prepending)
            is State.Error -> State.Error(it.exception)
        }
    }
}
