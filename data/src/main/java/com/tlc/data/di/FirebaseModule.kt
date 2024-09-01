package com.tlc.data.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tlc.data.ui.repository.firebase.AuthRepositoryImpl
import com.tlc.data.ui.repository.firebase.CustomerRepositoryImpl
import com.tlc.data.ui.repository.firebase.DesignRepositoryImpl
import com.tlc.data.ui.repository.firebase.PlaceRepositoryImpl
import com.tlc.domain.repository.firebase.AuthRepository
import com.tlc.domain.repository.firebase.CustomerRepository
import com.tlc.domain.repository.firebase.DesignRepository
import com.tlc.domain.repository.firebase.PlaceRepository
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
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
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
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): PlaceRepository {
        return PlaceRepositoryImpl(firebaseAuth,firestore,storage)

    }
    @Provides
    @Singleton
    fun provideDesignRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
    ):DesignRepository {
        return DesignRepositoryImpl(firestore,firebaseAuth)
    }
    @Provides
    @Singleton
    fun provideCustomerRepository(
        firestore: FirebaseFirestore
    ):CustomerRepository{
        return CustomerRepositoryImpl(firestore)
    }
}