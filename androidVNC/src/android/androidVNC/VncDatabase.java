/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package android.androidVNC;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Michael A. MacDonald
 *
 */
class VncDatabase extends SQLiteOpenHelper {
	VncDatabase(Context context)
	{
		super(context,"VncDatabase",null,2);
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(AbstractConnectionBean.GEN_CREATE);
		db.execSQL(MostRecentBean.GEN_CREATE);
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL("DROP TABLE IF EXISTS " + AbstractConnectionBean.GEN_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + MostRecentBean.GEN_TABLE_NAME);
		onCreate(db);
	}

}
