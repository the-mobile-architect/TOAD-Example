package com.hopcape.toadexample.example.data

interface PreferencesRepository {
    suspend fun getDarkModeEnabled(): Boolean
    suspend fun setDarkModeEnabled(enabled: Boolean)
}