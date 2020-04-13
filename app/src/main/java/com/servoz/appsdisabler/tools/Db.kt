package com.servoz.appsdisabler.tools

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*
import kotlin.collections.ArrayList

class Db(context: Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, "appsDisabler.db", factory, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // println("DEBUG:Creating DBS")
        db.execSQL("CREATE TABLE `app`( `id` TEXT PRIMARY KEY, name TEXT, `launcher` TEXT, tag TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //when upgrading
    }

    fun dbUpd(){
        //Db(context, null).dbUpd()
        val db = this.writableDatabase
        db.execSQL("DROP TABLE `app`")
        db.execSQL("CREATE TABLE `app`( `id` TEXT PRIMARY KEY, name TEXT, `launcher` TEXT, tag TEXT)")
        db.close()
    }

    fun addData(table:String, data:HashMap<String, String>) {
        // println("DEBUG:DB ADD $table")
        val values = ContentValues()
        for(dat in data){
            values.put(dat.key, dat.value)
        }
        val db = this.writableDatabase
        db.insert(table, null, values)
        db.close()
    }

    fun getData(table: String, where:String="",fields:String="*",groupBy:String="",sortBy:String="", limit:Int=0):ArrayList<ArrayList<String>>{
        // println("DEBUG:DB get data $table $where")
        val query = "SELECT $fields FROM $table " +
                if(where !="") "WHERE $where" else "" +
                if(limit >0) "LIMIT $limit" else "" +
                if(groupBy !="") "GROUP BY $groupBy" else "" +
                if(sortBy !="") "ORDER BY $sortBy" else ""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        val rows = arrayListOf<ArrayList<String>>()
        while (cursor.moveToNext()){
            val row= arrayListOf<String>()
            for(c in 0 until cursor.columnCount){
                row.add(cursor.getString(c))
            }
            rows.add(row)
        }
        cursor.close()
        db.close()
        return rows
    }

    fun deleteById(table:String,id: String): Boolean {
        val query = "SELECT * FROM $table WHERE id = '$id'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            db.delete(table, "id = ?", arrayOf(id))
            cursor.close()
        }
        if(cursor.moveToFirst())
            return true
        db.close()
        return false
    }

    /*fun deleteWhere(table:String,where: String=""):Boolean {
        // println("DEBUG:DB Delete $table $where")
        val query = "SELECT * FROM $table " +
                if(where !="") "WHERE $where" else ""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            db.delete(table, where, arrayOf())
            cursor.close()
            return true
        }
        db.close()
        return false
    }*/

    fun editData(table: String, where:String="",data:HashMap<String, String>):Boolean{
        // println("DEBUG:DB Edit $table $where")
        val query = "SELECT * FROM $table " +
                if(where !="") "WHERE $where" else ""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        val values = ContentValues()
        for(dat in data){
            values.put(dat.key, dat.value)
        }
        var result = false
        if (cursor.moveToFirst()) {
            db.update(table, values,where, arrayOf())
            cursor.close()
            result = true
        }
        return result
    }

    fun replaceFieldData(table: String,field:String,oldValue:String,replace:String, where:String=""){
        // println("DEBUG:DB Replace $table $where")
        val query = "UPDATE $table " +
                "SET $field=REPLACE($field,'$oldValue','$replace')" +
                if(where !="") "WHERE $where" else ""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.close()
    }
}