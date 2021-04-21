package com.kazakago.storeflowable.core

import com.os.operando.guild.kt.to
import kotlinx.coroutines.flow.combine

/**
 * Combine multiple [FlowableState].
 *
 * @param otherFlow The second [FlowableState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableState] containing the combined data.
 */
fun <A, B, Z> FlowableState<A>.combineState(otherFlow: FlowableState<B>, transform: (content1: A, content2: B) -> Z): FlowableState<Z> {
    return combine(otherFlow) { state1, state2 ->
        state1.zip(state2) { content1, content2 ->
            transform(content1, content2)
        }
    }
}

/**
 * Combine multiple [FlowableState].
 *
 * @param otherFlow1 The second [FlowableState] to combine.
 * @param otherFlow2 The third [FlowableState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, Z> FlowableState<A>.combineState(otherFlow1: FlowableState<B>, otherFlow2: FlowableState<C>, transform: (content1: A, content2: B, content3: C) -> Z): FlowableState<Z> {
    return combineState(otherFlow1) { content1, content2 ->
        content1 to content2
    }.combineState(otherFlow2) { content1_2, content3 ->
        transform(content1_2.first, content1_2.second, content3)
    }
}

/**
 * Combine multiple [FlowableState].
 *
 * @param otherFlow1 The second [FlowableState] to combine.
 * @param otherFlow2 The third [FlowableState] to combine.
 * @param otherFlow3 The fourth [FlowableState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, D, Z> FlowableState<A>.combineState(otherFlow1: FlowableState<B>, otherFlow2: FlowableState<C>, otherFlow3: FlowableState<D>, transform: (content1: A, content2: B, content3: C, content4: D) -> Z): FlowableState<Z> {
    return combineState(otherFlow1) { content1, content2 ->
        content1 to content2
    }.combineState(otherFlow2) { content1_2, content3 ->
        content1_2.first to content1_2.second to content3
    }.combineState(otherFlow3) { content1_2_3, content4 ->
        transform(content1_2_3.first, content1_2_3.second, content1_2_3.third, content4)
    }
}

/**
 * Combine multiple [FlowableState].
 *
 * @param otherFlow1 The second [FlowableState] to combine.
 * @param otherFlow2 The third [FlowableState] to combine.
 * @param otherFlow3 The fourth [FlowableState] to combine.
 * @param otherFlow4 The fifth [FlowableState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, D, E, Z> FlowableState<A>.combineState(otherFlow1: FlowableState<B>, otherFlow2: FlowableState<C>, otherFlow3: FlowableState<D>, otherFlow4: FlowableState<E>, transform: (content1: A, content2: B, content3: C, content4: D, content5: E) -> Z): FlowableState<Z> {
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
