package com.lbe.imsdk.pages.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.*
import com.lbe.imsdk.repository.remote.model.CreateSessionResModel

/**
 * lbe chat route enum
 *
 * @Date 2025-08-15
 */
public typealias NavigationBackStack = SnapshotStateList<PageRoute>


private val routesInternal: NavigationBackStack = mutableStateListOf(PageRoute.Init)

interface PageRoute {
    companion object {
        val routes get() = routesInternal
    }


    data object Unknown : PageRoute
    data object Test : PageRoute

    data object Init : PageRoute

    /**
     * conversation page route
     */
//    @Serializable
    data object Conversation : PageRoute

    /**
     * init page route
     */
//    @Serializable


}

