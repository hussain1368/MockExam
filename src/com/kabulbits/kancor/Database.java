package com.kabulbits.kancor;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class Database extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "kankor.sqlite";
    private static final int DATABASE_VERSION = 1;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
	public Cursor getQuestions(String cat, String num) {

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select * from question WHERE category = ? ORDER BY random() limit ?", 
				new String [] {cat, num});
		cursor.moveToFirst();
		db.close();
		return cursor;
	}
	
	public Cursor getQuestions(String num) {

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * from question ORDER BY random() limit ?",
				new String [] {num});
		cursor.moveToFirst();
		db.close();
		return cursor;
	}
	
	public void incrementWrong(int id){
		
		SQLiteDatabase db = getReadableDatabase();
		db.execSQL("UPDATE question set wrongs = wrongs + 1 WHERE _id = ?", new Integer [] {id});
		db.close();
	}
	
	public Cursor wrongAnswers(String cat, String num){
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from question WHERE wrongs > 0 AND category = ? ORDER BY wrongs DESC limit ?", 
				new String [] {cat, num});
		cursor.moveToFirst();
		db.close();
		return cursor;
	}
	
	public Cursor wrongAnswers(String num) {

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * from question WHERE wrongs > 0 ORDER BY wrongs DESC limit ?",
				new String [] {num});
		cursor.moveToFirst();
		db.close();
		return cursor;
	}

	public void clearWrong(int id) {

		SQLiteDatabase db = getReadableDatabase();
		db.execSQL("UPDATE question set wrongs = 0 WHERE _id = ?", new Integer [] {id});
		db.close();
	}

	public void clearAllWrongs() {
		
		SQLiteDatabase db = getReadableDatabase();
		db.execSQL("UPDATE question set wrongs = 0");
		db.close();
	}
	
	public boolean countWrongs(){
		
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("SELECT COUNT(*) AS num FROM question WHERE wrongs > 0", null);
		c.moveToFirst();
		db.close();
		if(c.getInt(c.getColumnIndex("num")) > 0)
			return true;
		return false;
	}
	
public boolean countWrongs(String cat){
		
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("SELECT COUNT(*) AS num FROM question WHERE category = ? AND wrongs > 0", new String[]{cat});
		c.moveToFirst();
		db.close();
		if(c.getInt(c.getColumnIndex("num")) > 0)
			return true;
		return false;
	}
}















