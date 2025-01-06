package com.zippe.client.repository.implementation

import com.zippe.client.datastore.PreferencesManager
import com.zippe.client.datastore.UserManager
import com.zippe.client.entities.user.User
import com.zippe.client.network.manager.AuthDataSource
import com.zippe.client.repository.AuthRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val preferencesManager: PreferencesManager,
    private val userManager: UserManager
) : AuthRepository {
    override fun isSigned(): Flow<Boolean> {
        return userManager.getUser().map { it.phone.isNotEmpty() }
    }

    override fun isTokenAvailable(): Flow<Boolean> {
        return preferencesManager.getPreferences().map { it.token != null }
    }

    override fun login(phone: String, password: String): Flow<User> = flow {
        val (token, user) = authDataSource.login(phone, password)
        preferencesManager.setToken(token = token)
        userManager.setUser(
            user = user
        )
        emit(user)
    }

    override fun logout(withoutRequest: Boolean): Flow<Unit> = flow {
        if (!isSigned().first()) {
            throw Exception("Was not authorized")
        }
        if (!withoutRequest) {
            if (isTokenAvailable().first()) {
                preferencesManager.getPreferences().first().firebaseToken?.let {
                    try {
                        deleteFirebaseToken(it)
                    } catch (e: Exception) {
                        Timber.e(e, "Firebase token delete")
                        // ignore api logout request errors, continue to logout locally
                    }
                }
                try {
                    authDataSource.logout()
                } catch (e: Throwable) {
                    Timber.e(e, "logout request failed")
                    // ignore response of request and continue local logout
                }
            }
        }
        deleteUserLocalData()
        emit(Unit)
    }

    override fun signUp(
        name: String,
        phone: String,
        password: String
    ): Flow<Unit> = flow {
        authDataSource.signUp(
            name = name,
            phone = phone,
            password = password
        )
        userManager.setPhone(phone = phone)
        emit(Unit)
    }

    override fun checkPhoneNumber(phone: String): Flow<Boolean> = flow {
        val result = authDataSource.checkPhoneNumber(
            phone = phone,
        )
        emit(result)
    }

    private suspend fun deleteUserLocalData() = coroutineScope {
        listOf(
            async { preferencesManager.setToken(null) },
            async { userManager.resetUserData() },
        ).awaitAll()
    }

    override fun forgotPasswordCheckCode(phone: String, code: String): Flow<String> = flow {
        val token = authDataSource.forgotPasswordCheckCode(phone, code)
        emit(token)
    }

    override fun forgotPasswordResendCode(phone: String): Flow<Unit> = flow {
        authDataSource.forgotPasswordResendCode(phone = phone)
        emit(Unit)
    }

    override fun forgotPasswordResetPassword(
        code: String,
        password: String
    ): Flow<Unit> = flow {
        authDataSource.forgotPasswordResetPassword(code, password)
        emit(Unit)
    }

    override fun signUpVerification(password: String, phone: String, code: String): Flow<Unit> =
        flow {
            authDataSource.signUpVerification(
                password = password,
                phone = phone,
                code = code
            )
            emit(Unit)
        }

    override fun signUpVerificationResendCode(phone: String): Flow<Unit> = flow {
        authDataSource.signUpVerificationResendCode(phone)
        emit(Unit)
    }

    override suspend fun saveFirebaseToken(token: String) {
        preferencesManager.setFirebaseToken(token)
    }

    override suspend fun sendFirebaseToken(token: String) {
        authDataSource.sendFirebaseToken(token)
    }

    override suspend fun deleteFirebaseToken(token: String) {
        authDataSource.deleteFirebaseToken(token)
    }
}