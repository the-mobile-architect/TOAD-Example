package com.hopcape.toadexample.toad

import kotlinx.coroutines.CoroutineScope

/**
 * Marker interface for immutable UI state.
 *
 * Implementations should be data classes with all properties immutable (val).
 * State changes are made by creating new instances via copy().
 *
 * Example:
 * ```
 * data class HomeScreenState(
 *     val isLoading: Boolean = false,
 *     val items: List<Item> = emptyList(),
 *     val error: String? = null
 * ) : ViewState
 * ```
 */
interface ViewState

/**
 * Marker interface for one-time UI events.
 *
 * Events are consumed once and not persisted in state.
 * Use for: navigation, snackbars, toasts, dialogs, analytics, etc.
 *
 * Example:
 * ```
 * sealed interface HomeScreenEvent : ViewEvent {
 *     data class ShowSnackbar(val message: String) : HomeScreenEvent
 *     data class NavigateTo(val route: String) : HomeScreenEvent
 * }
 * ```
 */
interface ViewEvent

/**
 * Provides dependencies required by [ViewAction]s.
 *
 * Each ViewModel should create a concrete implementation containing:
 * - Repository interfaces
 * - Use cases
 * - Dispatchers (if needed for specific operations)
 * - Any other dependencies actions need
 *
 * The [coroutineScope] is provided for actions that need to launch
 * child coroutines (e.g., parallel operations, timeouts).
 * Note: Actions already run within viewModelScope, so this is only
 * needed for advanced coroutine scenarios.
 *
 * Example:
 * ```
 * class ProfileDependencies(
 *     override val coroutineScope: CoroutineScope,
 *     val userRepository: UserRepository,
 *     val analyticsTracker: AnalyticsTracker
 * ) : ActionDependencies()
 * ```
 */
abstract class ActionDependencies {
    abstract val coroutineScope: CoroutineScope
}
