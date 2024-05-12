package com.example.myremainder

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



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
        const val COLUMN_MERIDIAN = "meridian"
        const val COLUMN_REPEAT = "repeat"
        const val COLUMN_ACTIVE = "active"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TITLE TEXT, $COLUMN_CONTENT TEXT, $COLUMN_TIME TEXT, $COLUMN_DATE TEXT, $COLUMN_MERIDIAN TEXT, $COLUMN_REPEAT TEXT, $COLUMN_ACTIVE TEXT)"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    suspend fun insertRemainder(title: String, content: String, time: String, date: String, meridian: String, repeat: String, active: String): Long = withContext(Dispatchers.IO) {
        val db = this@RemainderDbHelper.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COLUMN_TITLE, title)
        contentValues.put(COLUMN_CONTENT, content)
        contentValues.put(COLUMN_TIME, time)
        contentValues.put(COLUMN_DATE, date)
        contentValues.put(COLUMN_MERIDIAN, meridian)
        contentValues.put(COLUMN_REPEAT, repeat)
        contentValues.put(COLUMN_ACTIVE, active)

        db.insert(TABLE_NAME, null, contentValues)
    }

    suspend fun getAllRemainders(): List<Remainder> = withContext(Dispatchers.IO) {
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
            val meridian = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MERIDIAN))
            val repeat = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPEAT))
            val active = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIVE))

            val remainder = Remainder(id, title, content, time, date, meridian, repeat, active)
            remainderList.add(remainder)
        }

        cursor.close()
        db.close()
        remainderList
    }

    suspend fun updateRemainder(remainder: Remainder) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, remainder.title)
            put(COLUMN_CONTENT, remainder.content)
            put(COLUMN_TIME, remainder.time)
            put(COLUMN_DATE, remainder.date)
            put(COLUMN_MERIDIAN, remainder.meridian)
            put(COLUMN_REPEAT, remainder.repeat)
            put(COLUMN_ACTIVE, remainder.active)
        }
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(remainder.id.toString())
        db.update(TABLE_NAME, values, whereClause, whereArgs)
        db.close()
    }

    suspend fun getRemainderById(id: Int): Remainder? = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = $id"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
            val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            val meridian = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MERIDIAN))
            val repeat = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPEAT))
            val active = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIVE))
            cursor.close()
            db.close()
            Remainder(id, title, content, time, date, meridian, repeat, active)
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    suspend fun deleteRemainder(id: Int) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())
        db.delete(TABLE_NAME, whereClause, whereArgs)
        db.close()
    }

    suspend fun addRemainder(remainder: Remainder): Long = withContext(Dispatchers.IO) {
        val db = this@RemainderDbHelper.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COLUMN_TITLE, remainder.title)
        contentValues.put(COLUMN_CONTENT, remainder.content)
        contentValues.put(COLUMN_TIME, remainder.time)
        contentValues.put(COLUMN_DATE, remainder.date)
        contentValues.put(COLUMN_MERIDIAN, remainder.meridian)
        contentValues.put(COLUMN_REPEAT, remainder.repeat)
        contentValues.put(COLUMN_ACTIVE, remainder.active)

        db.insert(TABLE_NAME, null, contentValues)
    }
}