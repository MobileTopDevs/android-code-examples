package com.timelimiter.android.features.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.timelimiter.android.common_ui.components.AppFilledButton
import com.timelimiter.android.common_ui.theme.AppColor
import com.timelimiter.android.common_ui.theme.AppTheme
import com.timelimiter.android.features.R

@Composable
fun OnBoardingScreen(
    onBoardingViewModel: OnBoardingViewModel = hiltViewModel()
) {
    OnBoardingContent(
        finishedOnBoarding = onBoardingViewModel::finishedOnBoarding
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun OnBoardingContent(finishedOnBoarding: () -> Unit) {
    val pages = OnBoardingPage.values()
    val pagerState = rememberPagerState { pages.size }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                modifier = Modifier.weight(1f),
                state = pagerState
            ) { position ->
                OnBoardingPagerScreen(
                    onBoardingPage = pages[position],
                    finishedOnBoarding = finishedOnBoarding
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalPagerIndicator(
                pagerState = pagerState,
                pageCount = pages.size,
                activeColor = Color.White,
                inactiveColor = Color.White.copy(alpha = .35f),
                indicatorWidth = 8.dp,
                spacing = 8.dp
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
fun OnBoardingPagerScreen(
    onBoardingPage: OnBoardingPage,
    modifier: Modifier = Modifier,
    finishedOnBoarding: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Image(
            modifier = Modifier
                .fillMaxHeight(.5f)
                .clip(RoundedCornerShape(16.dp)),
            painter = painterResource(id = onBoardingPage.imageId),
            contentDescription = null,
            contentScale = ContentScale.FillHeight
        )
        Spacer(modifier = Modifier.height(32.dp))
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier,
                text = stringResource(id = onBoardingPage.titleTextId),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                minLines = 1
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                modifier = Modifier,
                text = stringResource(id = onBoardingPage.subtitleTextId),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                minLines = 1,
                textAlign = TextAlign.Center
            )

        }
        Spacer(modifier = Modifier.weight(1f))
        if (onBoardingPage == OnBoardingPage.STEP_3) {
            AppFilledButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.go),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = AppColor.Blue,
                    disabledContentColor = AppColor.Blue.copy(alpha = .35f),
                    disabledContainerColor = Color.White.copy(alpha = .35f),

                    ),
                onClick = finishedOnBoarding
            )
        }
    }
}

@Preview
@Composable
fun OnBoardingContentPreview() {
    AppTheme {
        OnBoardingContent(finishedOnBoarding = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF3C7DFC)
@Composable
fun FirstOnBoardingScreenPreview() {
    AppTheme {
        OnBoardingPagerScreen(
            onBoardingPage = OnBoardingPage.STEP_1,
            finishedOnBoarding = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF3C7DFC)
@Composable
fun SecondOnBoardingScreenPreview() {
    AppTheme {
        OnBoardingPagerScreen(
            onBoardingPage = OnBoardingPage.STEP_2,
            finishedOnBoarding = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF3C7DFC)
@Composable
fun ThirdOnBoardingScreenPreview() {
    AppTheme {
        OnBoardingPagerScreen(
            onBoardingPage = OnBoardingPage.STEP_3,
            finishedOnBoarding = {}
        )
    }
}