@file:OptIn(ExperimentalComposeUiApi::class)

package com.phoenixflex.android.features.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenixflex.android.common_ui.components.AppAlertDialog
import com.phoenixflex.android.common_ui.components.AppFilledButton
import com.phoenixflex.android.common_ui.components.AppTextButton
import com.phoenixflex.android.common_ui.components.AppTextField
import com.phoenixflex.android.common_ui.components.autofill
import com.phoenixflex.android.common_ui.state.TextFieldState
import com.phoenixflex.android.common_ui.theme.AppGradientBackground
import com.phoenixflex.android.common_ui.theme.AppTheme
import com.phoenixflex.android.common_ui.theme.AppColor
import com.phoenixflex.android.common_ui.theme.AppTypography
import com.phoenixflex.android.core.result.Result
import com.phoenixflex.android.core.result.loading
import com.phoenixflex.android.features.R

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    navigateToForgotPassword: () -> Unit,
    navigateToSignUp: () -> Unit
) {
    val loginResponse by loginViewModel.loginResponse.collectAsStateWithLifecycle()

    var errorForDialog by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = loginResponse) {
        when (val result = loginResponse) {
            is Result.Error -> {
                errorForDialog = result.message
                loginViewModel.resetLoginResponse()
            }

            is Result.Success -> {
                loginViewModel.resetLoginResponse()
            }

            else -> {
                // ignore
            }
        }
    }

    LoginContent(
        emailTextFieldState = loginViewModel.emailTextFieldState,
        onEmailInputChanged = { loginViewModel.updateEmailInput(it) },
        passwordTextFieldState = loginViewModel.passwordTextFieldState,
        onPasswordInputChanged = { loginViewModel.updatePasswordInput(it) },
        onLogin = loginViewModel::validateDataAndLogin,
        loginActive = loginResponse.loading,
        navigateToForgotPassword = navigateToForgotPassword,
        navigateToSignUp = navigateToSignUp,
    )

    errorForDialog?.let { errorMessage ->
        val closeDialog = { errorForDialog = null }
        AppAlertDialog(
            title = stringResource(id = R.string.error),
            subtitle = errorMessage,
            onDismiss = closeDialog,
            onConfirm = closeDialog
        )
    }
}

@Composable
fun LoginContent(
    emailTextFieldState: TextFieldState,
    onEmailInputChanged: (TextFieldValue) -> Unit,
    passwordTextFieldState: TextFieldState,
    onPasswordInputChanged: (TextFieldValue) -> Unit,
    onLogin: () -> Unit,
    navigateToForgotPassword: () -> Unit,
    navigateToSignUp: () -> Unit,
    loginActive: Boolean,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Image(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                painter = painterResource(id = R.drawable.ic_logo_login),
                contentDescription = "Logo image"
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                text = stringResource(id = R.string.login_title),
                style = AppTypography.title2SemiBold28,
                color = AppColor.dType1,
            )

            Spacer(modifier = Modifier.weight(0.9f))

            LoginMiddleContent(
                modifier = Modifier.padding(top = 16.dp),
                emailTextFieldState = emailTextFieldState,
                onEmailInputChanged = onEmailInputChanged,
                passwordTextFieldState = passwordTextFieldState,
                onPasswordInputChanged = onPasswordInputChanged,
                inputsEnabled = !loginActive,
                navigateToForgotPassword = navigateToForgotPassword,
                navigateToSignUp = navigateToSignUp
            )

            Spacer(modifier = Modifier.weight(1f))

            AppFilledButton(
                text = stringResource(id = R.string.login),
                progressLoading = loginActive,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                onClick = onLogin
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
fun LoginMiddleContent(
    emailTextFieldState: TextFieldState,
    onEmailInputChanged: (TextFieldValue) -> Unit,
    passwordTextFieldState: TextFieldState,
    onPasswordInputChanged: (TextFieldValue) -> Unit,
    navigateToForgotPassword: () -> Unit,
    navigateToSignUp: () -> Unit,
    inputsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppTextField(
            modifier = Modifier.padding(horizontal = 16.dp),
            modifierTextField = Modifier.autofill(
                autofillTypes = listOf(AutofillType.EmailAddress),
                onFill = { onEmailInputChanged(TextFieldValue(it, TextRange(it.length))) }
            ),
            enabled = inputsEnabled,
            label = stringResource(id = R.string.auth_email_input_title_text),
            maxLength = integerResource(id = R.integer.input_length_max_email),
            maxLines = 1,
            textFieldState = emailTextFieldState,
            onChanged = onEmailInputChanged,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        AppTextField(
            modifier = Modifier.padding(horizontal = 16.dp),
            enabled = inputsEnabled,
            label = stringResource(id = R.string.auth_password_input_title_text),
            maxLength = integerResource(id = R.integer.input_length_max_password),
            maxLines = 1,
            textFieldState = passwordTextFieldState,
            onChanged = onPasswordInputChanged,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppTextButton(
                onClick = navigateToForgotPassword,
                text = stringResource(id = R.string.login_forgot_password_btn_text),
                textColor = AppColor.dType1
            )
            Spacer(modifier = Modifier.width(16.dp))
            AppTextButton(
                onClick = navigateToSignUp,
                text = stringResource(id = R.string.auth_sign_up_btn_text)
            )
        }
    }
}

@Composable
@Preview
fun LoginContentPreview() {
    AppTheme {
        AppGradientBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                LoginContent(
                    emailTextFieldState = TextFieldState().apply {
                        inputText = TextFieldValue("email@test")
                        errorText = "Invalid email"
                    },
                    onEmailInputChanged = { },
                    passwordTextFieldState = TextFieldState().apply {
                        inputText = TextFieldValue("123456")
                        errorText = null
                    },
                    onPasswordInputChanged = { },
                    onLogin = { },
                    loginActive = false,
                    navigateToSignUp = {},
                    navigateToForgotPassword = {}
                )
            }
        }
    }
}