package com.hopcape.toadexample

import com.hopcape.toadexample.example.data.PreferencesRepository
import com.hopcape.toadexample.example.data.ProfileRepository
import com.hopcape.toadexample.example.domain.models.UserProfile
import com.hopcape.toadexample.example.presentation.LoadProfile
import com.hopcape.toadexample.example.presentation.ProfileActionDependencies
import com.hopcape.toadexample.example.presentation.ProfileEvent
import com.hopcape.toadexample.example.presentation.ProfileUiState
import com.hopcape.toadexample.toad.TestActionScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class LoadProfileTest {

    @Test
    fun `execute emits success on valid repo response`() = runTest {
        // 1. Dependencies: Simple Mock/Fake
        val repo = mock<ProfileRepository> {
            onBlocking { loadProfile() } doReturn Result.success(UserProfile("John Doe", "url"))
        }
        val pref = mock<PreferencesRepository> {
            onBlocking { getDarkModeEnabled() } doReturn true
        }

        // 2. The Scope: A simple capture helper (no Android lifecycle needed)
        val scope = TestActionScope<ProfileUiState, ProfileEvent>(
            initialState = ProfileUiState.Loading
        )

        // 3. Execution: Call the object directly
        LoadProfile.execute(
            dependencies = ProfileActionDependencies(
                repo,
                pref,
                this
            ),
            scope = scope
        )

        // 4. Verification: Just check the list of states in the scope
        assert(scope.capturedStates[0] is ProfileUiState.Loading)
        assert(scope.capturedStates[1] is ProfileUiState.Success)
    }
}