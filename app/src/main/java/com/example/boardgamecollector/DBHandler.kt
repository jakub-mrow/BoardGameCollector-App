package com.example.boardgamecollector

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import android.content.ContentValues
import android.database.Cursor
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

    fun addBoardGame(boardGame: BoardGame){
        val values = ContentValues()
        val db = this.writableDatabase
        values.put("title", boardGame.title)
        values.put("release_date", boardGame.releaseDate)
        values.put("bgg_id", boardGame.bggId)
        values.put("rank", boardGame.rank)
        values.put("image", boardGame.image)

        db.insert("boardgames", null, values)
    }

    fun addDlc(dlc: DLC){
        val values = ContentValues()
        val db = this.writableDatabase
        values.put("dlc_id", dlc.dlcId)
        values.put("title", dlc.dlcId)
        values.put("release_date", dlc.releaseDate)
        values.put("bgg_id", dlc.bggId)
        values.put("image", dlc.image)

        db.insert("dlc", null, values)
    }

    fun userExists(): Boolean {
        val cursor = readableDatabase.rawQuery("SELECT name FROM user", null)
        val count = cursor.count
        cursor.close()
        return count == 1
    }

    fun countGames(): Int {
        val cursor = readableDatabase.rawQuery("SELECT game_id FROM boardgames", null)
        val gamesCount = cursor.count
        cursor.close()
        return gamesCount
    }

    fun countDLC(): Int {
        val cursor = readableDatabase.rawQuery("SELECT dlc_id FROM dlc", null)
        val dlcCount = cursor.count
        cursor.close()
        return dlcCount
    }

    fun getName(): String? {
        val cursor = readableDatabase.rawQuery("SELECT name FROM user", null)
        var name: String? = null
        if (cursor.moveToFirst()){
            val columnIndex = cursor.getColumnIndex("name")
            if (columnIndex != -1) {
                name = cursor.getString(columnIndex).toString()
            }
            cursor.close()
        }
        return name
    }

    fun findGamesCursor(): Cursor {
        val query = "SELECT game_id as _id, title, release_date, rank, image FROM boardgames ORDER BY game_id"
        return readableDatabase.rawQuery(query, null)
    }

    fun findDlcCursor(): Cursor {
        val query =
            "SELECT dlc_id as _id, title, release_date FROM dlc ORDER BY dlc_id"
        return readableDatabase.rawQuery(query, null)
    }

    fun deleteUsers() {
        writableDatabase.execSQL("DELETE FROM user")
    }

    fun deleteBoardGames() {
        writableDatabase.execSQL("DELETE FROM boardgames")
    }

    fun deleteDLC() {
        writableDatabase.execSQL("DELETE FROM dlc")
    }



}