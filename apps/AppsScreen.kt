package com.timelimiter.android.features.apps

import android.app.Activity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.timelimiter.android.common_ui.components.AppIcon
import com.timelimiter.android.common_ui.components.AppPlaceholder
import com.timelimiter.android.common_ui.components.AppTopAppBar
import com.timelimiter.android.common_ui.components.PaidDialog
import com.timelimiter.android.common_ui.components.ToolbarIcon
import com.timelimiter.android.common_ui.components.TopAppBarIcon
import com.timelimiter.android.common_ui.state.DragTarget
import com.timelimiter.android.common_ui.state.DraggableArea
import com.timelimiter.android.common_ui.theme.AppTheme
import com.timelimiter.android.common_ui.utils.IntentUtils.openApp
import com.timelimiter.android.entities.apps.ItemAppSettings
import com.timelimiter.android.entities.apps.ItemsScreen
import com.timelimiter.android.features.R
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun AppsScreen(
    appsViewModel: AppsViewModel = hiltViewModel(),
    navigateToWallpapers: () -> Unit,
    navigateToSettings: () -> Unit
) {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val screens by appsViewModel.screens.collectAsStateWithLifecycle()
    val timeString by appsViewModel.timeString.collectAsStateWithLifecycle()
    val isFirstLaunch by appsViewModel.isFirstLaunch.collectAsStateWithLifecycle()
    val showPaidDialog = appsViewModel.showPaidDialog
    val productDetails = appsViewModel.productDetails

    val onBackPressedCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // ignore back press to prevent back action
            }
        }
    }

    DisposableEffect(lifecycleOwner, backPressedDispatcher) {
        backPressedDispatcher?.addCallback(
            lifecycleOwner,
            onBackPressedCallback
        )
        onDispose {
            onBackPressedCallback.remove()
        }
    }

    LaunchedEffect(key1 = true) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            appsViewModel.refreshBillingInfo()
        }
    }

    LaunchedEffect(key1 = true) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            appsViewModel.getApps()
        }
    }

    LaunchedEffect(key1 = true) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            appsViewModel.checkIsPaid()
        }
    }

    if (showPaidDialog && productDetails != null) {
        val price = productDetails.oneTimePurchaseOfferDetails?.formattedPrice
        PaidDialog(
            price = price,
            title = stringResource(R.string.apps_paid_dialog_title),
            subtitle = stringResource(R.string.apps_paid_dialog_subtitle),
            confirmText = stringResource(R.string.apps_paid_dialog_confirm_btn),
            cancelText = stringResource(R.string.apps_paid_dialog_cancel_btn),
            onConfirm = {
                appsViewModel.closePaidDialog()
                appsViewModel.launchBillingFlow(context as Activity, productDetails)
            },
            onDismiss = appsViewModel::closePaidDialog
        )
    }

    AppsContent(
        screens = screens,
        title = timeString,
        onWallpapers = navigateToWallpapers,
        onSettings = {
            appsViewModel.setFirstLaunch()
            navigateToSettings.invoke()
        },
        onAppClick = { packageName ->
            openApp(context, packageName)
        },
        onDragAndDrop = { settings1, settings2 ->
            if (settings1 != null && settings2 != null && settings1 != settings2) {
                appsViewModel.swapIndexes(settings1, settings2)
            }
        },
        isFirstLaunch = isFirstLaunch
    )
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun AppsContent(
    screens: List<ItemsScreen>?,
    title: String,
    onWallpapers: () -> Unit,
    onSettings: () -> Unit,
    onAppClick: (packageName: String) -> Unit,
    onDragAndDrop: (settings1: ItemAppSettings?, settings2: ItemAppSettings?) -> Unit,
    isFirstLaunch: Boolean
) {
    AppTheme {
        val density = LocalDensity.current
        val screenWidthPx =
            with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
        val pagerState = rememberPagerState { screens?.size ?: 0 }
        val scope = rememberCoroutineScope()

        DraggableArea(
            modifier = Modifier,
            onPositionInWindow = { offset, size ->
                scope.launch {
                    if (offset.x <= -size.width * .1f && pagerState.currentPage > 0) {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                    if (offset.x >= screenWidthPx - size.width * .9f) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    AppTopAppBar(
                        modifier = Modifier,
                        title = title,
                        topAppBarIcon = TopAppBarIcon.ThemeIcon,
                        onNavigationClick = onWallpapers,
                        actions = {
                            ToolbarIcon(
                                topAppBarIcon = TopAppBarIcon.SettingsIcon,
                                onIconClick = onSettings
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
                    screens?.let {
                        HorizontalPager(
                            modifier = Modifier.fillMaxSize(),
                            state = pagerState,
                            beyondBoundsPageCount = it.size,
                            contentPadding = PaddingValues(
                                bottom = 48.dp,
                                start = 8.dp,
                                end = 8.dp
                            )
                        ) { page ->
                            var gridSize by remember { mutableStateOf(IntSize.Zero) }
                            val iconHeight = 100.dp
                            LazyVerticalGrid(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .onGloballyPositioned { coordinates ->
                                        gridSize = coordinates.size
                                    },
                                columns = GridCells.Fixed(ItemsScreen.COLUMNS),
                                verticalArrangement = Arrangement.spacedBy(
                                    space = getIconVerticalSpace(
                                        density = density,
                                        gridSize = gridSize,
                                        iconHeight = iconHeight,
                                        rowsCount = ItemsScreen.ROWS
                                    ),
                                    alignment = Alignment.Top
                                ),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                try {
                                    items(
                                        items = it[page].items,
                                        key = { app -> "${app.packageName} ${app.settings.orderIndex}" },
                                        contentType = { app -> "${app.packageName} ${app.settings.orderIndex}" }
                                    ) { app ->
                                        DragTarget(
                                            modifier = Modifier.animateItemPlacement(),
                                            data = app,
                                            onDrop = onDragAndDrop
                                        ) {
                                            AppIcon(
                                                modifier = Modifier.height(iconHeight),
                                                painter = rememberAsyncImagePainter(model = app.icon),
                                                title = app.label,
                                                onClick = {
                                                    onAppClick(app.packageName)
                                                }
                                            )
                                        }
                                    }
                                } catch (e: IndexOutOfBoundsException) {
                                    // avoid lazy grid error while pager recomposition
                                    Timber.e(e)
                                }
                            }
                        }

                        HorizontalPagerIndicator(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            pagerState = pagerState,
                            pageCount = it.size
                        )
                    }
                        ?: AppPlaceholder(
                            modifier = Modifier.align(Alignment.Center),
                            title = if (isFirstLaunch) {
                                stringResource(R.string.apps_placeholder_title_first)
                            } else {
                                stringResource(R.string.apps_placeholder_title_second)
                            },
                            subtitle = if (isFirstLaunch) {
                                stringResource(id = R.string.apps_placeholder_subtitle_first)
                            } else {
                                null
                            }
                        )
                }
            }
        }
    }
}

private fun getIconVerticalSpace(
    density: Density,
    gridSize: IntSize,
    iconHeight: Dp,
    rowsCount: Int
): Dp {
    val gridHeightInDp = with(density) { gridSize.height.toDp() }
    return (gridHeightInDp - iconHeight * rowsCount) / rowsCount
}
