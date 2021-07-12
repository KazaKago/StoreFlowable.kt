package com.kazakago.storeflowable.core.pagination.oneway

import com.kazakago.storeflowable.core.U
import kotlinx.coroutines.flow.combine

/**
 * Combine multiple [FlowableOneWayLoadingState].
 *
 * @param flowableState2 The second [FlowableOneWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableOneWayLoadingState] containing the combined data.
 */
fun <A, B, Z> FlowableOneWayLoadingState<A>.combineState(flowableState2: FlowableOneWayLoadingState<B>, transform: (rawContent1: A, rawContent2: B) -> Z): FlowableOneWayLoadingState<Z> {
    return combine(flowableState2) { state1, state2 ->
        state1.zip(state2) { rawContent, other ->
            transform(rawContent, other)
        }
    }
}

/**
 * Combine multiple [FlowableOneWayLoadingState].
 *
 * @param flowableState2 The second [FlowableOneWayLoadingState] to combine.
 * @param flowableState3 The third [FlowableOneWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableOneWayLoadingState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, Z> FlowableOneWayLoadingState<A>.combineState(flowableState2: FlowableOneWayLoadingState<B>, flowableState3: FlowableOneWayLoadingState<C>, transform: (rawContent1: A, rawContent2: B, rawContent3: C) -> Z): FlowableOneWayLoadingState<Z> {
    return combineState(flowableState2) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState3) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, other)
    }
}

/**
 * Combine multiple [FlowableOneWayLoadingState].
 *
 * @param flowableState2 The second [FlowableOneWayLoadingState] to combine.
 * @param flowableState3 The third [FlowableOneWayLoadingState] to combine.
 * @param flowableState4 The fourth [FlowableOneWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableOneWayLoadingState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, D, Z> FlowableOneWayLoadingState<A>.combineState(flowableState2: FlowableOneWayLoadingState<B>, flowableState3: FlowableOneWayLoadingState<C>, flowableState4: FlowableOneWayLoadingState<D>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D) -> Z): FlowableOneWayLoadingState<Z> {
    return combineState(flowableState2) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState3) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState4) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, other)
    }
}

/**
 * Combine multiple [FlowableOneWayLoadingState].
 *
 * @param flowableState2 The second [FlowableOneWayLoadingState] to combine.
 * @param flowableState3 The third [FlowableOneWayLoadingState] to combine.
 * @param flowableState4 The fourth [FlowableOneWayLoadingState] to combine.
 * @param flowableState5 The fifth [FlowableOneWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableOneWayLoadingState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, D, E, Z> FlowableOneWayLoadingState<A>.combineState(flowableState2: FlowableOneWayLoadingState<B>, flowableState3: FlowableOneWayLoadingState<C>, flowableState4: FlowableOneWayLoadingState<D>, flowableState5: FlowableOneWayLoadingState<E>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D, rawContent5: E) -> Z): FlowableOneWayLoadingState<Z> {
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
