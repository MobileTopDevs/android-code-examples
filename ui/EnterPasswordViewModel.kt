package com.zippe.client.features.auth.password

import android.content.Context
import android.util.Range
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zippe.client.common_ui.state.TextFieldState
import com.zippe.client.common_ui.validator.TextFieldValidator
import com.zippe.client.core.result.Result
import com.zippe.client.core.util.isActive
import com.zippe.client.domain.auth.ForgotPasswordResetUseCase
import com.zippe.client.domain.auth.UserSignUpMainUseCase
import com.zippe.client.features.R
import com.zippe.client.features.auth.password.navigation.EnterPasswordDestination
import com.zippe.client.features.auth.password.validator.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnterPasswordViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
    private val signUpUseCase: UserSignUpMainUseCase,
    private val forgotPasswordResetUseCase: ForgotPasswordResetUseCase
) : ViewModel() {
    val phone: String? = savedStateHandle[EnterPasswordDestination.phoneArg]
    private val name: String? = savedStateHandle[EnterPasswordDestination.nameArg]
    private val verificationToken: String? = savedStateHandle[EnterPasswordDestination.verificationTokenArg]

    val isSignUpFlow = name != null && phone != null

    private var requestJob: Job? = null
    private val _requestResponse = MutableStateFlow<Result<Unit>>(Result.Initial)
    val requestResponse = _requestResponse.asStateFlow()

    val passwordTextFieldState = TextFieldState(context.getString(R.string.auth_input_password))

    val passwordRepeatTextFieldState = TextFieldState(context.getString(R.string.auth_input_password))

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
                R.string.auth_text_field_password_empty_error_text,
                passwordLengthRange.lower
            ),
            invalidLengthMessage = context.getString(
                R.string.auth_text_field_password_invalid_length_error_text,
                passwordLengthRange.lower
            ),
            invalidPatternErrorMessage = context.getString(
                R.string.auth_text_field_password_invalid_pattern_error_text
            ),
        )
    )

    private val passwordRepeatTextFieldValidator = TextFieldValidator(
        fieldState = passwordRepeatTextFieldState,
        validator = Validators.PasswordConfirm.create(
            passwordConfirmErrorMessage = context.getString(
                R.string.auth_text_field_password_confirm_error_text
            ),
            textFieldStateToCompare = passwordTextFieldState
        )
    )

    fun updatePasswordInput(text: TextFieldValue) {
        with(passwordTextFieldState) {
            val hasTextChanged = inputText.text != text.text
            inputText = text
            if (hasTextChanged) {
                passwordTextFieldValidator.validate()
            }
        }
    }

    fun updatePasswordRepeatInput(text: TextFieldValue) {
        with(passwordRepeatTextFieldState) {
            val hasTextChanged = inputText.text != text.text
            inputText = text
            if (hasTextChanged) {
                passwordRepeatTextFieldValidator.validate()
            }
        }
    }

    fun resetRequestResponse() {
        _requestResponse.value = Result.Initial
    }

    fun validateDataAndMakeRequest() {
        if (requestJob.isActive) return
        requestJob = viewModelScope.launch {
            val validators = listOf(
                passwordTextFieldValidator,
                passwordRepeatTextFieldValidator
            )
            val dataValid = validators.map { it.validate() }.all { it }
            if (dataValid) {
                if (isSignUpFlow) {
                    signUpUseCase(
                        name = name!!,
                        phone = "+${phone!!}",
                        password = passwordTextFieldState.inputText.text.trim()
                    ).collect {
                        _requestResponse.emit(it)
                    }
                } else {
                    forgotPasswordResetUseCase(
                        code = verificationToken!!,
                        password = passwordTextFieldState.inputText.text.trim()
                    ).collect {
                        _requestResponse.emit(it)
                    }
                }
            } else {
                validators.first { it.fieldState.focusIfError() }
            }
        }
    }

}