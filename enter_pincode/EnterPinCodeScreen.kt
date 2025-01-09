package com.timelimiter.android.features.enter_pincode

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timelimiter.android.common_ui.components.AppAlertDialog
import com.timelimiter.android.common_ui.components.AppFilledButton
import com.timelimiter.android.common_ui.components.AppTopAppBar
import com.timelimiter.android.common_ui.components.CheckCodeInputField
import com.timelimiter.android.common_ui.components.TopAppBarIcon
import com.timelimiter.android.common_ui.theme.AppColor
import com.timelimiter.android.common_ui.theme.AppTheme
import com.timelimiter.android.core.result.Result
import com.timelimiter.android.features.R

@Composable
fun EnterPinCodeScreen(
    enterPinCodeViewModel: EnterPinCodeViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPinCodeEntered: () -> Unit
) {

    val approvePinCodeResponse by enterPinCodeViewModel.approvePinCodeResponse.collectAsStateWithLifecycle()
    var showForgotPinCodeDialog by rememberSaveable { mutableStateOf(false) }
    val closeForgotPinCodeDialog = {
        showForgotPinCodeDialog = false
    }

    LaunchedEffect(approvePinCodeResponse) {
        val result = approvePinCodeResponse
        if (result is Result.Success) {
            if (result.data) {
                onPinCodeEntered.invoke()
            } else {
                onBack.invoke()
            }
        }
    }

    EnterPinCodeContent(
        code = enterPinCodeViewModel.inputCode,
        onBack = onBack,
        onCodeChanged = enterPinCodeViewModel::updateInputCode,
        onEnterPinCode = enterPinCodeViewModel::approvePinCode,
        onForgotPinCode = { showForgotPinCodeDialog = true }
    )

    if (showForgotPinCodeDialog) {
        AppAlertDialog(
            title = stringResource(R.string.enter_pin_code_dialog_title),
            subtitle = stringResource(R.string.enter_pin_code_dialog_subtitle),
            confirmButton = stringResource(R.string.enter_pin_code_dialog_confirm_btn),
            onConfirm = closeForgotPinCodeDialog
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EnterPinCodeContent(
    code: String,
    onBack: () -> Unit,
    onCodeChanged: (String) -> Unit,
    onEnterPinCode: () -> Unit,
    onForgotPinCode: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.parental_control_title),
                topAppBarIcon = TopAppBarIcon.BackIcon,
                onNavigationClick = onBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 504.dp)
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                CheckCodeInputField(
                    code = code,
                    onCodeChanged = onCodeChanged
                )
                Text(
                    modifier = Modifier
                        .padding(vertical = 24.dp)
                        .clickable(onClick = onForgotPinCode)
                        .align(Alignment.End),
                    text = stringResource(R.string.enter_pin_code_forgot_pin_code),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColor.Text1,
                )
                Spacer(modifier = Modifier.weight(1f))
                AppFilledButton(
                    text = stringResource(R.string.enter_pin_code_btn),
                    onClick = onEnterPinCode,
                    enabled = code.length == 6
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EnterPinCodeContentPreview() {
    AppTheme {
        EnterPinCodeContent(
            code = "1234",
            onBack = {},
            onCodeChanged = {},
            onEnterPinCode = {},
            onForgotPinCode = {}
        )
    }
}