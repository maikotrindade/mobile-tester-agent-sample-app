@file:OptIn(ExperimentalMaterial3Api::class)

package io.githib.maikotrindade.appfortesting.ui.screen.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.githib.maikotrindade.appfortesting.R
import io.githib.maikotrindade.appfortesting.component.StoryItem
import io.githib.maikotrindade.appfortesting.model.Story
import io.githib.maikotrindade.appfortesting.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(feedViewModel: FeedViewModel = viewModel(), navController: androidx.navigation.NavHostController? = null) {
    val loading = feedViewModel.loading.collectAsState().value
    val posts = feedViewModel.posts.collectAsState().value
    val stories = feedViewModel.stories.collectAsState().value
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = loading)
    val listState = rememberLazyListState()

    // Scroll to top when posts change (e.g., after new post is added)
    LaunchedEffect(posts) {
        if (posts.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    SwipeRefresh(state = swipeRefreshState, onRefresh = { feedViewModel.refresh() }) {
        if (loading) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 120.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
            }
        } else {
            Column {
                StoriesSection(stories = stories)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(state = listState) {
                    items(posts, key = { it.hashCode() }) { post ->
                        PostTile(
                            user = post.user,
                            gifUrl = post.mediaUrl.orEmpty(),
                            description = post.description.orEmpty(),
                            isLiked = post.isLiked,
                            onLikeClick = { feedViewModel.toggleLike(post) },
                            onUserClick = {
                                navController?.navigate("profile/${post.user.username}")
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun StoriesSection(stories: List<Story>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(stories) { story ->
            StoryItem(story = story)
        }
    }
}

@Composable
fun PostTile(
    user: User,
    gifUrl: String,
    description: String,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onUserClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(ImageDecoderDecoder.Factory())
        }
        .build()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = user.profilePictureUrl,
                contentDescription = stringResource(id = R.string.user_thumbnail),
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onUserClick() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = user.username,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onUserClick() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(gifUrl)
                .crossfade(true)
                .build(),
            imageLoader = imageLoader,
            contentDescription = stringResource(id = R.string.gif_post),
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(
                text = user.username,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.clickable { onUserClick() }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = description,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLikeClick) {
                if (isLiked) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = stringResource(id = R.string.like),
                        tint = Color.Red
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(id = R.string.like),
                        tint = Color.Gray
                    )
                }
            }
            IconButton(onClick = { /* handle click */ }) {
                Icon(Icons.Outlined.Email, contentDescription = stringResource(id = R.string.comment))
            }
            IconButton(onClick = { /* handle click */ }) {
                Icon(Icons.Outlined.Share, contentDescription = stringResource(id = R.string.share))
            }
        }
    }
}
