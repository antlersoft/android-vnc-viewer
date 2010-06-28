/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package android.androidVNC;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.antlersoft.android.bc.BCFactory;

/**
 * An AbstractInputHandler that uses GestureDetector to detect standard gestures in touch events
 * 
 * @author Michael A. MacDonald
 */
abstract class AbstractGestureInputHandler extends GestureDetector.SimpleOnGestureListener implements AbstractInputHandler {
	protected GestureDetector gestures;
	
	AbstractGestureInputHandler(Context c)
	{
		gestures=BCFactory.getInstance().getBCGestureDetector().createGestureDetector(c, this);
		gestures.setOnDoubleTapListener(this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent evt) {
		return gestures.onTouchEvent(evt);
	}
}
