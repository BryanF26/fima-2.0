package com.example.fima_2.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "leave_requests.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "leave_requests"
        const val COLUMN_ID = "id"
        const val COLUMN_START_DATE = "start_date"
        const val COLUMN_END_DATE = "end_date"
        const val COLUMN_REASON = "reason"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_START_DATE TEXT,
                $COLUMN_END_DATE TEXT,
                $COLUMN_REASON TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertLeaveRequest(startDate: String, endDate: String, reason: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_START_DATE, startDate)
            put(COLUMN_END_DATE, endDate)
            put(COLUMN_REASON, reason)
        }
        return db.insert(TABLE_NAME, null, values)
    }
}
