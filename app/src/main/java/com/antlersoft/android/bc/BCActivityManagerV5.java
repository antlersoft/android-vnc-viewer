/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package com.antlersoft.android.bc;

import android.annotation.SuppressLint;
import android.app.ActivityManager;

/**
 * @author Michael A. MacDonald
 */
@SuppressLint("NewApi")
public class BCActivityManagerV5 implements IBCActivityManager {

	/* (non-Javadoc)
	 * @see com.antlersoft.android.bc.IBCActivityManager#getMemoryClass(android.app.ActivityManager)
	 */
	@SuppressLint("NewApi")
	@Override
	public int getMemoryClass(ActivityManager am) {
		return am.getMemoryClass();
	}

}
