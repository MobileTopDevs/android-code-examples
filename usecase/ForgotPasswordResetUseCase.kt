package com.zippe.client.domain.auth

import com.zippe.client.core.result.Result
import com.zippe.client.core.result.wrapAsResult
import com.zippe.client.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ForgotPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Forgot password
     *
     * @param code code from phone
     * @param password code from phone
     */
    operator fun invoke(code: String, password: String): Flow<Result<Unit>> {
        return authRepository.forgotPasswordResetPassword(
            code = code,
            password = password
        ).wrapAsResult()
    }
}