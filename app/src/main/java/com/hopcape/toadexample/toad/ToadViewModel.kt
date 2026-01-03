package com.fourthfrontier.utils.toad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hopcape.toadexample.toad.ActionDependencies
import com.hopcape.toadexample.toad.ActionScope
import com.hopcape.toadexample.toad.ViewAction
import com.hopcape.toadexample.toad.ViewEvent
import com.hopcape.toadexample.toad.ViewState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * # 🐸 TOAD ViewModel
 *
 * Base ViewModel implementing the **TOAD (Typed Object Action Dispatch)** pattern.
 *
 * TOAD is a Kotlin-first, type-safe architecture pattern that combines:
 * - **T**yped: Compile-time type safety via generics
 * - **O**bject: Actions as first-class objects (Command Pattern)
 * - **A**ction: Self-contained business logic units
 * - **D**ispatch: Single dispatch point for all state changes
 *
 * ## Architecture Overview
 *
 * ```
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                              UI LAYER                                   │
 * │  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐       │
 * │  │  Composable │ ──────▶ │  ViewModel  │ ◀────── │    State    │       │
 * │  │   Screen    │ intent  │ runAction() │  state  │  StateFlow  │       │
 * │  └─────────────┘         └──────┬──────┘         └─────────────┘       │
 * └─────────────────────────────────┼─────────────────────────────────────┘
 *                                   │ dispatch
 *                                   ▼
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                           ACTION LAYER                                  │
 * │  ┌─────────────────────────────────────────────────────────────────┐   │
 * │  │                        ViewAction                                │   │
 * │  │  execute(dependencies, scope) {                                  │   │
 * │  │      scope.setState { copy(...) }  // Update state              │   │
 * │  │      scope.sendEvent(...)          // Emit one-time events      │   │
 * │  │  }                                                               │   │
 * │  └─────────────────────────────────────────────────────────────────┘   │
 * └─────────────────────────────────────────────────────────────────────────┘
 *                                   │
 *                                   ▼
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                         DEPENDENCIES LAYER                              │
 * │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                 │
 * │  │ Repository  │    │  Use Case   │    │   Service   │                 │
 * │  └─────────────┘    └─────────────┘    └─────────────┘                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 * ```
 *
 * ## Key Principles
 *
 * - **Unidirectional Data Flow**: State flows down, intents flow up
 * - **Open/Closed Principle**: Add new actions without modifying ViewModel
 * - **Single Source of Truth**: [state] is the only source of UI state
 * - **Immutable State**: State changes via copy(), never mutation
 * - **Testable Actions**: Each action can be tested in isolation
 *
 * ## Quick Start
 *
 * See `_UsageExample.kt` in this package for a complete working example.
 *
 * @param S The [com.hopcape.toadexample.toad.ViewState] type for this ViewModel
 * @param E The [com.hopcape.toadexample.toad.ViewEvent] type for this ViewModel
 * @param initialState The initial state when ViewModel is created
 *
 * @see com.hopcape.toadexample.toad.ViewAction
 * @see com.hopcape.toadexample.toad.ActionDependencies
 * @see com.hopcape.toadexample.toad.ActionScope
 */
abstract class ToadViewModel<S : ViewState, E : ViewEvent>(
    initialState: S
) : ViewModel() {

    /**
     * Dependencies available to all actions in this ViewModel.
     * Override to provide repositories, use cases, dispatchers, etc.
     */
    protected abstract val dependencies: ActionDependencies

    private val _state = MutableStateFlow(initialState)

    /**
     * The current UI state as an observable [StateFlow].
     * Collect this in your UI to react to state changes.
     */
    val state: StateFlow<S> = _state.asStateFlow()

    private val _events = Channel<E>(Channel.BUFFERED)

    /**
     * One-time events as a [Flow].
     * Collect this in your UI for navigation, snackbars, etc.
     * Each event is consumed only once.
     */
    val events: Flow<E> = _events.receiveAsFlow()

    /**
     * Dispatches an action for execution.
     *
     * The action runs asynchronously in [viewModelScope] on the specified [dispatcher].
     * Multiple actions can run concurrently.
     *
     * @param action The [com.hopcape.toadexample.toad.ViewAction] to execute
     * @param dispatcher The [CoroutineDispatcher] to run on (default: [Dispatchers.Default])
     */
    protected fun <D : ActionDependencies> dispatch(
        action: ViewAction<D, S, E>,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ) {
        val scope = ActionScope(_state, _events)
        viewModelScope.launch(dispatcher) {
            @Suppress("UNCHECKED_CAST")
            action.execute(dependencies as D, scope)
        }
    }

    /**
     * Dispatches multiple actions sequentially.
     *
     * Actions execute one after another in the order provided.
     * All actions share the same [ActionScope] instance.
     *
     * @param actions The list of [ViewAction]s to execute in order
     * @param dispatcher The [CoroutineDispatcher] to run on (default: [Dispatchers.Default])
     */
    protected fun <D : ActionDependencies> dispatchAll(
        actions: List<ViewAction<D, S, E>>,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ) {
        if (actions.isEmpty()) return

        val scope = ActionScope(_state, _events)
        viewModelScope.launch(dispatcher) {
            actions.forEach { action ->
                @Suppress("UNCHECKED_CAST")
                action.execute(dependencies as D, scope)
            }
        }
    }

    /**
     * Direct state update without an action.
     *
     * Use sparingly - prefer actions for testability.
     * Useful for simple UI-only state changes (e.g., toggling a dialog).
     *
     * @param reducer Function that transforms current state to new state
     */
    protected fun updateState(reducer: S.() -> S) {
        _state.value = _state.value.reducer()
    }

    /**
     * Direct event emission without an action.
     *
     * Use sparingly - prefer actions for testability.
     * Useful for forwarding events from child components.
     *
     * @param event The [ViewEvent] to emit
     */
    protected fun emitEvent(event: E) {
        _events.trySend(event)
    }
}
