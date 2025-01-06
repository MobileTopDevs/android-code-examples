package com.example.hashtex.ui.login.validator

import com.example.hashtex.base.validator.DirectValidator
import com.example.hashtex.base.validator.rule.CharsEmptyChecker
import com.example.hashtex.base.validator.rule.CharsLengthChecker

class Validators private constructor() {

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

    class EmailPhone private constructor() : DirectValidator<CharSequence, String>() {

        companion object {
            fun create(
                minLength: Int,
                maxLength: Int,
                emptyMessage: String,
                invalidLengthMessage: String
            ): EmailPhone {
                return EmailPhone()
                    .addChecker(CharsEmptyChecker(emptyMessage))
                    .addChecker(CharsLengthChecker(minLength, maxLength, invalidLengthMessage))
                        as EmailPhone
            }
        }
    }
}