package com.lbe.imsdk.provider

import android.annotation.*
import android.content.*
import android.database.*
import android.net.*

/**
 *
 *
 * @Date 2025-07-16
 */
internal class ContextProvider : ContentProvider()
//    , KoinStartup
{
    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context
        val appContext: Context get() = context
    }

//    override fun onKoinStartup(): KoinConfiguration = koinConfiguration {
//        androidLogger(level = Level.DEBUG)
//        androidContext(this@ContextProvider.context!!.applicationContext)
//        modules(koinFactoryModule, koinSingleModule, koinQualifierModule)
//    }

    override fun onCreate(): Boolean {
        ContextProvider.context = this.context!!.applicationContext
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }

}