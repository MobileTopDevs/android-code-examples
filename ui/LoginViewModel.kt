package com.phoenixflex.android.features.login

import android.content.Context
import android.util.Range
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenixflex.android.common_ui.state.TextFieldState
import com.phoenixflex.android.common_ui.validator.TextFieldValidator
import com.phoenixflex.android.core.result.Result
import com.phoenixflex.android.core.util.isActive
import com.phoenixflex.android.domain.auth.UserLoginUseCase
import com.phoenixflex.android.features.BuildConfig
import com.phoenixflex.android.features.R
import com.phoenixflex.android.features.login.validator.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val loginUseCase: UserLoginUseCase
) : ViewModel() {

    private var loginJob: Job? = null
    private val _loginResponse = MutableStateFlow<Result<Unit>>(Result.Initial)
    val loginResponse = _loginResponse.asStateFlow()

    val emailTextFieldState = TextFieldState(
        if (BuildConfig.DEBUG) {
            "test.email@gmail.com"
        } else {
            ""
        }
    )

    private val emailTextFieldValidator = TextFieldValidator(
        fieldState = emailTextFieldState,
        validator = Validators.Email.create(
            emptyMessage = context.getString(R.string.field_empty_error_text),
            invalidEmailMessage = context.getString(R.string.auth_email_invalid_error_text)
        )
    )

    val passwordTextFieldState = TextFieldState(
        if (BuildConfig.DEBUG) {
            "111111"
        } else {
            ""
        }
    )

    private val passwordLengthRange = Range(
        context.resources.getInteger(R.integer.input_length_min_password),
        context.resources.getInteger(R.integer.input_length_max_password)
    )
    private val passwordTextFieldValidator = TextFieldValidator(
        fieldState = passwordTextFieldState,
        validator = Validators.Password.create(
            minLength = passwordLengthRange.lower,
            maxLength = passwordLengthRange.upper,
            emptyMessage = context.getString(
                R.string.auth_password_empty_error_text,
                passwordLengthRange.lower
            ),
            invalidLengthMessage = context.getString(
                R.string.auth_password_invalid_length_error_text,
                passwordLengthRange.lower
            )
        )
    )

    fun updateEmailInput(text: TextFieldValue) {
        with(emailTextFieldState) {
            if (inputText.text != text.text) {
                errorText = null
            }
            inputText = text
        }
    }

    fun updatePasswordInput(text: TextFieldValue) {
        with(passwordTextFieldState) {
            if (inputText.text != text.text) {
                errorText = null
            }
            inputText = text
        }
    }

    fun resetLoginResponse() {
        _loginResponse.value = Result.Initial
    }

    fun validateDataAndLogin() {
        if (loginJob.isActive) return
        loginJob = viewModelScope.launch {
            val validators = listOf(
                emailTextFieldValidator,
                passwordTextFieldValidator
            )
            val dataValid = validators.map { it.validate() }.all { it }
            if (dataValid) {
                loginUseCase(
                    email = emailTextFieldState.inputText.text,
                    password = passwordTextFieldState.inputText.text
                ).collect {
                    _loginResponse.emit(it)
                }
            } else {
                validators.first { it.fieldState.focusIfError() }
            }
        }
    }
}