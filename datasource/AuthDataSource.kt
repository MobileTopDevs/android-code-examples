package com.zippe.client.network.manager

import com.zippe.client.entities.user.User

interface AuthDataSource {

    suspend fun login(phone: String, password: String): Pair<String, User>

    suspend fun logout()

    suspend fun signUp(
        name: String,
        phone: String,
        password: String
    )

    suspend fun checkPhoneNumber(phone: String): Boolean

    suspend fun forgotPasswordCheckCode(phone: String, code: String): String

    suspend fun forgotPasswordResendCode(phone: String)

    suspend fun forgotPasswordResetPassword(code: String, password: String)

    suspend fun signUpVerification(password: String, phone: String, code: String)

    suspend fun signUpVerificationResendCode(phone: String)

    suspend fun sendFirebaseToken(token: String)

    suspend fun deleteFirebaseToken(token: String)
}