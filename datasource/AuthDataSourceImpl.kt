package com.zippe.client.network.manager.implementation

import com.zippe.client.entities.user.User
import com.zippe.client.network.api.AuthApi
import com.zippe.client.network.manager.AuthDataSource
import com.zippe.client.network.models.user.asEntity
import com.zippe.client.network.requests.auth.NetworkCheckPhoneNumberRequest
import com.zippe.client.network.requests.auth.NetworkForgotPasswordCheckCodeRequest
import com.zippe.client.network.requests.auth.NetworkForgotPasswordResetRequest
import com.zippe.client.network.requests.auth.NetworkLoginRequest
import com.zippe.client.network.requests.auth.NetworkResendCodeRequest
import com.zippe.client.network.requests.auth.NetworkSignUpRequest
import com.zippe.client.network.requests.auth.NetworkSignUpVerificationRequest
import com.zippe.client.network.requests.auth.NetworkTokenRequest
import com.zippe.client.network.utils.NetworkErrorConverterHelper

class AuthDataSourceImpl(
    private val authApi: AuthApi,
    private val networkErrorConverterHelper: NetworkErrorConverterHelper
) : AuthDataSource {

    override suspend fun login(phone: String, password: String): Pair<String, User> = try {
        val response = authApi.login(
            NetworkLoginRequest(
                phone = phone,
                password = password
            )
        )
        val data = response.data
        Pair(data.token, data.user.asEntity())
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

    override suspend fun signUp(
        name: String,
        phone: String,
        password: String
    ) {
        try {
            authApi.signUp(
                NetworkSignUpRequest(
                    name = name,
                    phone = phone,
                    password = password
                )
            )
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun checkPhoneNumber(phone: String): Boolean {
        return try {
            authApi.checkPhoneNumber(
                NetworkCheckPhoneNumberRequest(
                    phone = phone,
                )
            ).data.result
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun forgotPasswordCheckCode(phone: String, code: String): String {
        return try {
            authApi.forgotPasswordCheckCode(
                NetworkForgotPasswordCheckCodeRequest(
                    phone = phone,
                    code = code
                )
            ).data.verificationToken
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun forgotPasswordResendCode(phone: String) {
        try {
            authApi.forgotPasswordResendCode(
                phone = NetworkResendCodeRequest(phone = phone),
            )
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun forgotPasswordResetPassword(
        code: String,
        password: String
    ) {
        try {
            authApi.forgotPasswordResetPassword(
                NetworkForgotPasswordResetRequest(
                    code = code,
                    password = password
                )
            )
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun signUpVerification(
        password: String,
        phone: String,
        code: String
    ) {
        try {
            authApi.signUpVerification(
                NetworkSignUpVerificationRequest(
                    password = password,
                    phone = phone,
                    code = code
                )
            )
        } catch (e: Throwable) {
            throw networkErrorConverterHelper.parseError(e)
        }
    }

    override suspend fun signUpVerificationResendCode(phone: String) {
        try {
            authApi.signUpVerificationResendCode(
                phone = NetworkResendCodeRequest(phone = phone),
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
}