package com.phoenixflex.android.network.manager.implementation

import com.phoenixflex.android.entities.auth.LoginResponse
import com.phoenixflex.android.network.api.AuthApi
import com.phoenixflex.android.network.manager.AuthDataSource
import com.phoenixflex.android.network.models.auth.asEntity
import com.phoenixflex.android.network.requests.auth.NetworkEmailVerificationRequest
import com.phoenixflex.android.network.requests.auth.NetworkForgotPasswordCheckCodeRequest
import com.phoenixflex.android.network.requests.auth.NetworkLoginRequest
import com.phoenixflex.android.network.requests.auth.NetworkResetPasswordRequest
import com.phoenixflex.android.network.requests.auth.NetworkTokenRequest
import com.phoenixflex.android.network.utils.NetworkErrorConverterHelper

class AuthDataSourceImpl(
    private val authApi: AuthApi,
    private val networkErrorConverterHelper: NetworkErrorConverterHelper
) : AuthDataSource {

    override suspend fun login(email: String, password: String): LoginResponse = try {
        val response = authApi.login(
            NetworkLoginRequest(
                email = email,
                password = password
            )
        )
        response.data.asEntity()
    } catch (e: Throwable) {
        throw networkErrorConverterHelper.parseError(e)
    }

    override suspend fun logout() {
        try {
            authApi.logout()
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun forgotPassword(email: String) {
        try {
            authApi.forgotPassword(
                email = email
            )
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun forgotPasswordCheckCode(email: String, code: String) {
        try {
            authApi.forgotPasswordCheckCode(
                NetworkForgotPasswordCheckCodeRequest(
                    email = email,
                    code = code
                )
            )
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun forgotPasswordResetPassword(
        email: String,
        code: String,
        password: String
    ) {
        try {
            authApi.forgotPasswordResetPassword(
                NetworkResetPasswordRequest(
                    email = email,
                    code = code,
                    password = password
                )
            )
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun emailVerification(email: String, code: String): LoginResponse {
        return try {
            val response = authApi.emailVerification(
                NetworkEmailVerificationRequest(
                    email = email,
                    code = code
                )
            )
            response.data.asEntity()
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun emailVerificationResendCode(email: String) {
        try {
            authApi.emailVerificationResendCode(
                email = email
            )
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun sendFirebaseToken(token: String) {
        try {
            authApi.sendFirebaseToken(
                NetworkTokenRequest(token = token)
            )
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun deleteFirebaseToken(token: String) {
        try {
            authApi.deleteFirebaseToken(
                NetworkTokenRequest(token = token)
            )
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun isLiveModeAccepted(): Boolean {
        return try {
            authApi.isLiveModeAccepted() == 1
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }
}