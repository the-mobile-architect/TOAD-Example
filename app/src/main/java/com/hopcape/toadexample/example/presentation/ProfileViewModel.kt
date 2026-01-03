package com.hopcape.toadexample.example.presentation

import androidx.lifecycle.viewModelScope
import com.fourthfrontier.utils.toad.ToadViewModel
import com.hopcape.toadexample.example.data.PreferencesRepository
import com.hopcape.toadexample.example.data.ProfileRepository

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val preferencesRepository: PreferencesRepository,
    initialActions: List<ProfileAction> = emptyList()
): ToadViewModel<ProfileUiState, ProfileEvent>(ProfileUiState.Loading){
    override val dependencies = ProfileActionDependencies(
        profileRepository = repository,
        preferencesRepository = preferencesRepository,
        coroutineScope = viewModelScope
    )

    init {
        dispatchAll(initialActions)
    }
    fun runAction(action: ProfileAction) = dispatch(action)
}
