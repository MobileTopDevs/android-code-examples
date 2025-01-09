package com.timelimiter.android.features.general_aaps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.rememberAsyncImagePainter
import com.timelimiter.android.common_ui.components.AppAlertDialog
import com.timelimiter.android.common_ui.components.AppFilledButtonSmall
import com.timelimiter.android.common_ui.components.AppTimeRangePicker
import com.timelimiter.android.common_ui.components.AppTopAppBar
import com.timelimiter.android.common_ui.components.DeletableAppSettingsItem
import com.timelimiter.android.common_ui.components.TopAppBarIcon
import com.timelimiter.android.common_ui.theme.AppColor
import com.timelimiter.android.entities.apps.ItemApp
import com.timelimiter.android.features.R

@Composable
fun GeneralAppsScreen(
    parentalControlViewModel: GeneralAppsViewModel = hiltViewModel(),
    onAddApp: () -> Unit,
    onBack: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val apps by parentalControlViewModel.appsWithSettings.collectAsStateWithLifecycle()
    val appProcessing = parentalControlViewModel.appProcessing
    val pickerStartTime = parentalControlViewModel.initialStartTimeMinutes
    val pickerEndTime = parentalControlViewModel.initialEndTimeMinutes

    LaunchedEffect(key1 = true) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            parentalControlViewModel.getApps()
        }
    }

    LaunchedEffect(key1 = true) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            parentalControlViewModel.getPickerTime()
        }
    }

    appProcessing?.let { app ->
        AppAlertDialog(
            title = stringResource(id = R.string.parental_control_dialog_title),
            subtitle = stringResource(id = R.string.parental_control_dialog_subtitle),
            confirmButton = stringResource(id = R.string.parental_control_dialog_confirm_btn),
            cancelButton = stringResource(id = R.string.parental_control_dialog_cancel_btn),
            confirmButtonColors = ButtonDefaults.buttonColors(
                containerColor = AppColor.Red,
                contentColor = Color.White,
                disabledContentColor = Color.White.copy(alpha = .35f),
                disabledContainerColor = AppColor.Blue.copy(alpha = .35f)
            ),
            onConfirm = {
                parentalControlViewModel.setVisible(
                    packageManager = app.packageName,
                    flag = false
                )
                parentalControlViewModel.processApp(null)
            },
            onCancel = {
                parentalControlViewModel.processApp(null)
            }
        )
    }

    GeneralAppsContent(
        apps = apps,
        startTimeMinutes = pickerStartTime,
        endTimeMinutes = pickerEndTime,
        onDeleteApp = parentalControlViewModel::processApp,
        onAddApp = onAddApp,
        onBack = onBack,
        onStartTimeChange = parentalControlViewModel::setPickerStartTime,
        onEndTimeChange = parentalControlViewModel::setPickerEndTime
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GeneralAppsContent(
    apps: List<ItemApp>?,
    startTimeMinutes: Int?,
    endTimeMinutes: Int?,
    onDeleteApp: (ItemApp) -> Unit,
    onAddApp: () -> Unit,
    onBack: () -> Unit,
    onStartTimeChange: ((Int) -> Unit)? = null,
    onEndTimeChange: ((Int) -> Unit)? = null
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.general_apps_title),
                topAppBarIcon = TopAppBarIcon.BackIcon,
                onNavigationClick = onBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            Column(

                modifier = Modifier
                    .widthIn(max = 504.dp)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (startTimeMinutes != null && endTimeMinutes != null) {
                    AppTimeRangePicker(
                        modifier = Modifier
                            .widthIn(max = 360.dp)
                            .padding(horizontal = 42.dp),
                        startTimeMinutes = startTimeMinutes,
                        endTimeMinutes = endTimeMinutes,
                        onStartTimeChange = onStartTimeChange,
                        onEndTimeChange = onEndTimeChange
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.parental_control_list_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    AppFilledButtonSmall(
                        text = stringResource(id = R.string.parental_control_add_app_btn_text),
                        iconContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_plus),
                                contentDescription = null
                            )
                        },
                        onClick = onAddApp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                apps?.let {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(it, { app -> app.packageName }) { app ->
                            DeletableAppSettingsItem(
                                painter = rememberAsyncImagePainter(model = app.icon),
                                title = app.label,
                                onClick = { onDeleteApp(app) }
                            )
                        }
                    }
                } ?: Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(.7f)
                            .align(Alignment.Center),
                        text = stringResource(R.string.parental_control_no_app_text),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColor.Text1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}