package com.example.fragment.project.ui.system

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fragment.library.base.compose.FullScreenLoading
import com.example.fragment.library.base.compose.SwipeRefresh
import com.example.fragment.project.R
import com.example.fragment.project.components.ArticleCard
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SystemScreen(
    cid: String,
    systemTreeViewModel: SystemTreeViewModel,
    systemViewModel: SystemViewModel,
) {
    val systemTreeUiState by systemTreeViewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(systemTreeViewModel.getTabIndex(cid))
    DisposableEffect(LocalLifecycleOwner.current) {
        systemTreeViewModel.init(cid)
        onDispose {
            systemTreeViewModel.updateTabIndex(pagerState.currentPage, cid)
        }
    }
    Column(
        modifier = Modifier
            .background(colorResource(R.color.background))
            .systemBarsPadding()
    ) {
        TitleBar(systemTreeUiState.title)
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            backgroundColor = colorResource(R.color.white),
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                    color = colorResource(R.color.theme)
                )
            },
            divider = {
                TabRowDefaults.Divider(color = colorResource(R.color.transparent))
            }
        ) {
            systemTreeUiState.result.forEachIndexed { index, item ->
                Tab(
                    text = { Text(item.name) },
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    selected = pagerState.currentPage == index,
                    selectedContentColor = colorResource(R.color.theme),
                    unselectedContentColor = colorResource(R.color.text_999)
                )
            }
        }
        TabRowDefaults.Divider(color = colorResource(R.color.line))
        HorizontalPager(
            count = systemTreeUiState.result.size,
            state = pagerState,
        ) { page ->
            val pageCid = systemTreeUiState.result[page].id
            DisposableEffect(LocalLifecycleOwner.current) {
                systemViewModel.init(pageCid)
                onDispose {}
            }
            val systemUiState by systemViewModel.uiState.collectAsStateWithLifecycle()
            val listState = rememberLazyListState()
            if (systemTreeUiState.isLoading
                || (systemUiState.getRefreshing(pageCid) && !systemUiState.getLoading(pageCid))
            ) {
                FullScreenLoading()
            } else {
                SwipeRefresh(
                    modifier = Modifier.fillMaxSize(),
                    listState = listState,
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    refreshing = systemUiState.getRefreshing(pageCid),
                    loading = systemUiState.getLoading(pageCid),
                    onRefresh = { systemViewModel.getHome(pageCid) },
                    onLoad = { systemViewModel.getNext(pageCid) },
                    onRetry = { systemViewModel.getHome(pageCid) },
                    data = systemUiState.getResult(pageCid),
                ) { _, item ->
                    ArticleCard(item = item)
                }
            }
        }
    }
}

@Composable
fun TitleBar(title: String) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(colorResource(R.color.theme))
    ) {
        IconButton(
            modifier = Modifier.height(45.dp),
            onClick = {
                if (context is AppCompatActivity) {
                    context.onBackPressedDispatcher.onBackPressed()
                }
            }
        ) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = null,
                tint = colorResource(R.color.white)
            )
        }
        Text(
            text = title,
            fontSize = 16.sp,
            color = colorResource(R.color.text_fff),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}