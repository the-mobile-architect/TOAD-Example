package com.hopcape.toadexample.example.data

import com.hopcape.toadexample.example.domain.models.UserProfile

typealias LoadProfileResult = Result<UserProfile>

interface ProfileRepository {
    suspend fun loadProfile(): LoadProfileResult
    suspend fun updateName(name: String): Result<Unit>
    suspend fun updateProfilePic(imageUri: String): Result<Unit>

}