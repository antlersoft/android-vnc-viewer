/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package android.androidVNC;

import android.database.sqlite.SQLiteDatabase;

import com.antlersoft.android.dbimpl.NewInstance;

/**
 * @author Michael A. MacDonald
 *
 */
class ConnectionBean extends AbstractConnectionBean {
	static final NewInstance<ConnectionBean> newInstance=new NewInstance<ConnectionBean>() {
		public ConnectionBean get() { return new ConnectionBean(); }
	};
	ConnectionBean()
	{
		set_Id(0);
		setAddress("0.0.0.0");
		setPassword("");
		setNickname("");
		setPort(5900);
		setColorModel(COLORMODEL.C64.nameString());
		setRepeaterId("");
	}
	
	boolean isNew()
	{
		return get_Id()== 0;
	}
	
	void save(SQLiteDatabase database) {
		if ( isNew()) {
			Gen_insert(database);
		} else {
			Gen_update(database);
		}
	}
	
	@Override
	public String toString() {
		if ( isNew())
		{
			return "New";
		}
		return getNickname()+":"+getAddress()+":"+getPort();
	}
}
