package com.phoenixflex.android.repository

import com.phoenixflex.android.core.result.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun isSigned(): Flow<Boolean>

    fun isTokenAvailable(): Flow<Boolean>

    fun login(email: String, password: String): Flow<Unit>

    fun logout(withoutRequest: Boolean): Flow<Unit>

    fun forgotPassword(email: String): Flow<Unit>

    fun forgotPasswordCheckCode(email: String, code: String): Flow<Unit>

    fun forgotPasswordResetPassword(email: String, code: String, password: String): Flow<Unit>

    fun emailVerification(email: String, code: String): Flow<Unit>

    fun emailVerificationResendCode(email: String): Flow<Unit>

    suspend fun saveFirebaseToken(token: String)

    suspend fun sendFirebaseToken(token: String)

    suspend fun deleteFirebaseToken(token: String)

    fun isLiveModeAccepted(): Flow<Boolean>
}