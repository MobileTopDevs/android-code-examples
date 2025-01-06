package com.example.hashtex.ui.login

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hashtex.BuildConfig
import com.example.hashtex.models.Result
import com.example.hashtex.models.auth.AuthResponseUI
import com.example.hashtex.models.auth.SocialProvider
import com.example.hashtex.models.auth.TwoFactorAuthWay
import com.example.hashtex.models.auth.TwoFactorAuthWayResponse
import com.example.hashtex.repository.AuthRepository
import com.example.hashtex.util.SocialSignInFacebookUtility
import com.example.hashtex.util.isActive
import com.example.hashtex.util.wrapAsResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext application: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private var jobLogin: Job? = null
    private val _loginResponse = MutableStateFlow<Result<AuthResponseUI>>(Result.Initial)
    val loginResponse = _loginResponse.asStateFlow()

    private var jobRegister: Job? = null
    private val _registerResponse = MutableStateFlow<Result<AuthResponseUI>>(Result.Initial)
    val registerResponse = _registerResponse.asStateFlow()

    private var jobFetchGoogleToken: Job? = null
    private val _googleTokenResponse = MutableStateFlow<Result<String>>(Result.Initial)
    val googleTokenResponse = _googleTokenResponse.asStateFlow()

    private var jobFacebookLogin: Job? = null
    private val _facebookLoginTokenResponse = MutableStateFlow<Result<String>>(Result.Initial)
    val facebookLoginTokenResponse = _facebookLoginTokenResponse.asStateFlow()

    private var jobTwoFactorAuthWayChoose: Job? = null
    private val _twoFactorAuthWayChooseResponse =
        MutableStateFlow<Result<TwoFactorAuthWayResponse>>(Result.Initial)
    val twoFactorAuthWayChooseResponse = _twoFactorAuthWayChooseResponse.asStateFlow()

    val processing = combine(
        loginResponse,
        registerResponse,
        googleTokenResponse,
        facebookLoginTokenResponse,
        twoFactorAuthWayChooseResponse
    ) {  responses ->
        responses.any { it.loading }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val emailOrPhoneText = MutableStateFlow("")
    val passwordText = MutableStateFlow("")

    init {
        if (BuildConfig.DEBUG) {
            setTestDebugCredentials(application)
        }
    }

    private fun isAnyJobActive(): Boolean {
        return jobLogin.isActive
                || jobRegister.isActive
                || jobFetchGoogleToken.isActive
                || jobFacebookLogin.isActive
                || jobTwoFactorAuthWayChoose.isActive
    }

    fun login() {
        if (isAnyJobActive()) {
            return
        }
        jobLogin = viewModelScope.launch {
            authRepository.login(
                login = emailOrPhoneText.value.trim(),
                password = passwordText.value.trim()
            ).collect {
                _loginResponse.emit(it)
            }
        }
    }

    fun register() {
        if (isAnyJobActive()) {
            return
        }
        jobRegister = viewModelScope.launch {
            authRepository.register(
                email = emailOrPhoneText.value.trim(),
                password = passwordText.value.trim()
            ).collect {
                _registerResponse.emit(it)
            }
        }
    }

    fun socialLogin(
        provider: SocialProvider,
        accessToken: String
    ) {
        if (jobLogin.isActive) {
            return
        }
        jobLogin = viewModelScope.launch {
            authRepository.socialLogin(
                provider = provider,
                accessToken = accessToken
            ).collect {
                _loginResponse.emit(it)
            }
        }
    }

    fun resetLoginResponse() {
        _loginResponse.value = Result.Initial
    }

    fun resetRegisterResponse() {
        _registerResponse.value = Result.Initial
    }

    fun getGoogleToken(
        clientId: String,
        clientSecret: String,
        authCode: String,
        idToken: String
    ) {
        if (isAnyJobActive()) {
            return
        }
        jobFetchGoogleToken = viewModelScope.launch {
            authRepository.getGoogleToken(
                clientId = clientId,
                clientSecret = clientSecret,
                authCode = authCode,
                idToken = idToken
            ).collect {
                _googleTokenResponse.emit(it)
            }
        }
    }

    fun resetGoogleTokenResponse() {
        _googleTokenResponse.value = Result.Initial
    }

    fun getFacebookLoginToken(fragment: Fragment) {
        if (isAnyJobActive()) {
            return
        }
        jobFacebookLogin = viewModelScope.launch {
            flow {
                val token = SocialSignInFacebookUtility.getAccessToken(fragment)
                emit(token)
            }.wrapAsResult().collect {
                _facebookLoginTokenResponse.emit(it)
            }
        }
    }

    fun resetFacebookLoginTokenResponse() {
        _facebookLoginTokenResponse.value = Result.Initial
    }

    fun chooseTwoFactorAuthWay(userSession: String, twoFactorAuthWay: TwoFactorAuthWay) {
        if (jobTwoFactorAuthWayChoose.isActive) {
            return
        }

        jobTwoFactorAuthWayChoose = viewModelScope.launch {
            authRepository.chooseTwoFactorAuthWay(
                userSession = userSession,
                twoFactorAuthWay = twoFactorAuthWay
            ).collect {
                _twoFactorAuthWayChooseResponse.emit(it)
            }
        }
    }

    fun resetTwoFactorAuthWayChooseResponse() {
        _twoFactorAuthWayChooseResponse.value = Result.Initial
    }

    @SuppressLint("DiscouragedApi")
    private fun setTestDebugCredentials(context: Context) {
        emailOrPhoneText.value = context.getString(
            context.resources.getIdentifier(
                "test_user_email",
                "string",
                context.packageName
            )
        )
        passwordText.value = context.getString(
            context.resources.getIdentifier(
                "test_user_password",
                "string",
                `context`.packageName
            )
        )
    }

    fun clearPasswordInputText() {
        passwordText.value = ""
    }
}