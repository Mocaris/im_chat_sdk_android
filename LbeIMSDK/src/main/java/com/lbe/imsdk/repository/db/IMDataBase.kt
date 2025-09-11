package com.lbe.imsdk.repository.db

import androidx.room.*
import androidx.sqlite.db.*
import com.lbe.imsdk.extension.*
import com.lbe.imsdk.repository.db.dao.*
import com.lbe.imsdk.repository.db.entry.*
import kotlin.concurrent.Volatile

@Database(
    entities = [
        IMMessageEntry::class,
    ],
    autoMigrations = [],

    version = IMDataBase.dbVersion,
    exportSchema = true,
)
abstract class IMDataBase : RoomDatabase() {

    companion object {
        const val dbVersion = 1

        @Volatile
        private var INSTANCE: IMDataBase? = null

        fun get(): IMDataBase {
            synchronized(this) {
                if (null == INSTANCE) {
                    INSTANCE =
                        Room.databaseBuilder(appContext, IMDataBase::class.java, "im_database.db")
                            .addCallback(object : Callback() {
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    println("database-----onCreate")
                                }

                                override fun onOpen(db: SupportSQLiteDatabase) {
                                    println("database-----onOpen")
                                }
                            })

                            .build()
                }
                return INSTANCE as IMDataBase
            }
        }
    }

    abstract fun imMsgDao(): IMMsgDao

}
