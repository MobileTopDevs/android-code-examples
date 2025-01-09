package com.timelimiter.android.features.parental_control

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.timelimiter.android.common_ui.components.AppMenuItem
import com.timelimiter.android.common_ui.components.AppTopAppBar
import com.timelimiter.android.common_ui.components.AppsSectionCard
import com.timelimiter.android.common_ui.components.ToolbarIcon
import com.timelimiter.android.common_ui.components.TopAppBarIcon
import com.timelimiter.android.common_ui.theme.AppColor
import com.timelimiter.android.common_ui.utils.IntentUtils
import com.timelimiter.android.core.PRIVACY_POLICY
import com.timelimiter.android.entities.apps.ItemApp
import com.timelimiter.android.features.R

@Composable
fun ParentalControlScreen(
    parentalControlViewModel: ParentalControlViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onTopApps: () -> Unit,
    onGeneralApps: () -> Unit,
    onGames: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val apps by parentalControlViewModel.appsWithSettings.collectAsStateWithLifecycle()
    val pickerGeneralAppsStartTime by parentalControlViewModel.initialGeneralAppsStartTimeMinutes.collectAsStateWithLifecycle()
    val pickerGeneralAppsEndTime by parentalControlViewModel.initialGeneralAppsEndTimeMinutes.collectAsStateWithLifecycle()
    val pickerGamesStartTime by parentalControlViewModel.initialGamesStartTimeMinutes.collectAsStateWithLifecycle()
    val pickerGamesEndTime by parentalControlViewModel.initialGamesEndTimeMinutes.collectAsStateWithLifecycle()

    val defaultLauncherRequest =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val activity = context as Activity
                activity.finishAndRemoveTask()
            }
        }

    LaunchedEffect(key1 = true) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            parentalControlViewModel.getApps()
        }
    }

    ParentalControlContent(
        generalApps = apps?.filter { it.settings.visibleForGeneralApps },
        games = apps?.filter { it.settings.visibleForGames },
        generalAppsPickerStartTime = pickerGeneralAppsStartTime,
        generalAppsPickerEndTime = pickerGeneralAppsEndTime,
        gamesPickerStartTime = pickerGamesStartTime,
        gamesPickerEndTime = pickerGamesEndTime,
        onBack = onBack,
        onTopApps = onTopApps,
        onDefault = { requestDefaultHomeApp(context, defaultLauncherRequest) },
        onPolicy = { IntentUtils.openLink(context, PRIVACY_POLICY) },
        onGeneralApps = onGeneralApps,
        onGames = onGames
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ParentalControlContent(
    generalApps: List<ItemApp>?,
    games: List<ItemApp>?,
    generalAppsPickerStartTime: Int,
    generalAppsPickerEndTime: Int,
    gamesPickerStartTime: Int,
    gamesPickerEndTime: Int,
    onBack: () -> Unit,
    onTopApps: () -> Unit,
    onPolicy: () -> Unit,
    onDefault: () -> Unit,
    onGeneralApps: () -> Unit,
    onGames: () -> Unit
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            var showMenu by remember { mutableStateOf(false) }
            AppTopAppBar(
                title = stringResource(id = R.string.parental_control_title),
                topAppBarIcon = TopAppBarIcon.BackIcon,
                onNavigationClick = onBack,
                actions = {
                    ToolbarIcon(
                        topAppBarIcon = TopAppBarIcon.HintIcon,
                        onIconClick = { showMenu = !showMenu }
                    )
                    DropdownMenu(
                        modifier = Modifier.background(AppColor.BlueLight),
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        /*AppMenuItem(
                            text = stringResource(R.string.parental_control_menu_top_apps),
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_game),
                                    contentDescription = null,
                                    tint = AppColor.BlueDark
                                )
                            },
                            onClick = onTopApps
                        )*/
                        AppMenuItem(
                            text = stringResource(R.string.parental_control_menu_policy),
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_document),
                                    contentDescription = null,
                                    tint = AppColor.BlueDark
                                )
                            },
                            onClick = onPolicy
                        )
                        Divider(Modifier.padding(vertical = 8.dp))
                        AppMenuItem(
                            text = stringResource(R.string.parental_control_menu_top_set_default),
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_quit),
                                    contentDescription = null
                                )
                            },
                            onClick = onDefault
                        )
                    }
                }
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
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    AppsSectionCard(
                        title = stringResource(R.string.general_apps_title),
                        startTime = generalAppsPickerStartTime,
                        endTime = generalAppsPickerEndTime,
                        onClick = onGeneralApps,
                        apps = generalApps
                    )
                    AppsSectionCard(
                        title = stringResource(R.string.games_title),
                        startTime = gamesPickerStartTime,
                        endTime = gamesPickerEndTime,
                        onClick = onGames,
                        apps = games
                    )
                }
            }
        }
    }
}

private fun requestDefaultHomeApp(
    context: Context,
    defaultLauncherRequest: ActivityResultLauncher<Intent>
) {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            val roleManager = context.getSystemService<RoleManager>()
            val roleName = RoleManager.ROLE_HOME

            defaultLauncherRequest.launch(
                roleManager?.let {
                    if (it.isRoleAvailable(roleName) && !it.isRoleHeld(roleName)) {
                        it.createRequestRoleIntent(roleName)
                    } else {
                        Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    }
                }
            )
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
            defaultLauncherRequest.launch(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            )
        }

        else -> {
            defaultLauncherRequest.launch(
                Intent(
                    Settings.ACTION_HOME_SETTINGS
                )
            )
        }
    }
}

fun isDefaultLauncher(context: Context): Boolean {
    val packageName = context.packageName
    val defaultPackageName = context.packageManager.resolveActivity(
        Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) },
        PackageManager.MATCH_DEFAULT_ONLY
    )?.activityInfo?.packageName
    return defaultPackageName == packageName
}