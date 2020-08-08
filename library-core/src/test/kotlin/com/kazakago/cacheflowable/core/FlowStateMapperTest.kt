package com.kazakago.cacheflowable.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
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
    fun validateMapContentMethod() = runBlocking {
        val mappedFlowFixedState = flowFixedState.mapContent { it + 70 }
        val mappedFixedState = mappedFlowFixedState.first()
        assertThat(mappedFixedState, `is`(instanceOf(State.Fixed::class.java)))
        mappedFixedState.content.doAction(
            onExist = {
                assertThat(it, `is`(100))
            },
            onNotExist = {
                fail()
            }
        )
    }

}
