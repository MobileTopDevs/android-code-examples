package com.zippe.client.features.auth.password

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zippe.client.common_ui.components.AppAlertDialog
import com.zippe.client.common_ui.components.AppAlertDialogNetworkIssue
import com.zippe.client.common_ui.components.AppOutlinedButton
import com.zippe.client.common_ui.components.AppTextField
import com.zippe.client.common_ui.components.AppTopAppBar
import com.zippe.client.common_ui.components.TopAppBarIcon
import com.zippe.client.common_ui.state.TextFieldState
import com.zippe.client.common_ui.theme.AppColor
import com.zippe.client.common_ui.theme.AppTheme
import com.zippe.client.common_ui.theme.AppTypography
import com.zippe.client.core.errors.NetworkException
import com.zippe.client.core.result.Result
import com.zippe.client.core.result.loading
import com.zippe.client.core.util.digits
import com.zippe.client.features.R
import com.zippe.client.features.auth.password.validator.Validators

@Composable
fun EnterPasswordScreen(
    enterPasswordViewModel: EnterPasswordViewModel = hiltViewModel(),
    navigateToVerificationScreen: (phone: String, password: String) -> Unit,
    navigateUp: () -> Unit
) {
    val requestResponse by enterPasswordViewModel.requestResponse.collectAsStateWithLifecycle()

    var errorForDialog by rememberSaveable { mutableStateOf<Throwable?>(null) }
    var showResetPasswordSuccessDlg by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = requestResponse) {
        when (val result = requestResponse) {
            is Result.Error -> {
                errorForDialog = result.exception
                enterPasswordViewModel.resetRequestResponse()
            }

            is Result.Success -> {
                if (enterPasswordViewModel.isSignUpFlow) {
                    navigateToVerificationScreen(
                        enterPasswordViewModel.phone!!,
                        enterPasswordViewModel.passwordTextFieldState.inputText.text
                    )
                } else {
                    showResetPasswordSuccessDlg = true
                }

                enterPasswordViewModel.resetRequestResponse()
            }

            else -> {
                // ignore
            }
        }
    }

    EnterPasswordContent(
        navigateUp = navigateUp,
        passwordTextFieldState = enterPasswordViewModel.passwordTextFieldState,
        onPasswordInputChanged = { enterPasswordViewModel.updatePasswordInput(it) },
        repeatPasswordTextFieldState = enterPasswordViewModel.passwordRepeatTextFieldState,
        onRepeatPasswordInputChanged = { enterPasswordViewModel.updatePasswordRepeatInput(it) },
        isSignUpFlow = enterPasswordViewModel.isSignUpFlow,
        onMakeRequest = enterPasswordViewModel::validateDataAndMakeRequest,
        requesting = requestResponse.loading
    )

    errorForDialog?.let { exception ->
        val closeDialog = { errorForDialog = null }
        if (exception is NetworkException) {
            AppAlertDialogNetworkIssue(onDismiss = closeDialog)
        } else {
            AppAlertDialog(
                title = stringResource(id = R.string.error),
                subtitle = exception.message ?: exception.toString(),
                onDismiss = closeDialog,
                onPositiveBtnClicked = closeDialog
            )
        }
    }

    if (showResetPasswordSuccessDlg) {
        val closeDialog = {
            showResetPasswordSuccessDlg = false
            navigateUp()
        }
        AppAlertDialog(
            title = stringResource(R.string.restore_password_success_dlg_title),
            subtitle = stringResource(R.string.restore_password_success_dlg_subtitle),
            onDismiss = closeDialog,
            onPositiveBtnClicked = closeDialog
        )
    }
}

