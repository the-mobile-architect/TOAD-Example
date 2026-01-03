package com.hopcape.toadexample.example.presentation

import com.hopcape.toadexample.toad.ActionScope
import com.hopcape.toadexample.toad.ViewAction

abstract class ProfileAction: ViewAction<ProfileActionDependencies, ProfileUiState, ProfileEvent>

data object LoadProfile : ProfileAction() {
    override suspend fun execute(
        dependencies: ProfileActionDependencies,
        scope: ActionScope<ProfileUiState, ProfileEvent>
    ) {

        scope.setState { ProfileUiState.Loading }
        val loadProfileResult = dependencies.profileRepository.loadProfile()
        loadProfileResult
            .fold(
                onSuccess = {
                    scope.setState { ProfileUiState.Success(it.username, it.imageUrl) }
                },
                onFailure = {
                    scope.setState { ProfileUiState.Error(it.message.toString()) }
                }
            )
    }
}

data class UpdateName(val newName: String): ProfileAction(){
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