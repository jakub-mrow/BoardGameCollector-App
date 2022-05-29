package com.example.boardgamecollector

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import android.content.ContentValues
import java.text.DateFormat
import java.time.LocalDateTime
import java.util.*

class DBHandler(context: Context,
                factory: SQLiteDatabase.CursorFactory?,
                ) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "boardGamesDB"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_BOARDGAMES_TABLE = (
                "CREATE TABLE boardgames (" +
                        "game_id INTEGER PRIMARY KEY," +
                        "title TEXT," +
                        "release_date INTEGER," +
                        "bgg_id INTEGER," +
                        "rank INTEGER," +
                        "image TEXT" +
                        ")"
                )

        val CREATE_DLC_TABLE = (
                "CREATE TABLE dlc (" +
                        "dlc_id INTEGER PRIMARY KEY," +
                        "title TEXT," +
                        "release_date INTEGER," +
                        "bgg_id INTEGER," +
                        "image TEXT" +
                        ")"
                )

        val CREATE_USER_TABLE = (
                "CREATE TABLE user (" +
                        "name TEXT," +
                        "last_sync TEXT" +
                        ")"
                )

        val CREATE_RANKS_TABLE = (
                "CREATE TABLE ranks(" +
                        "rank_id INTEGER PRIMARY KEY," +
                        "game_id INTEGER," +
                        "release_date TEXT," +
                        "rank INTEGER," +
                        "FOREIGN KEY(game_id) REFERENCES boardgames(game_id) ON DELETE CASCADE" +
                        ")"
                )

        db.execSQL(CREATE_BOARDGAMES_TABLE)
        db.execSQL(CREATE_DLC_TABLE)
        db.execSQL(CREATE_USER_TABLE)
        db.execSQL(CREATE_RANKS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS boardgames")
        db.execSQL("DROP TABLE IF EXISTS dlc")
        db.execSQL("DROP TABLE IF EXISTS user")
        db.execSQL("DROP TABLE IF EXISTS ranks")
        onCreate(db)
    }

    fun addUser(name: String, lastSync: String) {
        val values = ContentValues()
        val db = this.writableDatabase
        values.put("name", name)
        values.put("last_sync", lastSync)

        db.insert("user", null, values)
    }

    fun userExists(): Boolean {
        val cursor = readableDatabase.rawQuery("SELECT name FROM user", null)
        val count = cursor.count
        cursor.close()
        return count == 1
    }

    fun deleteUsers() {
        writableDatabase.execSQL("DELETE FROM user")
    }



}