package com.kazakago.storeflowable.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <A, Z> Flow<State<A>>.mapContent(transform: suspend (content: A) -> Z): Flow<State<Z>> {
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
