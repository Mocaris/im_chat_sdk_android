package com.lbe.imsdk.repository.remote.model.enumeration

import androidx.annotation.IntDef
import kotlin.intArrayOf

/**
 *
 *
 * @Date 2025-08-18
 */

@Retention(AnnotationRetention.SOURCE)
@IntDef(value = [ReqSource.H5, ReqSource.APP])
annotation class ReqSource {
    companion object {

        //0-h5 1-app
        const val H5 = 0

        //1-app
        const val APP = 1
    }
}