package com.kazakago.storeflowable.core.pagination.twoway

import com.kazakago.storeflowable.core.U
import kotlinx.coroutines.flow.combine

/**
 * Combine multiple [FlowableTwoWayLoadingState].
 *
 * @param flowableState2 The second [FlowableTwoWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableTwoWayLoadingState] containing the combined data.
 */
fun <A, B, Z> FlowableTwoWayLoadingState<A>.combineState(flowableState2: FlowableTwoWayLoadingState<B>, transform: (rawContent1: A, rawContent2: B) -> Z): FlowableTwoWayLoadingState<Z> {
    return combine(flowableState2) { state1, state2 ->
        state1.zip(state2) { rawContent, other ->
            transform(rawContent, other)
        }
    }
}

/**
 * Combine multiple [FlowableTwoWayLoadingState].
 *
 * @param flowableState2 The second [FlowableTwoWayLoadingState] to combine.
 * @param flowableState3 The third [FlowableTwoWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableTwoWayLoadingState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, Z> FlowableTwoWayLoadingState<A>.combineState(flowableState2: FlowableTwoWayLoadingState<B>, flowableState3: FlowableTwoWayLoadingState<C>, transform: (rawContent1: A, rawContent2: B, rawContent3: C) -> Z): FlowableTwoWayLoadingState<Z> {
    return combineState(flowableState2) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState3) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, other)
    }
}

/**
 * Combine multiple [FlowableTwoWayLoadingState].
 *
 * @param flowableState2 The second [FlowableTwoWayLoadingState] to combine.
 * @param flowableState3 The third [FlowableTwoWayLoadingState] to combine.
 * @param flowableState4 The fourth [FlowableTwoWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableTwoWayLoadingState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, D, Z> FlowableTwoWayLoadingState<A>.combineState(flowableState2: FlowableTwoWayLoadingState<B>, flowableState3: FlowableTwoWayLoadingState<C>, flowableState4: FlowableTwoWayLoadingState<D>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D) -> Z): FlowableTwoWayLoadingState<Z> {
    return combineState(flowableState2) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState3) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState4) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, other)
    }
}

/**
 * Combine multiple [FlowableTwoWayLoadingState].
 *
 * @param flowableState2 The second [FlowableTwoWayLoadingState] to combine.
 * @param flowableState3 The third [FlowableTwoWayLoadingState] to combine.
 * @param flowableState4 The fourth [FlowableTwoWayLoadingState] to combine.
 * @param flowableState5 The fifth [FlowableTwoWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableTwoWayLoadingState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, D, E, Z> FlowableTwoWayLoadingState<A>.combineState(flowableState2: FlowableTwoWayLoadingState<B>, flowableState3: FlowableTwoWayLoadingState<C>, flowableState4: FlowableTwoWayLoadingState<D>, flowableState5: FlowableTwoWayLoadingState<E>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D, rawContent5: E) -> Z): FlowableTwoWayLoadingState<Z> {
    return combineState(flowableState2) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState3) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState4) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState5) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, rawContent.t3, other)
    }
}
