package com.lbe.imsdk.pages.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.*
import com.lbe.imsdk.components.DialogHost
import com.lbe.imsdk.pages.TestPage
import com.lbe.imsdk.pages.conversation.*
import com.lbe.imsdk.pages.init.*
import com.lbe.imsdk.pages.unknown.UnknownPage
import java.util.Map.entry

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