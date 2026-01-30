package com.lbe.imsdk

import android.content.Context
import com.lbe.imsdk.pages.LbeMainActivity
import com.lbe.imsdk.repository.model.SDKInitConfig

/**
 *
 * @Author mocaris
 * @Date 2026-01-30
 * @Since
 */
object LbeSDK {
    fun init(
        context: Context,
        lbeSign: String,
        lbeIdentity: String,
        nickId: String,
        nickName: String,
        phone: String,
        email: String,
        headerIcon: String,
        language: String,
        device: String,
        source: String,
        extraInfo: String,
        groupId: String,
        domain: String,
    ){
        LbeMainActivity.start(
            context, SDKInitConfig(
                lbeSign = lbeSign,
                lbeIdentity = lbeIdentity,
                phone = phone,
                email = email,
                language = language,
                device = device,
                headerIcon = headerIcon,
                groupID = groupId,
                domain = domain,
                source = source,
                nickId = nickId,
                nickName = nickName,
                extraInfo = extraInfo,
            )
        )
    }
}