package com.lbe.imsdk.extension

import com.lbe.imsdk.repository.db.entry.IMMessageEntry

/**
 *
 * @Date 2025-09-04
 */


typealias OnResendMessage = (msg: IMMessageEntry) -> Unit
