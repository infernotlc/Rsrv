package com.tlc.domain.use_cases.firebase_use_cases.auth

import com.tlc.domain.repository.firebase.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
suspend operator fun invoke () = authRepository.signOut()
}