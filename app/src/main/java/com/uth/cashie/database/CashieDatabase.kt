package com.uth.cashie.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uth.cashie.database.dao.*
import com.uth.cashie.database.entity.*

/**
 * Room Database cho toàn bộ ứng dụng Cashie.
 *
 * 5 bảng: users, app_settings, categories, transactions, saved_accounts
 * Phải bật FOREIGN KEY support qua PRAGMA trong callback onCreate.
 */
@Database(
    entities = [
        UserEntity::class,
        AppSettingsEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        SavedAccountEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CashieDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savedAccountDao(): SavedAccountDao

    companion object {
        private const val DB_NAME = "cashie.db"

        @Volatile
        private var INSTANCE: CashieDatabase? = null

        fun getInstance(context: Context): CashieDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): CashieDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                CashieDatabase::class.java,
                DB_NAME
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Bật Foreign Key support – bắt buộc với Room + SQLite
                        db.execSQL("PRAGMA foreign_keys = ON")
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Bật lại mỗi khi mở DB (SQLite reset về OFF sau mỗi connection)
                        db.execSQL("PRAGMA foreign_keys = ON")
                    }
                })
                .build()
    }
}
