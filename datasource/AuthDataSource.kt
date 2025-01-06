package com.phoenixflex.android.network.manager

import com.phoenixflex.android.entities.auth.LoginResponse

interface AuthDataSource {

    suspend fun login(email: String, password: String): LoginResponse

    suspend fun logout()

    suspend fun forgotPassword(email: String)

    suspend fun forgotPasswordCheckCode(email: String, code: String)

    suspend fun forgotPasswordResetPassword(email: String, code: String, password: String)

    suspend fun emailVerification(email: String, code: String): LoginResponse

    suspend fun emailVerificationResendCode(email: String)

    suspend fun sendFirebaseToken(token: String)

    suspend fun deleteFirebaseToken(token: String)

    suspend fun isLiveModeAccepted(): Boolean
}