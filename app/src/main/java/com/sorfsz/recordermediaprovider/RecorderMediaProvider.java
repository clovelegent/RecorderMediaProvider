package com.sorfsz.recordermediaprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.sotfsz.recordermediaprovider.R;

public class RecorderMediaProvider extends ContentProvider {
	
	public static final String TAG = "RecorderMediaProvider";
	public static final String AUTHORITY ="com.soft.recordermediaprovider.contentprovider";
	
	public static final Uri VIDEO_CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/video");
	public static final Uri IMAGE_CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/image");
	public static final Uri AUDIO_CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/audio");
	public static final Uri MEDIA_SETTINGS_CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/media_settings");
	
	public static final int VIDEO_CODE = 1;
	public static final int IMAGE_CODE = 2;
	public static final int AUDIO_CODE = 3;
	public static final int MEDIA_SETTINGS_CODE = 4;
	
	public static final String UNKNOW_URI = "Unknown URI ";
	
	public static final String VIDEO_CONTENT_TYPE = "vnd.android.cursor.dir/com.softsz.recordermediaprovider.video";
	public static final String IMAGE_CONTENT_TYPE = "vnd.android.cursor.dir/com.softsz.recordermediaprovider.image";
	public static final String AUDIO_CONTENT_TYPE = "vnd.android.cursor.dir/com.softsz.recordermediaprovider.audio";
	public static final String MEDIA_SETTINGS_CONTENT_TYPE = "vnd.android.cursor.dir/com.softsz.recordermediaprovider.media_settings";
	
	public static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static {
		uriMatcher.addURI(AUTHORITY, "video", VIDEO_CODE);
		uriMatcher.addURI(AUTHORITY, "image", IMAGE_CODE);
		uriMatcher.addURI(AUTHORITY, "audio", AUDIO_CODE);
		uriMatcher.addURI(AUTHORITY, "media_settings", MEDIA_SETTINGS_CODE);
	}
	
	public DatabaseHelper dbHelper = null;
	
	static final class DatabaseHelper extends SQLiteOpenHelper{

		private static final String DATABASE_NAME = "recorder.db";
		private static final int DATABSE_VERSION = 1;
		private static final String TABLE_VIDEO = "video";
		private static final String TABLE_IMAGE = "image";
		private static final String TABLE_AUDIO = "audio";
		private static final String TABLE_MEDIA_SETTINGS = "media_settings";

		private Context mContext;
		
		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
			mContext = context;
		}
		
		public DatabaseHelper(Context context, String name, int version) {
	        this(context, name, null, version);
	        mContext = context;
	    }
		
