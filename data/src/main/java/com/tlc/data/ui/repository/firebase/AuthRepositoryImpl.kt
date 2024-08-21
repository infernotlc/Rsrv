package com.tlc.data.ui.repository.firebase

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.tlc.domain.repository.firebase.AuthRepository
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<RootResult<FirebaseUser>> = flow {
        emit(RootResult.Loading)
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            Log.d("AuthRepositoryImpl", "Signed in User: $user")
            val role = user?.let { getUserRole(it.uid) }
            Log.d("AuthRepositoryImpl", "User Role: $role")
            if (role == "admin" || role == "customer") {
                emit(RootResult.Success(user))
            } else {
                emit(RootResult.Error("Unknown role"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "SignIn Error: ${e.message}")
            emit(RootResult.Error(e.message ?: "Something went wrong"))
        }
    }.flowOn(Dispatchers.IO)


    suspend fun getUserRole(uid: String): String? {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.getString("role")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error getting user role: ${e.message}")
            null
        }
    }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<RootResult<FirebaseUser>> = flow {
        emit(RootResult.Loading)
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            val userMap = mapOf(
                "email" to (user?.email),
                "role" to "customer"
            )
            if (user != null) {
                firestore.collection("users").document(user.uid).set(userMap).await()
            }
            emit(RootResult.Success(user))
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Something went wrong"))
        }
    }.flowOn(Dispatchers.IO)

    override fun signOut(): Flow<RootResult<Boolean>> = flow {
        emit(RootResult.Loading)
        try {
            firebaseAuth.signOut()
            emit(RootResult.Success(true))
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Something went wrong"))
        }
    }.flowOn(Dispatchers.IO)

    override fun isLoggedIn(): Flow<RootResult<Boolean>> = flow {
        emit(RootResult.Loading)
        try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                emit(RootResult.Success(true))
            } else {
                emit(RootResult.Success(false))
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: "An unexpected error occurred while checking login status."
            Log.e(TAG, "isLoggedIn: error: $errorMessage")
            emit(RootResult.Error(errorMessage))
        }
    }

    override fun sendPasswordResetEmail(email: String): Flow<RootResult<Boolean>> = flow {
        emit(RootResult.Loading)
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            emit(RootResult.Success(true))
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Something went wrong"))
        }
    }

    override suspend fun deleteCurrentUser(): Flow<RootResult<Boolean>> = flow {
        emit(RootResult.Loading)
        try {
            val currentUser = firebaseAuth.currentUser
            val userId = currentUser?.uid
            if (userId != null) {

                val userDocRef = firestore.collection("users").document(userId)

                val playerCollection = userDocRef.collection("players").get().await()
                playerCollection.documents.forEach { it.reference.delete().await() }

                val competitionCollection = userDocRef.collection("competitions").get().await()
                competitionCollection.documents.forEach { it.reference.delete().await() }

                userDocRef.delete().await()

                currentUser.delete().await()

                emit(RootResult.Success(true))
            } else {
                emit(RootResult.Error("User ID is null"))
            }
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Something went wrong"))
        }
    }.flowOn(Dispatchers.IO)
}