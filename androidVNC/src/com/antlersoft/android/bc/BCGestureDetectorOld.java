/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package com.antlersoft.android.bc;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;

/**
 * Calls the deprecated version in order to have compatibility with pre-1.5 devices
 * @author Michael A. MacDonald
 */
public class BCGestureDetectorOld implements IBCGestureDetector {

	/* (non-Javadoc)
	 * @see com.antlersoft.android.bc.IBCGestureDetector#createGestureDetector(android.content.Context, android.view.GestureDetector.OnGestureListener)
	 */
	@Override
	public GestureDetector createGestureDetector(Context context,
			OnGestureListener listener) {
		return new GestureDetector(listener);
	}

}
