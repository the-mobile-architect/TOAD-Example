package com.hopcape.toadexample.example.presentation

import com.hopcape.toadexample.example.data.PreferencesRepository
import com.hopcape.toadexample.example.data.ProfileRepository
import com.hopcape.toadexample.toad.ActionDependencies
import com.hopcape.toadexample.toad.ActionScope
import kotlinx.coroutines.CoroutineScope

data class ProfileActionDependencies(
    val profileRepository: ProfileRepository,
    val preferencesRepository: PreferencesRepository,
    override val coroutineScope: CoroutineScope
): ActionDependencies()

data class UpdateName1(val newName: String): ProfileAction(){
    override suspend fun execute(
        dependencies: ProfileActionDependencies,
        scope: ActionScope<ProfileUiState, ProfileEvent>
    ) {
        scope.setState { ProfileUiState.Loading }
        val updateNameResult = dependencies
            .profileRepository
            .updateName(newName)
        updateNameResult.fold(
            onSuccess = {
                scope.setState { ProfileUiState.Success(newName) }
            },
            onFailure = {
                scope.setState { ProfileUiState.Error(it.message.toString()) }
            }
        )
    }
}