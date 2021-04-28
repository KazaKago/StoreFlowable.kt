package com.kazakago.storeflowable.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FlowStateMapperTest {

    private lateinit var flowFixedState: FlowableState<Int>

    @Before
    fun setup() {
        flowFixedState = flow { emit(State.Fixed(StateContent.wrap(30))) }
    }

    @Test
    fun mapContent() = runBlockingTest {
        val mappedFlowFixedState = flowFixedState.mapContent { it + 70 }
        val mappedFixedState = mappedFlowFixedState.first()
        mappedFixedState shouldBeInstanceOf State.Fixed::class
        mappedFixedState.content.doAction(
            onExist = {
                it shouldBeEqualTo 100
            },
            onNotExist = {
                fail()
            }
        )
    }
}
