package com.lbe.imsdk.provider

import androidx.compose.runtime.compositionLocalOf
import com.lbe.imsdk.components.DialogManager
import com.lbe.imsdk.pages.conversation.vm.ConversationSateHolderVM
import com.lbe.imsdk.pages.conversation.vm.CurrentConversationVM
import com.lbe.imsdk.pages.navigation.NavigationBackStack
import com.lbe.imsdk.pages.vm.LbeMainViewModel
import com.lbe.imsdk.repository.db.entry.IMMessageEntry
import com.lbe.imsdk.repository.model.SDKInitConfig
import com.lbe.imsdk.repository.remote.model.CreateSessionResModel
import com.lbe.imsdk.theme.ThemeColors

/**
 *
 *
 * @Date 2025-07-16
 */

val LocalSDKInitConfig = compositionLocalOf<SDKInitConfig> {
    error("CompositionLocal SDKConfig not present")
}

internal val LocalDialogManager = compositionLocalOf<DialogManager> {
    error("CompositionLocal DialogManager not present")
}
internal val LocalMainViewModel = compositionLocalOf<LbeMainViewModel> {
    error("CompositionLocal ActivityViewModel not present")
}

internal val LocalNavBackStack = compositionLocalOf<NavigationBackStack> {
    error("CompositionLocal NavBackStack not present")
}

/**
 * 当前会话ID
 */
internal val LocalSession = compositionLocalOf<CreateSessionResModel.SessionData> {
    error("CompositionLocal SessionData not present")
}

internal val LocalThemeColors = compositionLocalOf<ThemeColors> {
    error("CompositionLocal SessionData not present")
}

internal val LocalConversationStateViewModel = compositionLocalOf<ConversationSateHolderVM> {
    error("CompositionLocal ConversationSateHolderVM not present")
}

internal val LocalCurrentConversationViewModel = compositionLocalOf<CurrentConversationVM> {
    error("CompositionLocal CurrentConversationVM not present")
}

internal val LocalIMMessageEntry = compositionLocalOf<IMMessageEntry> {
    error("CompositionLocal IMMessageEntry not present")
}