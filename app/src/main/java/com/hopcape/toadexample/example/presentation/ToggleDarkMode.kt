package com.hopcape.toadexample.example.presentation

import com.hopcape.toadexample.toad.ActionScope

class ToggleDarkMode : ProfileAction(){
    override suspend fun execute(
        dependencies: ProfileActionDependencies,
        scope: ActionScope<ProfileUiState, ProfileEvent>
    ) {
        TODO("Not yet implemented")
    }
}