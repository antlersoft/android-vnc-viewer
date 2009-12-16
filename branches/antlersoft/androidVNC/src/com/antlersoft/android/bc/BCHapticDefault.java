/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package com.antlersoft.android.bc;

import android.view.View;
import android.view.HapticFeedbackConstants;

/**
 * Implementation for SDK version >= 3
 * @author Michael A. MacDonald
 */
class BCHapticDefault implements IBCHaptic {

	/* (non-Javadoc)
	 * @see com.antlersoft.android.bc.IBCHaptic#performLongPressHaptic(android.view.View)
	 */
	@Override
	public boolean performLongPressHaptic(View v) {
		return v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
	}

}
