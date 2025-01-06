package com.phoenixflex.android.repository.implementation

import com.phoenixflex.android.core.result.Result
import com.phoenixflex.android.core.result.wrapAsResult
import com.phoenixflex.android.datastore.PreferencesManager
import com.phoenixflex.android.datastore.RegistrationStepsManager
import com.phoenixflex.android.datastore.UserManager
import com.phoenixflex.android.entities.auth.LoginResponse
import com.phoenixflex.android.network.manager.AuthDataSource
import com.phoenixflex.android.repository.AuthRepository
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
    private val userManager: UserManager,
    private val registrationStepsManager: RegistrationStepsManager
) : AuthRepository {
    override fun isSigned(): Flow<Boolean> {
        return userManager.getUser().map { it.email.isNotEmpty() }
    }

    override fun isTokenAvailable(): Flow<Boolean> {
        return preferencesManager.getPreferences().map { it.token != null }
    }

    override fun login(email: String, password: String): Flow<Unit> = flow {
        val authResponse = authDataSource.login(email, password)
        authGeneral(authResponse)
        emit(Unit)
    }

    private suspend fun authGeneral(loginResponse: LoginResponse) {
        preferencesManager.setToken(token = loginResponse.token)
        userManager.setUser(user = loginResponse.user)
        if (loginResponse.token != null) {
            preferencesManager.getPreferences().first().firebaseToken?.let {
                sendFirebaseToken(it)
            }
        }
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

    private suspend fun deleteUserLocalData() = coroutineScope {
        listOf(
            async { preferencesManager.setToken(null) },
            async { userManager.resetUserData() },
            async { registrationStepsManager.clearStepsInfo() }
        ).awaitAll()
    }

    override fun forgotPassword(email: String): Flow<Unit> = flow {
        authDataSource.forgotPassword(email)
        emit(Unit)
    }

    override fun forgotPasswordCheckCode(email: String, code: String): Flow<Unit> = flow {
        authDataSource.forgotPasswordCheckCode(email, code)
        emit(Unit)
    }

    override fun forgotPasswordResetPassword(
        email: String,
        code: String,
        password: String
    ): Flow<Unit> = flow {
        authDataSource.forgotPasswordResetPassword(email, code, password)
        emit(Unit)
    }

    override fun emailVerification(email: String, code: String): Flow<Unit> = flow {
        val authResponse = authDataSource.emailVerification(email, code)
        authGeneral(authResponse)
        emit(Unit)
    }

    override fun emailVerificationResendCode(email: String): Flow<Unit> = flow {
        authDataSource.emailVerificationResendCode(email)
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

    override fun isLiveModeAccepted(): Flow<Boolean> = flow {
        val result = authDataSource.isLiveModeAccepted()
        emit(result)
    }
}