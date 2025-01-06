package com.example.hashtex.ui.login.validator

import com.example.hashtex.base.validator.ui.TextInputEditTextValidator
import com.google.android.material.textfield.TextInputLayout

class UIInputHelper private constructor() {
    companion object {
        @JvmStatic
        fun buildPasswordValidator(
            til: TextInputLayout,
            minLength: Int,
            maxLength: Int,
            emptyMessage: String,
            invalidLengthMessage: String
        ): TextInputEditTextValidator<CharSequence> {
            return TextInputEditTextValidator(
                til,
                Validators.Password.create(
                    minLength,
                    maxLength,
                    emptyMessage,
                    invalidLengthMessage
                ),
                showIconError = false
            )
        }

        @JvmStatic
        fun buildEmailPhoneValidator(
            til: TextInputLayout,
            minLength: Int,
            maxLength: Int,
            emptyMessage: String,
            invalidLengthMessage: String
        ): TextInputEditTextValidator<CharSequence> {
            return TextInputEditTextValidator(
                til,
                Validators.EmailPhone.create(
                    minLength,
                    maxLength,
                    emptyMessage,
                    invalidLengthMessage
                )
            )
        }
    }
}