package com.kazakago.storeflowable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class TestFlow<T>(flow: Flow<T>, scope: CoroutineScope) {
    private val _history = mutableListOf<T>()
    val history: List<T> = _history
    private val job = flow.onEach { _history.add(it) }
        .launchIn(scope)

    inline fun use(block: (flow: TestFlow<T>) -> Unit) {
        block(this)
        close()
    }

    fun close() {
        job.cancel()
    }

    fun getHistoryWithClose(): List<T> {
        close()
        return history
    }
}

fun <T> Flow<T>.toTest(scope: CoroutineScope): TestFlow<T> {
    return TestFlow(this, scope)
}
