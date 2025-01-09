package com.timelimiter.android.features.wallpapers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timelimiter.android.common_ui.components.AppTopAppBar
import com.timelimiter.android.common_ui.components.CustomBackground
import com.timelimiter.android.common_ui.components.SelectWallpaperButton
import com.timelimiter.android.common_ui.components.TopAppBarIcon
import com.timelimiter.android.common_ui.components.getWallpaperList
import com.timelimiter.android.common_ui.theme.AppColor
import com.timelimiter.android.features.R

@Composable
fun WallpapersScreen(
    wallpapersViewModel: WallpapersViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val wallpapers = getWallpaperList()
    val wpIndex by wallpapersViewModel.wpIndex.collectAsStateWithLifecycle()
    var selected by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(wpIndex) {
        selected = wpIndex
    }

    WallpapersContent(
        wallpapers = wallpapers,
        selected = selected,
        onSelect = wallpapersViewModel::setWallpaperIndex,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WallpapersContent(
    wallpapers: List<CustomBackground>,
    selected: Int,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.wallpapers_title),
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "title", span = { GridItemSpan(3) }) {
                    Text(
                        modifier = Modifier.padding(bottom = 24.dp),
                        text = stringResource(R.string.wallpapers_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColor.Text1
                    )
                }
                itemsIndexed(wallpapers) { index, theme ->
                    SelectWallpaperButton(
                        index = index,
                        wallpaper = theme,
                        selected = index == selected,
                        onSelect = onSelect
                    )
                }
            }
        }
    }
}