package com.kazakago.storeflowable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.Closeable

class TestFlow<T>(flow: Flow<T>, scope: CoroutineScope) : Closeable {
    private val _history = mutableListOf<T>()
    val history: List<T> = _history
    private val job: Job

    init {
        job = flow.onEach { _history.add(it) }
            .launchIn(scope)
    }

    override fun close() {
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
