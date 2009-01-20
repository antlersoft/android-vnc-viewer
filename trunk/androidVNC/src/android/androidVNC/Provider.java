package android.androidVNC;

import java.util.HashMap;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class Provider extends ContentProvider {

  private static final String ALL = "settings";
  private static final int ALL_MATCHCODE = 1;
  private static final String ONE = ALL + "/#";
  private static final int ONE_MATCHCODE = 2;

  public static final String AUTHORITY = "android.androidVNC.Provider";
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ALL);
  public static final String CONTENT_ALL_TYPE = "vnd.android.cursor.dir/vnd.vnc.config";
  public static final String CONTENT_ONE_TYPE = "vnd.android.cursor.item/vnd.vnc.config";

  private static UriMatcher matcher;

  static {
    matcher = new UriMatcher(ALL_MATCHCODE);
    matcher.addURI(AUTHORITY, ALL, ALL_MATCHCODE);
    matcher.addURI(AUTHORITY, ONE, ONE_MATCHCODE);
  }

  private DatabaseHelper fDatabase;

  @Override
  public boolean onCreate() {
    fDatabase = new DatabaseHelper(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    SQLiteQueryBuilder qb = fDatabase.getQueryBuilder();

    switch (matcher.match(uri)) {
      case ALL_MATCHCODE:
        break;
      case ONE_MATCHCODE:
        qb.appendWhere(VncSettings._ID + "=" + uri.getPathSegments().get(1));
        break;
      default:
        throw new RuntimeException("illegal uri");
    }
    Cursor res = qb.query(fDatabase.getReadableDatabase(), null, null, null, null, null, null);
    res.setNotificationUri(getContext().getContentResolver(), uri);
    return res;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    if (matcher.match(uri) != ONE_MATCHCODE) {
      throw new RuntimeException("nyi");
    }
    
    String segment = uri.getPathSegments().get(1); // contains rowId
    getContext().getContentResolver().notifyChange(uri, null);
    return fDatabase.getWritableDatabase().delete(TABLE_NAME, VncSettings._ID + "=" + segment, null);
  }

  @Override
  public String getType(Uri uri) {
    switch (matcher.match(uri)) {
      case ALL_MATCHCODE:
        return CONTENT_ALL_TYPE;
      case ONE_MATCHCODE:
        return CONTENT_ONE_TYPE;
      default:
        throw new RuntimeException("no match");
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    if (matcher.match(uri) != ALL_MATCHCODE) {
      throw new IllegalArgumentException("insert must be done on the list resource");
    }

    SQLiteDatabase db = fDatabase.getWritableDatabase();
    long id = db.insert(TABLE_NAME, VncSettings.HOST, values);
    if (id > 0) {
      Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id);
      getContext().getContentResolver().notifyChange(newUri, null);
      return newUri;

    }
    throw new SQLException("could not insert element");
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    if (matcher.match(uri) != ONE_MATCHCODE) {
      throw new IllegalArgumentException("update must be called on an item");
    }

    String id = uri.getPathSegments().get(1);
    return fDatabase.getWritableDatabase().update(TABLE_NAME, values, VncSettings._ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
  }

  private static final String TABLE_NAME = "vncsettings";

  public static class VncSettings implements BaseColumns {
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String PASSWORD = "password";
    public static final String COLORMODEL = "colormodel";
    private final Cursor fCursor;
    private static final HashMap<String, Integer> fIndexMap = new HashMap<String, Integer>();
    
    public static VncSettings getHelper(Activity activity, Uri uri) {
      Cursor c = activity.managedQuery(uri, null, null, null, null);
      if (!c.moveToFirst()) {
        return null;
      }
      return new VncSettings(c);
    }

    public String getString(String key) {
      return fCursor.getString(getIndex(key));
    }

    public int getInt(String key) {
      return fCursor.getInt(getIndex(key));
    }
    
    private VncSettings(Cursor c) {
      fCursor = c;
    }
    
    private int getIndex(String key) {
      int idx = -1;
      if (fIndexMap.containsKey(key)) {
        idx = fIndexMap.get(key).intValue();
      } else {
        idx = fCursor.getColumnIndex(key);
        if (idx != -1) {
          fIndexMap.put(key, new Integer(idx));
        }
      }
      
      if (idx == -1) {
        throw new RuntimeException("index for row " + key + " not found");
      }
      return idx;
    }
  }

  private static class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "vnc_settings.db";
    private static final int DATABASE_VERSION = 3;

    DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public SQLiteQueryBuilder getQueryBuilder() {
      SQLiteQueryBuilder res = new SQLiteQueryBuilder();
      res.setTables(TABLE_NAME);
      return res;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + VncSettings._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + VncSettings.HOST + " TEXT," + VncSettings.PORT + " TEXT," + VncSettings.PASSWORD + " TEXT," + VncSettings.COLORMODEL + " INTEGER" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(getClass().getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
      onCreate(db);
    }
  }
}
