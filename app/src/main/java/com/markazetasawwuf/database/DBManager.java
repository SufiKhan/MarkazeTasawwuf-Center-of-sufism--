package com.markazetasawwuf.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {

	private DatabaseHelper dbHelper;
	
	private Context context;
	
	private SQLiteDatabase database;

	public DBManager(Context c) {
		context = c;
	}

	public DBManager open() throws SQLException {
		dbHelper = new DatabaseHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}
   
	public void close() {
		dbHelper.close();
	}

	public void insertVideo(String videoID, String thumbnailID, String name, String time) {
		ContentValues contentValue = new ContentValues();
		contentValue.put(DatabaseHelper.VIDEO_ID, videoID);
		contentValue.put(DatabaseHelper.THUMBNAIL_ID, thumbnailID);
		contentValue.put(DatabaseHelper.NAME, name);
		contentValue.put(DatabaseHelper.TIME, time);
		database.insert(DatabaseHelper.TABLE_NAME_VIDEOS, null, contentValue);
	}
	
	public void insertTeachings(String teaching, String teacher) {
		ContentValues contentValue = new ContentValues();
		contentValue.put(DatabaseHelper.MT_TEACHINGS, teaching);
		contentValue.put(DatabaseHelper.MT_TEACHER, teacher);
		database.insert(DatabaseHelper.TABLE_NAME_TEACHING, null, contentValue);
	}
	public void insertAudios(String name, String id) {
		ContentValues contentValue = new ContentValues();
		contentValue.put(DatabaseHelper.NAME, name);
		contentValue.put(DatabaseHelper.AUDIO_ID, id);
		database.insert(DatabaseHelper.TABLE_NAME_AUDIOS, null, contentValue);
	}
	public void insertPhotos(String id) {
		ContentValues contentValue = new ContentValues();
		contentValue.put(DatabaseHelper.PHOTO_ID, id);
		database.insert(DatabaseHelper.TABLE_NAME_PHOTOS, null, contentValue);
	}
	public void insertBook(String bookId, String thumbnailID, String name, String author) {
		ContentValues contentValue = new ContentValues();
		contentValue.put(DatabaseHelper.BOOK_ID, bookId);
		contentValue.put(DatabaseHelper.THUMBNAIL_ID, thumbnailID);
		contentValue.put(DatabaseHelper.NAME, name);
		contentValue.put(DatabaseHelper.AUTHOR, author);
		database.insert(DatabaseHelper.TABLE_NAME_BOOKS, null, contentValue);
	}
	public Cursor fetchVideos() {
		String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.VIDEO_ID, DatabaseHelper.THUMBNAIL_ID, DatabaseHelper.NAME, DatabaseHelper.TIME };
		Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_VIDEOS, columns, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	public Cursor fetchTeachings() {

		String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.MT_TEACHINGS, DatabaseHelper.MT_TEACHER };
		Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_TEACHING, columns, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	public Cursor fetchAudios() {

		String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.NAME, DatabaseHelper.AUDIO_ID };
		Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_AUDIOS, columns, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	public Cursor fetchPhotos() {

		String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.PHOTO_ID };
		Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_PHOTOS, columns, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	public Cursor fetchBooks() {
		String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.BOOK_ID, DatabaseHelper.THUMBNAIL_ID, DatabaseHelper.NAME, DatabaseHelper.AUTHOR };
		Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_BOOKS, columns, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	public int update(long _id, String videoID, String thumbnailID, String name, String time) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.VIDEO_ID, videoID);
		contentValues.put(DatabaseHelper.THUMBNAIL_ID, thumbnailID);
		contentValues.put(DatabaseHelper.NAME, name);
		contentValues.put(DatabaseHelper.TIME, time);
		int i = database.update(DatabaseHelper.TABLE_NAME_VIDEOS, contentValues, DatabaseHelper._ID + " = " + _id, null);
		return i;
	}

	public void delete(long _id) {
		database.delete(DatabaseHelper.TABLE_NAME_VIDEOS, DatabaseHelper._ID + "=" + _id, null);
		database.delete(DatabaseHelper.TABLE_NAME_TEACHING, DatabaseHelper._ID + "=" + _id, null);
		database.delete(DatabaseHelper.TABLE_NAME_AUDIOS, DatabaseHelper._ID + "=" + _id, null);
		database.delete(DatabaseHelper.TABLE_NAME_PHOTOS, DatabaseHelper._ID + "=" + _id, null);
		database.delete(DatabaseHelper.TABLE_NAME_BOOKS, DatabaseHelper._ID + "=" + _id, null);
	}

}
