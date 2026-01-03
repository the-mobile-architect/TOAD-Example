package com.hopcape.toadexample.example.presentation

import androidx.lifecycle.viewModelScope
import com.fourthfrontier.utils.toad.ToadViewModel
import com.hopcape.toadexample.example.data.PreferencesRepository
import com.hopcape.toadexample.example.data.ProfileRepository
import com.hopcape.toadexample.toad.ViewEvent
import com.hopcape.toadexample.toad.ViewState

sealed interface ProfileUiState: ViewState{
    data object Loading: ProfileUiState
    data class Error(val errorMessage: String): ProfileUiState
    data class Success(val username: String,val imageUrl: String = ""): ProfileUiState

}
sealed interface ProfileEvent: ViewEvent {
    data object NavigateBack: ProfileEvent
}
