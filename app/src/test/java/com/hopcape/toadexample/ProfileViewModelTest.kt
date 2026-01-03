package com.hopcape.toadexample

import com.hopcape.toadexample.example.data.PreferencesRepository
import com.hopcape.toadexample.example.data.ProfileRepository
import com.hopcape.toadexample.example.domain.models.UserProfile
import com.hopcape.toadexample.example.presentation.ProfileUiState
import com.hopcape.toadexample.example.presentation.TraditionalProfileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever


@OptIn(ExperimentalCoroutinesApi::class)
class TraditionalProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    @Mock
    private lateinit var profileRepository: ProfileRepository
    @Mock
    private lateinit var preferencesRepository: PreferencesRepository // Unused, but required by constructor
    private lateinit var viewModel: TraditionalProfileViewModel
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // 3. Lifecycle Nonsense: We must define behavior BEFORE init block runs
        whenever(runBlocking { profileRepository.loadProfile() })
            .thenReturn(Result.success(UserProfile("John Doe", "url")))

        // Initialize the heavy class
        viewModel = TraditionalProfileViewModel(profileRepository, preferencesRepository)
    }
    @Test
    fun `loadData emits loading then success`() = runTest {
        // We have to use a library like Turbine to catch flows,
        // or inspect the state manually after delays.

        val state = viewModel.state.value

        assert(state is ProfileUiState.Success)
        assertEquals("John Doe", (state as ProfileUiState.Success).username)

        // Verify we didn't accidentally call the other repo
        verifyNoInteractions(preferencesRepository)
    }
}