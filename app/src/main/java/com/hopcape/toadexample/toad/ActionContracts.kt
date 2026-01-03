package com.hopcape.toadexample.toad

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * A **Typed Object** that encapsulates a single piece of business logic.
 *
 * This is the "O" in **TOAD** (Typed Object Action Dispatch).
 *
 * ViewAction follows the **Command Pattern**, enabling:
 * - **Open/Closed Principle**: Add new behavior without modifying ViewModel
 * - **Single Responsibility**: Each action handles one concern
 * - **Testability**: Actions can be tested in isolation with mocked dependencies
 * - **Reusability**: Actions can be shared across ViewModels if dependencies match
 *
 * @param D The [ActionDependencies] subtype providing required dependencies
 * @param S The [ViewState] subtype this action operates on
 * @param E The [ViewEvent] subtype this action can emit
 *
 * Example:
 * ```
 * data object LoadUserProfile : ProfileAction() {
 *     override suspend fun execute(
 *         dependencies: ProfileDependencies,
 *         scope: ActionScope<ProfileState, ProfileEvent>
 *     ) {
 *         scope.setState { copy(isLoading = true) }
 *         val user = dependencies.userRepository.getCurrentUser()
 *         scope.setState { copy(isLoading = false, user = user) }
 *     }
 * }
 * ```
 */
interface ViewAction<D : ActionDependencies, S : ViewState, E : ViewEvent> {
    /**
     * Executes the action's business logic.
     *
     * @param dependencies The [ActionDependencies] providing repositories, use cases, etc.
     * @param scope The [ActionScope] for updating state and emitting events.
     */
    suspend fun execute(dependencies: D, scope: ActionScope<S, E>)
}

/**
 * Provides controlled access to state mutation and event emission within an action.
 *
 * This scope ensures actions can only interact with state/events through
 * well-defined methods, preventing direct access to internal flows.
 *
 * @param S The [ViewState] type
 * @param E The [ViewEvent] type
 */
open class ActionScope<S : ViewState, E : ViewEvent>(
    private val stateFlow: MutableStateFlow<S>,
    private val eventChannel: Channel<E>
) {
    /**
     * The current state value. Use this to read state before making decisions.
     */
    val currentState: S get() = stateFlow.value

    /**
     * Updates the state using a reducer function.
     *
     * The reducer receives the current state and returns a new state.
     * Thread-safe via [MutableStateFlow.update].
     *
     * Example:
     * ```
     * scope.setState { copy(isLoading = true) }
     * scope.setState { copy(items = items + newItem) }
     * ```
     *
     * @param reducer Function that transforms current state to new state
     */
    open fun setState(reducer: S.() -> S) {
        stateFlow.update(reducer)
    }

    /**
     * Emits a one-time event to the UI layer.
     *
     * Events are buffered and consumed once. Use for side effects
     * like navigation, snackbars, or analytics.
     *
     * Note: Uses [Channel.trySend] - events may be dropped if channel is full.
     * For critical events, consider increasing buffer size in ViewModel.
     *
     * Example:
     * ```
     * scope.sendEvent(ProfileEvent.ShowSnackbar("Profile updated"))
     * scope.sendEvent(ProfileEvent.NavigateTo("/home"))
     * ```
     *
     * @param event The [ViewEvent] to emit
     */
    open fun sendEvent(event: E) {
        eventChannel.trySend(event)
    }

    /**
     * Executes an async operation with automatic loading state management.
     *
     * Handles the common pattern of:
     * 1. Set loading = true
     * 2. Execute async operation
     * 3. Handle success/failure
     * 4. Set loading = false
     *
     * Example:
     * ```
     * scope.withLoading(
     *     setLoading = { copy(isLoading = it) },
     *     block = { userRepository.fetchUser(userId) },
     *     onSuccess = { user -> setState { copy(user = user) } },
     *     onFailure = { error -> sendEvent(ShowError(error.message)) }
     * )
     * ```
     *
     * @param setLoading Reducer to update loading state
     * @param block Suspending operation to execute (return value directly, not Result)
     * @param onSuccess Callback with the successful result
     * @param onFailure Callback with the exception (defaults to logging)
     */
    suspend fun <T> withLoading(
        setLoading: S.(Boolean) -> S,
        block: suspend () -> T,
        onSuccess: (T) -> Unit,
        onFailure: (Throwable) -> Unit = { throwable ->
            // Default: log the error. Override to emit events or update error state.
            println("Action failed: ${throwable.message}")
            throwable.printStackTrace()
        }
    ) {
        setState { setLoading(true) }
        try {
            val result = block()
            onSuccess(result)
        } catch (e: Throwable) {
            onFailure(e)
        } finally {
            setState { setLoading(false) }
        }
    }

    /**
     * Executes an async operation that returns [Result] with automatic loading state.
     *
     * Use this variant when your repository/use case returns [Result<T>].
     *
     * Example:
     * ```
     * scope.withLoadingResult(
     *     setLoading = { copy(isLoading = it) },
     *     block = { userRepository.fetchUserSafe(userId) }, // Returns Result<User>
     *     onSuccess = { user -> setState { copy(user = user) } },
     *     onFailure = { error -> sendEvent(ShowError(error.message)) }
     * )
     * ```
     */
    suspend fun <T> withLoadingResult(
        setLoading: S.(Boolean) -> S,
        block: suspend () -> Result<T>,
        onSuccess: (T) -> Unit,
        onFailure: (Throwable) -> Unit = { throwable ->
            println("Action failed: ${throwable.message}")
            throwable.printStackTrace()
        }
    ) {
        setState { setLoading(true) }
        try {
            block().fold(
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        } catch (e: Throwable) {
            onFailure(e)
        } finally {
            setState { setLoading(false) }
        }
    }

    /**
     * Convenience method to update state and emit an event atomically.
     *
     * Example:
     * ```
     * scope.setStateAndEmit(
     *     reducer = { copy(isSubmitted = true) },
     *     event = FormEvent.SubmissionComplete
     * )
     * ```
     */
    fun setStateAndEmit(reducer: S.() -> S, event: E) {
        setState(reducer)
        sendEvent(event)
    }




}