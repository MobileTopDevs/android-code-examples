package com.phoenixflex.android.features.login.validator

import com.phoenixflex.android.common_ui.validator.DirectValidator
import com.phoenixflex.android.common_ui.validator.rule.CharsEmptyChecker
import com.phoenixflex.android.common_ui.validator.rule.CharsLengthChecker
import com.phoenixflex.android.common_ui.validator.rule.EmailMatchChecker

class Validators private constructor() {

    class Email private constructor() : DirectValidator<CharSequence, String>() {

        companion object {
            fun create(
                emptyMessage: String,
                invalidEmailMessage: String
            ): Email {
                return Email()
                    .addChecker(CharsEmptyChecker(emptyMessage))
                    .addChecker(EmailMatchChecker(invalidEmailMessage))
                        as Email
            }
        }
    }

    class Password private constructor() : DirectValidator<CharSequence, String>() {
        companion object {
            fun create(
                minLength: Int,
                maxLength: Int,
                emptyMessage: String,
                invalidLengthMessage: String
            ): Password {
                return Password()
                    .addChecker(CharsEmptyChecker(emptyMessage))
                    .addChecker(CharsLengthChecker(minLength, maxLength, invalidLengthMessage))
                        as Password
            }
        }
    }
}