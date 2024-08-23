package com.tlc.data.ui.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tlc.data.ui.repository.firebase.AuthRepositoryImpl
import com.tlc.data.ui.repository.firebase.ReservationRepositoryImpl
import com.tlc.domain.repository.firebase.AuthRepository
import com.tlc.domain.repository.firebase.ReservationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideReservationRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): ReservationRepository {
        return ReservationRepositoryImpl(firebaseAuth,firestore)

    }
}