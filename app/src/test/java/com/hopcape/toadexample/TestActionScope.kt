package com.hopcape.toadexample.toad

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A Test Double for [ActionScope].
 *
 * This class captures every state update and event emission into lists,
 * allowing you to write simple assertions without dealing with Coroutines Flow/Channel complexity.
 *
 * Usage:
 * ```
 * val scope = TestActionScope<State, Event>(initialState)
 * action.execute(deps, scope)
 *
 * assertEquals(true, scope.capturedStates[0].isLoading)
 * assertEquals(Event.ShowError, scope.capturedEvents[0])
 * ```
 */
class TestActionScope<S : ViewState, E : ViewEvent>(
    initialState: S
) : ActionScope<S, E>(
    // We pass real implementations to the parent so 'currentState' logic works automatically
    stateFlow = MutableStateFlow(initialState),
    eventChannel = Channel(Channel.UNLIMITED)
) {

    /**
     * A history of all states produced by the action (excluding the initial state).
     * Index 0 is the first update, Index 1 is the second, etc.
     */
    val capturedStates = mutableListOf<S>()

    /**
     * A history of all events emitted by the action.
     */
    val capturedEvents = mutableListOf<E>()

    /**
     * Access the most recent state directly (convenience property).
     */
    val lastState: S
        get() = if (capturedStates.isNotEmpty()) capturedStates.last() else currentState

    override fun setState(reducer: S.() -> S) {
        // 1. Let the parent update the actual MutableStateFlow
        super.setState(reducer)

        // 2. Capture the new result for our test assertions
        capturedStates.add(currentState)
    }

    override fun sendEvent(event: E) {
        // 1. Let the parent send it to the channel (optional, but keeps behavior consistent)
        super.sendEvent(event)

        // 2. Capture the event for our test assertions
        capturedEvents.add(event)
    }
}