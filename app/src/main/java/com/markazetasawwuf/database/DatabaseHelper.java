package com.markazetasawwuf.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.markazetasawwuf.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {

	// Table Name
	public static final String TABLE_NAME_VIDEOS = Constants.TABLE_NAME_VIDEOS;
	public static final String TABLE_NAME_TEACHING = Constants.TABLE_NAME_TEACHING;
	public static final String TABLE_NAME_AUDIOS = Constants.TABLE_NAME_AUDIOS;
    public static final String TABLE_NAME_PHOTOS = Constants.TABLE_NAME_PHOTOS;
	public static final String TABLE_NAME_BOOKS = Constants.TABLE_NAME_BOOKS;

	// Table columns
	public static final String _ID = "_id";
	public static final String MT_TEACHER = "teacher";
	public static final String MT_TEACHINGS = "teachings";

	public static final String VIDEO_ID = "videoId";
	public static final String THUMBNAIL_ID = "thumbnailId";
	public static final String NAME = "name";
	public static final String TIME = "time";
	public static final String AUDIO_ID = "audioId";
    public static final String PHOTO_ID = "photoId";
	public static final String BOOK_ID = "bookId";
	public static final String AUTHOR = "author";

	// Database Information
	static final String DB_NAME = "SUFISM";

	// database version
	static final int DB_VERSION = 2;

	// Creating table query
//	private static final String CREATE_TABLE = "create table " + TABLE_NAME_QUOTES + "(" + _ID
//			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + MT_QUOTE + " TEXT NOT NULL, " + MT_TEACHER + " TEXT);";
	
	private static final String CREATE_TABLE_TEACHINGS = "create table " + TABLE_NAME_TEACHING + "(" + _ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + MT_TEACHINGS + " TEXT NOT NULL, " + MT_TEACHER + " TEXT);";

	private static final String CREATE_TABLE_VIDEOS = "create table " + TABLE_NAME_VIDEOS + "(" + _ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + VIDEO_ID + " TEXT NOT NULL, " + THUMBNAIL_ID + " TEXT, " + NAME + " TEXT, " + TIME + " TEXT);";

	private static final String CREATE_TABLE_AUDIOS = "create table " + TABLE_NAME_AUDIOS + "(" + _ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " TEXT NOT NULL, " + AUDIO_ID + " TEXT);";

    private static final String CREATE_TABLE_PHOTOS = "create table " + TABLE_NAME_PHOTOS+ "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT," + PHOTO_ID + " TEXT);";
	private static final String CREATE_TABLE_BOOKS = "create table " + TABLE_NAME_BOOKS + "(" + _ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + BOOK_ID + " TEXT NOT NULL, " + THUMBNAIL_ID + " TEXT, " + NAME + " TEXT, " + AUTHOR + " TEXT);";


	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_TEACHINGS);
		db.execSQL(CREATE_TABLE_VIDEOS);
		db.execSQL(CREATE_TABLE_AUDIOS);
        db.execSQL(CREATE_TABLE_PHOTOS);
		db.execSQL(CREATE_TABLE_BOOKS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME_TEACHING);
		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME_VIDEOS);
		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME_AUDIOS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME_PHOTOS);
		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME_BOOKS);
		onCreate(db);
	}
}