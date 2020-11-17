package com.kazakago.storeflowable.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <A, Z> Flow<State<A>>.mapContent(transform: suspend (content: A) -> Z): Flow<State<Z>> {
    return map {
        val stateContent = when (val stateContent = it.content) {
            is StateContent.Exist -> StateContent.Exist(transform(stateContent.rawContent))
            is StateContent.NotExist -> StateContent.NotExist<Z>()
        }
        when (it) {
            is State.Fixed -> State.Fixed(stateContent)
            is State.Loading -> State.Loading(stateContent)
            is State.Error -> State.Error(stateContent, it.exception)
        }
    }
}
