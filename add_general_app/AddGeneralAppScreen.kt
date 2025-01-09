package com.timelimiter.android.features.add_general_app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.rememberAsyncImagePainter
import com.timelimiter.android.common_ui.components.AppTopAppBar
import com.timelimiter.android.common_ui.components.CheckableAppSettingsItem
import com.timelimiter.android.common_ui.components.SimpleSearchBar
import com.timelimiter.android.common_ui.components.ToolbarIcon
import com.timelimiter.android.common_ui.components.TopAppBarIcon
import com.timelimiter.android.core.result.Result
import com.timelimiter.android.entities.apps.ItemApp
import com.timelimiter.android.features.R

@Composable
fun AddGeneralAppScreen(
    addGeneralAppViewModel: AddGeneralAppViewModel = hiltViewModel(),
    onBack: () -> Unit
) {

    val lifecycleOwner = LocalLifecycleOwner.current
    val apps by addGeneralAppViewModel.appsWithSettings.collectAsStateWithLifecycle()
    val search by addGeneralAppViewModel.search.collectAsStateWithLifecycle()
    val saveAppsResponse by addGeneralAppViewModel.saveAppsResponse.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            addGeneralAppViewModel.getApps()
        }
    }

    LaunchedEffect(saveAppsResponse) {
        val result = saveAppsResponse
        if (result is Result.Success) {
            onBack()
        }
    }

    AddGeneralAppContent(
        apps = apps,
        onBack = onBack,
        onSave = addGeneralAppViewModel::save,
        search = search,
        onSearchTextChanged = addGeneralAppViewModel::updateSearchText,
        onAppChecked = addGeneralAppViewModel::preSaveApp
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddGeneralAppContent(
    apps: List<ItemApp>?,
    onBack: () -> Unit,
    onSave: () -> Unit,
    search: TextFieldValue,
    onSearchTextChanged: (TextFieldValue) -> Unit,
    onAppChecked: (ItemApp) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.add_app_title),
                topAppBarIcon = TopAppBarIcon.BackIcon,
                onNavigationClick = onBack,
                actions = {
                    ToolbarIcon(
                        topAppBarIcon = TopAppBarIcon.SaveIcon,
                        onIconClick = onSave
                    )
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
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 504.dp)
                    .align(Alignment.TopCenter),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item("subtitle") {
                    Text(text = stringResource(id = R.string.add_app_subtitle))
                }
                item("search") {
                    SimpleSearchBar(
                        search = search,
                        hintResId = R.string.add_app_search_hint,
                        onSearchTextChanged = onSearchTextChanged
                    )
                }
                apps?.let {
                    items(it) { app ->
                        CheckableAppSettingsItem(
                            painter = rememberAsyncImagePainter(model = app.icon),
                            title = app.label,
                            checked = app.settings.visibleForGeneralApps,
                            onCheckedChange = { checked ->
                                onAppChecked.invoke(
                                    app.copy(
                                        settings = app.settings.copy(visibleForGeneralApps = checked)
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}