package com.kazakago.storeflowable.core

import com.os.operando.guild.kt.to
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

fun <A, B, Z> Flow<State<A>>.combineState(otherFlow: Flow<State<B>>, transform: (content1: A, content2: B) -> Z): Flow<State<Z>> {
    return combine(otherFlow) { state1, state2 ->
        state1.zip(state2) { content1, content2 ->
            transform(content1, content2)
        }
    }
}

fun <A, B, C, Z> Flow<State<A>>.combineState(otherFlow1: Flow<State<B>>, otherFlow2: Flow<State<C>>, transform: (content1: A, content2: B, content3: C) -> Z): Flow<State<Z>> {
    return combineState(otherFlow1) { content1, content2 ->
        content1 to content2
    }.combineState(otherFlow2) { content1_2, content3 ->
        transform(content1_2.first, content1_2.second, content3)
    }
}

fun <A, B, C, D, Z> Flow<State<A>>.combineState(otherFlow1: Flow<State<B>>, otherFlow2: Flow<State<C>>, otherFlow3: Flow<State<D>>, transform: (content1: A, content2: B, content3: C, content4: D) -> Z): Flow<State<Z>> {
    return combineState(otherFlow1) { content1, content2 ->
        content1 to content2
    }.combineState(otherFlow2) { content1_2, content3 ->
        content1_2.first to content1_2.second to content3
    }.combineState(otherFlow3) { content1_2_3, content4 ->
        transform(content1_2_3.first, content1_2_3.second, content1_2_3.third, content4)
    }
}

fun <A, B, C, D, E, Z> Flow<State<A>>.combineState(otherFlow1: Flow<State<B>>, otherFlow2: Flow<State<C>>, otherFlow3: Flow<State<D>>, otherFlow4: Flow<State<E>>, transform: (content1: A, content2: B, content3: C, content4: D, content5: E) -> Z): Flow<State<Z>> {
    return combineState(otherFlow1) { content1, content2 ->
        content1 to content2
    }.combineState(otherFlow2) { content1_2, content3 ->
        content1_2.first to content1_2.second to content3
    }.combineState(otherFlow3) { content1_2_3, content4 ->
        content1_2_3.first to content1_2_3.second to content1_2_3.third to content4
    }.combineState(otherFlow4) { content1_2_3_4, content5 ->
        transform(content1_2_3_4.first, content1_2_3_4.second, content1_2_3_4.third, content1_2_3_4.fourth, content5)
    }
}
