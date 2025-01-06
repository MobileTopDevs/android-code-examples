package com.zippe.client.features.auth.password.validator

import com.zippe.client.common_ui.state.TextFieldState
import com.zippe.client.common_ui.validator.DirectValidator
import com.zippe.client.common_ui.validator.rule.CharsEditTextEqualityChecker
import com.zippe.client.common_ui.validator.rule.CharsEmptyChecker
import com.zippe.client.common_ui.validator.rule.CharsLengthChecker
import com.zippe.client.common_ui.validator.rule.FunctionalChecker
import java.util.regex.Pattern

class Validators private constructor() {
    class Password private constructor() : DirectValidator<CharSequence, String>() {

        private data class PasswordChecker(
            private val errorMessage: String,
            private val pattern: Pattern
        ) : FunctionalChecker<CharSequence, String>(check = { filledPassword ->
            pattern.matcher(filledPassword).matches()
        }, error = { errorMessage })

        companion object {
            val SPECIAL_CHARACTERS = "!@#&()–[{}]:;',?/*~$^+=<>"

            private val PATTERN = Pattern.compile(
                "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{1,100}$")

            fun create(
                minLength: Int,
                maxLength: Int,
                emptyMessage: String,
                invalidLengthMessage: String,
                invalidPatternErrorMessage: String
            ): Password {
                return Password()
                    .addChecker(CharsEmptyChecker(emptyMessage))
                    .addChecker(CharsLengthChecker(minLength, maxLength, invalidLengthMessage))
                    .addChecker(
                        PasswordChecker(
                            invalidPatternErrorMessage,
                            PATTERN
                        )
                    ) as Password
            }
        }
    }

    class PasswordConfirm private constructor() : DirectValidator<CharSequence, String>() {

        companion object {
            fun create(
                passwordConfirmErrorMessage: String,
                textFieldStateToCompare: TextFieldState
            ): PasswordConfirm {
                return PasswordConfirm()
                    .addChecker(
                        CharsEditTextEqualityChecker(
                            passwordConfirmErrorMessage,
                            textFieldStateToCompare
                        )
                    ) as PasswordConfirm
            }
        }
    }
}