package com.timelimiter.android.features.set_pincode

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.timelimiter.android.common_ui.components.AppAlertDialog
import com.timelimiter.android.common_ui.components.AppFilledButton
import com.timelimiter.android.common_ui.components.AppTopAppBar
import com.timelimiter.android.common_ui.components.CheckCodeInputField
import com.timelimiter.android.common_ui.components.TopAppBarIcon
import com.timelimiter.android.common_ui.components.Warning
import com.timelimiter.android.common_ui.theme.AppTheme
import com.timelimiter.android.features.R

@Composable
fun SetPinCodeScreen(
    setPinCodeViewModel: SetPinCodeViewModel = hiltViewModel(),
) {


    var showDialog by rememberSaveable { mutableStateOf(true) }
    val closeDialog = {
        showDialog = false
    }

    if (showDialog) {
        AppAlertDialog(
            title = stringResource(R.string.set_pin_code_dialog_title),
            subtitle = stringResource(R.string.set_pin_code_dialog_subtitle),
            confirmButton = stringResource(R.string.set_pin_code_dialog_confirm_btn),
            onConfirm = closeDialog
        )
    }

    SetPinCodeContent(
        code = setPinCodeViewModel.inputCode,
        onCodeChanged = setPinCodeViewModel::updateInputCode,
        onCreatePinCode = setPinCodeViewModel::saveCode
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SetPinCodeContent(
    code: String,
    onCodeChanged: (String) -> Unit,
    onCreatePinCode: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.parental_control_title),
                topAppBarIcon = TopAppBarIcon.NoneIcon,
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
                Spacer(modifier = Modifier.height(24.dp))
                Warning(
                    title = stringResource(R.string.set_pin_code_warning_title),
                    subtitle = stringResource(R.string.set_pin_code_warning_subtitle)
                )
                Spacer(modifier = Modifier.weight(1f))
                AppFilledButton(
                    text = stringResource(R.string.set_pin_code_btn),
                    onClick = onCreatePinCode,
                    enabled = code.length == 6
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SetPinCodeContentPreview() {
    AppTheme {
        SetPinCodeContent(
            code = "1234",
            onCodeChanged = {},
            onCreatePinCode = {}
        )
    }
}