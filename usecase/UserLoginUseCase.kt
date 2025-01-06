package com.phoenixflex.android.domain.auth

import com.phoenixflex.android.core.result.Result
import com.phoenixflex.android.core.result.wrapAsResult
import com.phoenixflex.android.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * User authorization
     *
     * @param email user email
     * @param password user password
     */
    operator fun invoke(email: String, password: String): Flow<Result<Unit>> {
        return authRepository.login(email = email, password = password)
            .wrapAsResult()
    }
}