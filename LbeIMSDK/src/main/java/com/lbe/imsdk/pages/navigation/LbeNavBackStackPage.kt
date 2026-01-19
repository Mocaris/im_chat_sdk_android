package com.lbe.imsdk.pages.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.lbe.imsdk.components.DialogHost
import com.lbe.imsdk.pages.TestPage
import com.lbe.imsdk.pages.conversation.ConversationPage
import com.lbe.imsdk.pages.init.InitSdkPage
import com.lbe.imsdk.pages.unknown.UnknownPage

/**
 * 导航页面
 *
 * @Date 2025-08-15
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LbeNavBackStackPage() {
//    val backStack =
//        rememberNavBackStack(PageRoute.Init)
    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
//        CompositionLocalProvider(LocalNavBackStack provides backStack) {
        DialogHost {
            NavDisplay(
                backStack = PageRoute.routes,
                entryDecorators = listOf(
                    // Add the default decorators for managing scenes and saving state
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                    // Then add the view model store decorator
                    //                    rememberViewModelStoreNavEntryDecorator()
                ),
                transitionSpec = {
                    ContentTransform(
                        slideIn(
                            animationSpec = tween(500),
                            initialOffset = { IntOffset(it.width, 0) }),
                        slideOut(
                            animationSpec = tween(500),
                            targetOffset = { IntOffset(-it.width, 0) }),
                    )
                },
                popTransitionSpec = {
                    ContentTransform(
                        fadeIn(animationSpec = tween(500)),
                        fadeOut(animationSpec = tween(500)),
                    )
                },
            ) { entry ->
                when (entry) {
                    is PageRoute.Test -> NavEntry(key = entry) { TestPage() }
                    is PageRoute.Init -> NavEntry(key = entry) { InitSdkPage() }
                    is PageRoute.Conversation -> NavEntry(key = entry) {
                        ConversationPage(entry.sessionData)
                    }
                    else -> NavEntry(key = PageRoute.Unknown) { UnknownPage() }
                }
            }
        }
//        }
    }
}