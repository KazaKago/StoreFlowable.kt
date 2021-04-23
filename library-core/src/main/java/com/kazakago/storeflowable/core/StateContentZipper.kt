package com.kazakago.storeflowable.core

/**
 * Combine multiple [StateContent].
 *
 * @param content2 The second [StateContent] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [StateContent] containing the combined data.
 */
fun <A, B, Z> StateContent<A>.zip(content2: StateContent<B>, transform: (rawContent1: A, rawContent2: B) -> Z): StateContent<Z> {
    return when (this) {
        is StateContent.Exist -> this.zip(content2, transform)
        is StateContent.NotExist -> this.zip(content2)
    }
}

/**
 * Combine multiple [StateContent].
 *
 * @param content2 The second [StateContent] to combine.
 * @param content3 The third [StateContent] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [StateContent] containing the combined data.
 */
fun <A, B, C, Z> StateContent<A>.zip(content2: StateContent<B>, content3: StateContent<C>, transform: (rawContent1: A, rawContent2: B, rawContent3: C) -> Z): StateContent<Z> {
    return zip(content2) { rawContent, other ->
        rawContent U other
    }.zip(content3) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, other)
    }
}

/**
 * Combine multiple [StateContent].
 *
 * @param content2 The second [StateContent] to combine.
 * @param content3 The third [StateContent] to combine.
 * @param content4 The fourth [StateContent] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [StateContent] containing the combined data.
 */
fun <A, B, C, D, Z> StateContent<A>.zip(content2: StateContent<B>, content3: StateContent<C>, content4: StateContent<D>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D) -> Z): StateContent<Z> {
    return zip(content2) { rawContent, other ->
        rawContent U other
    }.zip(content3) { rawContent, other ->
        rawContent U other
    }.zip(content4) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, other)
    }
}

/**
 * Combine multiple [StateContent].
 *
 * @param content2 The second [StateContent] to combine.
 * @param content3 The third [StateContent] to combine.
 * @param content4 The fourth [StateContent] to combine.
 * @param content5 The fifth [StateContent] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [StateContent] containing the combined data.
 */
fun <A, B, C, D, E, Z> StateContent<A>.zip(content2: StateContent<B>, content3: StateContent<C>, content4: StateContent<D>, content5: StateContent<E>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D, rawContent5: E) -> Z): StateContent<Z> {
    return zip(content2) { rawContent, other ->
        rawContent U other
    }.zip(content3) { rawContent, other ->
        rawContent U other
    }.zip(content4) { rawContent, other ->
        rawContent U other
    }.zip(content5) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, rawContent.t3, other)
    }
}

private fun <A, B, Z> StateContent.Exist<A>.zip(content2: StateContent<B>, transform: (rawContent1: A, rawContent2: B) -> Z): StateContent<Z> {
    return when (content2) {
        is StateContent.Exist -> StateContent.Exist(transform(rawContent, content2.rawContent))
        is StateContent.NotExist -> StateContent.NotExist()
    }
}

private fun <A, B, Z> StateContent.NotExist<A>.zip(content2: StateContent<B>): StateContent<Z> {
    return when (content2) {
        is StateContent.Exist -> StateContent.NotExist()
        is StateContent.NotExist -> StateContent.NotExist()
    }
}
