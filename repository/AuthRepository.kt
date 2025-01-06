package com.zippe.client.repository

import com.zippe.client.entities.user.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun isSigned(): Flow<Boolean>

    fun isTokenAvailable(): Flow<Boolean>

    fun login(phone: String, password: String): Flow<User>

    fun logout(withoutRequest: Boolean): Flow<Unit>

    fun signUp(name: String, phone: String, password: String): Flow<Unit>

    fun checkPhoneNumber(phone: String): Flow<Boolean>

    fun forgotPasswordCheckCode(phone: String, code: String): Flow<String>

    fun forgotPasswordResendCode(phone: String): Flow<Unit>

    fun forgotPasswordResetPassword(code: String, password: String): Flow<Unit>

    fun signUpVerification(password: String, phone: String, code: String): Flow<Unit>

    fun signUpVerificationResendCode(phone: String): Flow<Unit>

    suspend fun saveFirebaseToken(token: String)

    suspend fun sendFirebaseToken(token: String)

    suspend fun deleteFirebaseToken(token: String)
}