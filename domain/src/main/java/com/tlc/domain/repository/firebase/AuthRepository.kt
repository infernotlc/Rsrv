package com.tlc.domain.repository.firebase

import com.google.firebase.auth.FirebaseUser
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<RootResult<FirebaseUser>>

    suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<RootResult<FirebaseUser>>

    fun signOut(): Flow<RootResult<Boolean>>
    fun isLoggedIn(): Flow<RootResult<String?>>
    fun sendPasswordResetEmail(email: String): Flow<RootResult<Boolean>>
    suspend fun deleteCurrentUser(): Flow<RootResult<Boolean>>
    suspend fun getUserRole(uid: String): String?
}