package com.kazakago.cacheflowable.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class FlowStateMapperTest {

    private lateinit var flowFixedState: Flow<State<Int>>

    @Before
    fun setup() {
        flowFixedState = flow { emit(State.Fixed(StateContent.wrap(30))) }
    }

    @Test
    fun validateMapContentMethod() = runBlockingTest {
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
