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
        val content = when (val content = it.content) {
            is StateContent.Exist -> StateContent.Exist(transform(content.rawContent))
            is StateContent.NotExist -> StateContent.NotExist<Z>()
        }
        when (it) {
            is State.Fixed -> State.Fixed(content)
            is State.Loading -> State.Loading(content)
            is State.Error -> State.Error(content, it.exception)
        }
    }
}
