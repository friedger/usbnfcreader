package de.friedger.android.usbnfcreader.vote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class VoteDbHelper extends SQLiteOpenHelper {

	// If you change the database schema, you must increment the database
	// version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "vote.db";
	public static final String TABLE_NAME = "vote";
	
	private static final String SQL_CREATE_ENTRIES = 			
		    "CREATE TABLE " + TABLE_NAME + " (" +
		    BaseColumns._ID + " INTEGER PRIMARY KEY," +
		    Vote.COLUMN_NAME_ROOM_ID + " TEXT, " +
		    Vote.COLUMN_NAME_VOTE_TYPE + " TEXT, " +
		    Vote.COLUMN_NAME_ID + " TEXT, " +
		    Vote.COLUMN_NAME_TIMESTAMP + " LONG ) ";
		    
	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

	public VoteDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade
		// policy is
		// to simply to discard the data and start over
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

}
