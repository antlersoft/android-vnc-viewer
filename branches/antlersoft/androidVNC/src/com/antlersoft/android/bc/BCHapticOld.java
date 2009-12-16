/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package com.antlersoft.android.bc;

import android.view.View;

/**
 * Default implementation does nothing (we could have it get out the Vibrate service,
 * but that would require an extra permission)
 * @author Michael A. MacDonald
 */
class BCHapticOld implements IBCHaptic {

	/* (non-Javadoc)
	 * @see com.antlersoft.android.bc.IBCHaptic#performLongPressHaptic(android.view.View)
	 */
	@Override
	public boolean performLongPressHaptic(View v) {
		return false;
	}

}
