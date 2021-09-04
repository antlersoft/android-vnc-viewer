/**
 * Copyright (c) 2015 Michael A. MacDonald
 */
package com.antlersoft.android.bc;

import android.view.View;

/**
 * @author Michael A. MacDonald
 *
 */
class BCSystemUiVisibility19 implements IBCSystemUiVisibility {

	/* (non-Javadoc)
	 * @see com.antlersoft.android.bc.IBCSystemUiVisibility#HideSystemUI(android.view.View)
	 */
	@Override
	public void HideSystemUI(View v) {
		v.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

	}

}
