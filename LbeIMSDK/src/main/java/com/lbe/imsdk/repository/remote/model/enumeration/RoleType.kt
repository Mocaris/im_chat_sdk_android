package com.lbe.imsdk.repository.remote.model.enumeration

import androidx.annotation.*


/**
 * 角色类型
 *
 * @Date 2025-08-18
 */

@Retention(AnnotationRetention.SOURCE)
@IntDef(value = [RoleType.NORMAL_USER, RoleType.CUSTOMER_SERVICE])
annotation class RoleType {
    companion object {

        /**
         * 0-normal user
         */
        const val NORMAL_USER = 0

        /**
         * 1 - customer service
         */
        const val CUSTOMER_SERVICE = 1
    }
}


/**
 * 1-B端 2-C端
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(value = [UserType.WEB, UserType.APP])
annotation class UserType {
    companion object {

        /**
         * 1-B端
         */
        const val WEB = 0

        /**
         *  2-C端
         */
        const val APP = 1
    }

}