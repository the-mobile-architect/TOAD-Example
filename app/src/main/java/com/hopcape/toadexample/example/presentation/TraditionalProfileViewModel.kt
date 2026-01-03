package com.hopcape.toadexample.example.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hopcape.toadexample.example.data.PreferencesRepository
import com.hopcape.toadexample.example.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class TraditionalProfileViewModel(
    private val repository: ProfileRepository,
    private val preferencesRepository: PreferencesRepository
): ViewModel(){
    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state get() = _state.asStateFlow()

    init {
        loadData()
    }
    fun loadData(){
        viewModelScope.launch {
            repository.loadProfile()
                .fold(
                    onSuccess = {
                        _state.value = ProfileUiState.Success(it.username, it.imageUrl)
                    },
                    onFailure = {
                        _state.value = ProfileUiState.Error(it.message.toString())
                    }
                )

        }
    }

    fun updateName(name: String){
        viewModelScope.launch {
            repository.updateName(name)
                .fold(
                    onSuccess = {
                        _state.value = ProfileUiState.Success(name)
                    },
                    onFailure = {
                        _state.value = ProfileUiState.Error(it.message.toString())
                    }
                )
        }
    }

}