package com.hopcape.toadexample.example.presentation

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel
){
    val state by viewModel.state.collectAsState()
    ProfileScreen(
        state = state,
        onAction = viewModel::runAction
    )
}

// Stateless Screen
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit
){
    // Collect state and render ui accordingly
    Button(
        onClick = { onAction(UpdateName("John Doe")) }
    ) {
        Text(
            text = "Update Name"
        )
    }
}