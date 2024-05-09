package com.example.myremainder

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RemainderDbHelper (context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object{
        const val DATABASE_NAME = "remainder.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "all_remainder"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_TIME = "time"
        const val COLUMN_DATE = "date"
        const val COLUMN_REPEAT = "repeat"
        const val COLUMN_ACTIVE = "active"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TITLE TEXT, $COLUMN_CONTENT TEXT, $COLUMN_TIME TEXT, $COLUMN_DATE TEXT, $COLUMN_REPEAT TEXT, $COLUMN_ACTIVE TEXT)"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun insertRemainder(title: String, content: String, time: String, date: String, repeat: String, active: String){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_CONTENT, content)
            put(COLUMN_TIME, time)
            put(COLUMN_DATE, date)
            put(COLUMN_REPEAT, repeat)
            put(COLUMN_ACTIVE, active)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllRemainders(): List<Remainder>{
        val remainderList = mutableListOf<Remainder>()
        val db = readableDatabase
        val getAllQuery = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(getAllQuery, null)

        while (cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
            val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            val repeat = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPEAT))
            val active = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIVE))

            val remainder = Remainder(id, title, content, time, date, repeat, active)
            remainderList.add(remainder)
        }

        cursor.close()
        db.close()
        return remainderList
    }
}