package com.timelimiter.android.features.top_apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timelimiter.android.common_ui.components.AppTopAppBar
import com.timelimiter.android.common_ui.components.TopAppBarIcon
import com.timelimiter.android.common_ui.components.TopAppCard
import com.timelimiter.android.common_ui.theme.AppColor
import com.timelimiter.android.entities.top_app.TopApp
import com.timelimiter.android.features.R

@Composable
fun TopAppsScreen(
    topAppsViewModel: TopAppsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    TopAppsContent(
        onBack = onBack,
        topAppList = topApps
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TopAppsContent(
    topAppList: List<TopApp>,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopAppBar(
                title = stringResource(R.string.top_app_title),
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
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 504.dp)
                    .align(Alignment.TopCenter),
                contentPadding = PaddingValues(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            modifier = Modifier.fillMaxWidth(),
                            painter = painterResource(R.drawable.logo_timelimiter),
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = stringResource(R.string.top_app_logo_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColor.Text2,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                items(topAppList) { TopAppCard(topApp = it) }
            }
        }
    }
}

val topApps = listOf(
    TopApp(
        imageRes = R.drawable.top_app_android_police,
        titleRes = R.string.top_app_android_police_title,
        url = "https://www.androidpolice.com/best-kids-games-android/#hidden-through-time"
    ),
    TopApp(
        imageRes = R.drawable.top_app_common_sense_media,
        titleRes = R.string.top_app_common_sense_media_title,
        url = "https://www.commonsensemedia.org/lists/best-apps-for-kids-age-5-8"
    ),
    TopApp(
        imageRes = R.drawable.top_app_the_gamer,
        titleRes = R.string.top_app_the_gamer_title,
        url = "https://www.thegamer.com/mobile-phone-tablet-video-games-kids-children-best-appropriate-esrb/"
    )
)