@Composable
fun EnterPasswordContent(
    navigateUp: () -> Unit,
    passwordTextFieldState: TextFieldState,
    onPasswordInputChanged: (TextFieldValue) -> Unit,
    repeatPasswordTextFieldState: TextFieldState,
    onRepeatPasswordInputChanged: (TextFieldValue) -> Unit,
    isSignUpFlow: Boolean,
    onMakeRequest: () -> Unit,
    requesting: Boolean
) {
    Scaffold(
        topBar = {
            AppTopAppBar(
                topAppBarIcon = TopAppBarIcon.BackIcon,
                onNavigationClick = navigateUp,
                title = stringResource(
                    id = if (isSignUpFlow) {
                        R.string.sign_up_title_text
                    } else {
                        R.string.restore_password_title
                    }
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(
                    id = if (isSignUpFlow) {
                        R.string.enter_password_title
                    } else {
                        R.string.enter_password_new
                    }
                ),
                style = AppTypography.androidHeadline5,
                color = AppColor.gray14
            )
            Spacer(modifier = Modifier.height(32.dp))
            AppTextField(
                maxLength = integerResource(id = R.integer.input_length_max_password),
                maxLines = 1,
                textFieldState = passwordTextFieldState,
                onChanged = onPasswordInputChanged,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = stringResource(id = R.string.enter_password_text_field_create_password_placeholder)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppTextField(
                maxLength = integerResource(id = R.integer.input_length_max_password),
                maxLines = 1,
                textFieldState = repeatPasswordTextFieldState,
                onChanged = onRepeatPasswordInputChanged,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = stringResource(id = R.string.enter_password_text_field_confirm_password_placeholder)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Column {
                val text = passwordTextFieldState.inputText.text
                val passwordMinLength = integerResource(id = R.integer.input_length_min_password)
                PasswordRule(
                    checked = text.length >= passwordMinLength,
                    text = stringResource(id = R.string.enter_password_rules_length)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PasswordRule(
                    checked =  text.any { !it.isDigit() && it.isLowerCase() },
                    text = stringResource(id = R.string.enter_password_rules_lower_case)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PasswordRule(
                    checked =  text.any { !it.isDigit() && it.isUpperCase() },
                    text = stringResource(id = R.string.enter_password_rules_upper_case)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PasswordRule(
                    checked = text.any { it.isDigit() },
                    text = stringResource(id = R.string.enter_password_rules_number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PasswordRule(
                    checked = text.any { Validators.Password.SPECIAL_CHARACTERS.contains(it) },
                    text = stringResource(id = R.string.enter_password_rules_special_char)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))
            AppOutlinedButton(
                text = stringResource(
                    id = if (isSignUpFlow) {
                        R.string.continue_text
                    } else {
                        R.string.enter_password_set_new_btn_text
                    }
                ),
                modifier = Modifier.fillMaxWidth(),
                onClick = onMakeRequest,
                progressLoading = requesting,
                enabled = passwordTextFieldState.inputText.text.isNotEmpty() && repeatPasswordTextFieldState.inputText.text.isNotEmpty()
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PasswordRule(checked: Boolean, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(
                id = if (checked) R.drawable.ic_checked else R.drawable.ic_dot
            ),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = AppTypography.androidBody2,
            color = if (checked) AppColor.colors3 else AppColor.blueGray7
        )
    }
}

@Composable
@Preview
fun RestorePasswordContentPreview() {
    AppTheme {
        Surface {
            EnterPasswordContent(
                navigateUp = { },
                passwordTextFieldState = TextFieldState(),
                onPasswordInputChanged = { },
                repeatPasswordTextFieldState = TextFieldState(),
                onRepeatPasswordInputChanged = { },
                isSignUpFlow = true,
                onMakeRequest = { },
                requesting = false
            )
        }
    }
}

@Composable
@Preview
fun RestorePasswordForgotContentPreview() {
    AppTheme {
        Surface {
            EnterPasswordContent(
                navigateUp = { },
                passwordTextFieldState = TextFieldState(),
                onPasswordInputChanged = { },
                repeatPasswordTextFieldState = TextFieldState(),
                onRepeatPasswordInputChanged = { },
                isSignUpFlow = false,
                onMakeRequest = { },
                requesting = false
            )
        }
    }
}