		private void createVideoTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS video (" +
                    "_id INTEGER PRIMARY KEY," +
                    "_data TEXT NOT NULL," +
                    "_display_name TEXT," +
                    "_size INTEGER," +
                    "mime_type TEXT," +
                    "date_added INTEGER," +
                    "title TEXT," +
                    "duration INTEGER," +
                    "resolution TEXT," +
                    "latitude DOUBLE," +
                    "longitude DOUBLE," +
                    "important INTEGER"+
                    ");");
		}
		
		private void createImageTable(SQLiteDatabase db){
			db.execSQL("CREATE TABLE IF NOT EXISTS image (" +
                    "_id INTEGER PRIMARY KEY," +
                    "_data TEXT," +
                    "_size INTEGER," +
                    "_display_name TEXT," +
                    "mime_type TEXT," +
                    "title TEXT," +
                    "date_added INTEGER," +
                    "latitude DOUBLE," +
                    "longitude DOUBLE," +
                    "orientation INTEGER," +
                    "important INTEGER"+
                   ");");
		}
		
		private void createAudioTable(SQLiteDatabase db){
			db.execSQL("CREATE TABLE IF NOT EXISTS audio (" +
                    "_id INTEGER PRIMARY KEY," +
                    "_data TEXT UNIQUE NOT NULL," +
                    "date_added INTEGER," +
                    "_display_name TEXT," +
                    "mime_type TEXT," +
                    "title TEXT NOT NULL," +
                    "duration INTEGER," +
                    "important INTEGER"+
                    ");");
		}
		
		private void createMediaSettingsTable(SQLiteDatabase db){
				db.execSQL("CREATE TABLE media_settings (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "name TEXT UNIQUE ON CONFLICT REPLACE," + "value TEXT" + ");");
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			createVideoTable(db);
			createImageTable(db);
			createAudioTable(db);
			createMediaSettingsTable(db);
			
			//初始化多媒体设置
			loadMediaSettings(db);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
		
		/**
		 * Loads the default media settings value.
		 * 
		 * @param db
		 *            the database to insert the value into
		 */
		private void loadMediaSettings(SQLiteDatabase db) {
			SQLiteStatement stmt = null;
			try {
				stmt = db.compileStatement("INSERT OR IGNORE INTO media_settings(name,value) VALUES(?,?);");
				loadSetting(stmt, MediaSettings.MEDIA_SETTINGS_PICTURE_SIZE, mContext.getResources().getString(R.string.picture_size));
				loadSetting(stmt, MediaSettings.MEDIA_SETTINGS_CAPTURE_AUTO_UPLOAD, mContext.getResources().getString(R.string.capture_auto_upload));
				loadSetting(stmt, MediaSettings.MEDIA_SETTINGS_VIDEO_RESOLUTION, mContext.getResources().getString(R.string.video_resolution));
				loadSetting(stmt, MediaSettings.MEDIA_SETTINGS_PRERE_RECORD_TIME, mContext.getResources().getString(R.string.prere_record_time));
				loadSetting(stmt, MediaSettings.MEDIA_SETTINGS_DELAY_RECORD_TIME, mContext.getResources().getString(R.string.delay_record_time));
				loadSetting(stmt, MediaSettings.MEDIA_SETTINGS_VIDEO_SUBSECTION, mContext.getResources().getString(R.string.video_subsection));
				loadSetting(stmt, MediaSettings.MEDIA_SETTINGS_INFRARED_LED, mContext.getResources().getString(R.string.infrared_led));
				loadSetting(stmt, MediaSettings.MEDIA_SETTINGS_FLOATING_WINDOW, mContext.getResources().getString(R.string.floating_window));
			} finally {
				if (stmt != null)
					stmt.close();
			}
		}
		
		private void loadSetting(SQLiteStatement stmt, String key, Object value) {
			stmt.bindString(1, key);
			stmt.bindString(2, value.toString());
			stmt.execute();
		}
		
	}
	
	static final class MediaSettings{
		public static final String MEDIA_SETTINGS_PICTURE_SIZE = "media_settings_picture_size";
		public static final String MEDIA_SETTINGS_CAPTURE_AUTO_UPLOAD = "media_settings_capture_auto_upload";
		public static final String MEDIA_SETTINGS_VIDEO_RESOLUTION = "media_settings_video_resolution";
		public static final String MEDIA_SETTINGS_PRERE_RECORD_TIME = "media_settings_prere_record_time";
		public static final String MEDIA_SETTINGS_DELAY_RECORD_TIME = "media_settings_delay_record_time";
		public static final String MEDIA_SETTINGS_VIDEO_SUBSECTION = "media_settings_video_subsection";
		public static final String MEDIA_SETTINGS_INFRARED_LED = "media_settings_infrared_led";
		public static final String MEDIA_SETTINGS_FLOATING_WINDOW = "media_settings_floating_window";
	}
	
	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext(), DatabaseHelper.DATABASE_NAME, DatabaseHelper.DATABSE_VERSION);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "query uri:" + uri);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		switch (uriMatcher.match(uri)) {
		case VIDEO_CODE:
			return db.query(DatabaseHelper.TABLE_VIDEO, projection, selection, selectionArgs, null, null, sortOrder);
		case IMAGE_CODE:
			return db.query(DatabaseHelper.TABLE_IMAGE, projection, selection, selectionArgs, null, null, sortOrder);
		case AUDIO_CODE:
			return db.query(DatabaseHelper.TABLE_AUDIO, projection, selection, selectionArgs, null, null, sortOrder);
		case MEDIA_SETTINGS_CODE:
			return db.query(DatabaseHelper.TABLE_MEDIA_SETTINGS, projection, selection, selectionArgs, null, null, sortOrder);
		default:
			throw new IllegalArgumentException(UNKNOW_URI + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case VIDEO_CODE:
			return VIDEO_CONTENT_TYPE;
		case IMAGE_CODE:
			return IMAGE_CONTENT_TYPE;
		case AUDIO_CODE:
			return AUDIO_CONTENT_TYPE;
		case MEDIA_SETTINGS_CODE:
			return MEDIA_SETTINGS_CONTENT_TYPE;

		default:
			throw new IllegalArgumentException(UNKNOW_URI + uri);
		}

	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long id = 0;
		switch (uriMatcher.match(uri)) {
		case VIDEO_CODE:
			id = db.insert(DatabaseHelper.TABLE_VIDEO, null, values);
			return ContentUris.withAppendedId(uri, id);
		case IMAGE_CODE:
			id = db.insert(DatabaseHelper.TABLE_IMAGE, null, values);
			return ContentUris.withAppendedId(uri, id);
		case AUDIO_CODE:
			id = db.insert(DatabaseHelper.TABLE_AUDIO, null, values);
			return ContentUris.withAppendedId(uri, id);
		case MEDIA_SETTINGS_CODE:
			id = db.insert(DatabaseHelper.TABLE_MEDIA_SETTINGS, null, values);
			return ContentUris.withAppendedId(uri, id);
		default:
			throw new IllegalArgumentException(UNKNOW_URI + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case VIDEO_CODE:
			count = db.delete(DatabaseHelper.TABLE_VIDEO, selection, selectionArgs);
			break;
		case IMAGE_CODE:
			count = db.delete(DatabaseHelper.TABLE_IMAGE, selection, selectionArgs);
			break;
		case AUDIO_CODE:
			count = db.delete(DatabaseHelper.TABLE_AUDIO, selection, selectionArgs);
			break;
		case MEDIA_SETTINGS_CODE:
			count = db.delete(DatabaseHelper.TABLE_MEDIA_SETTINGS, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException(UNKNOW_URI + uri);
		}
		db.close();
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case VIDEO_CODE:
			count = db.update(DatabaseHelper.TABLE_VIDEO, values, selection, selectionArgs);
			break;
		case IMAGE_CODE:
			count = db.update(DatabaseHelper.TABLE_IMAGE, values, selection, selectionArgs);
			break;
		case AUDIO_CODE:
			count = db.update(DatabaseHelper.TABLE_AUDIO, values, selection, selectionArgs);
			break;
		case MEDIA_SETTINGS_CODE:
			count = db.update(DatabaseHelper.TABLE_MEDIA_SETTINGS, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException(UNKNOW_URI + uri);
		}
		db.close();
		return count;
	}

}
