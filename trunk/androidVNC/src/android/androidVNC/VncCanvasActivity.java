/* 
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 */

//
// CanvasView is the Activity for showing VNC Desktop.
//
package android.androidVNC;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;

public class VncCanvasActivity extends Activity {
	private final static String TAG = "VncCanvasActivity";

	private boolean panningMode = true;

	private VncCanvas vncCanvas;

	private final static int MENU_ITEM_INFO = Menu.FIRST;
	private final static int MENU_ITEM_SETTINGS = Menu.FIRST + 1;
	private final static int MENU_ITEM_ONE2ONE = Menu.FIRST + 2;
	private final static int MENU_ITEM_FITSCREEN = Menu.FIRST + 3;
	private final static int MENU_ITEM_DISCONNECT = Menu.FIRST + 4;
	private final static int MENU_ITEM_CTRLALTDEL = Menu.FIRST + 5;

	@Override
	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Bundle extras = getIntent().getExtras();
		String host = extras.getString("HOST");
		if (host == null)
			host = extras.getString("IP");
		int port = extras.getInt("PORT");
		if (port == 0)
			port = 5900;

		// Parse a HOST:PORT entry
		if (host.indexOf(':') > -1) {
			String p = host.substring(host.indexOf(':') + 1);
			try {
				port = Integer.parseInt(p);
			} catch (Exception e) {
			}
			host = host.substring(0, host.indexOf(':'));
		}

		String password = extras.getString("PASSWORD");
		String repeaterID = extras.getString("ID");

		vncCanvas = new VncCanvas(this, host, port, password, repeaterID);
		setContentView(vncCanvas);
	}
	
	@Override 
    public void onConfigurationChanged(Configuration newConfig) { 
      // ignore orientation/keyboard change 
      super.onConfigurationChanged(newConfig); 
    } 

	@Override 
    protected void onPause() { 
      vncCanvas.disableRepaints(); 
      super.onPause();       
    } 

	@Override 
    protected void onResume() { 
      vncCanvas.enableRepaints(); 
      super.onResume();       
    } 

	/** {@inheritDoc} */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MENU_ITEM_INFO, 0, "Info").setShortcut('1', 'i').setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(Menu.NONE, MENU_ITEM_SETTINGS, 0, "Settings").setShortcut('2', 's').setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, MENU_ITEM_ONE2ONE, 0, "1:1").setShortcut('3', 'r').setIcon(android.R.drawable.ic_menu_zoom);
		menu.add(Menu.NONE, MENU_ITEM_FITSCREEN, 0, "Fit-to-Screen").setShortcut('4', 'c').setIcon(android.R.drawable.ic_menu_zoom);
		menu.add(Menu.NONE, MENU_ITEM_CTRLALTDEL, 0, "Ctrl-Alt-Del").setShortcut('6', 'a').setIcon(android.R.drawable.ic_menu_share);
		menu.add(Menu.NONE, MENU_ITEM_DISCONNECT, 0, "Disconnect").setShortcut('5', 'd').setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return super.onCreateOptionsMenu(menu);
	}

	/** {@inheritDoc} */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Hide the button for the current scaling mode
		boolean isFitToScreen = vncCanvas.getScaleType() == ScaleType.FIT_CENTER;
		menu.findItem(MENU_ITEM_ONE2ONE).setVisible(isFitToScreen);
		menu.findItem(MENU_ITEM_FITSCREEN).setVisible(!isFitToScreen);
		return true;
	}
	
	int absoluteXPosition = 0, absoluteYPosition = 0;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_INFO:
			vncCanvas.showConnectionInfo();
			return true;
		case MENU_ITEM_SETTINGS:
			selectColorModel();
			return true;
		case MENU_ITEM_ONE2ONE:
			panningMode = true;
			showPanningState();
			// Change to 1:1 scaling (which auto-centers)
			vncCanvas.setScaleType(ScaleType.CENTER);

			// Reset the pan position to (0,0)
			absoluteXPosition = vncCanvas.getCenteredXOffset();
			absoluteYPosition = vncCanvas.getCenteredYOffset();
			vncCanvas.scrollBy(-1 * absoluteXPosition, -1 * absoluteYPosition);
			absoluteXPosition = 0;
			absoluteYPosition = 0;
			return true;
		case MENU_ITEM_FITSCREEN:
			vncCanvas.setScaleType(ScaleType.FIT_CENTER);
			absoluteXPosition = 0;
			absoluteYPosition = 0;
			vncCanvas.scrollTo(absoluteXPosition, absoluteYPosition);
			return true;
		case MENU_ITEM_DISCONNECT:
			vncCanvas.closeConnection();
			finish();
			return true;
		case MENU_ITEM_CTRLALTDEL:
			vncCanvas.ctrlAltDel();
			return true;
	}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing())
			vncCanvas.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent evt) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			return super.onKeyDown(keyCode, evt);
		case KeyEvent.KEYCODE_DPAD_CENTER:
			return true;
		}
		if (panningMode) {
			// DPAD KeyDown events are move MotionEvents in Panning Mode
			final int dPos = 100;
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				onTouchEvent(MotionEvent.obtain(1, System.currentTimeMillis(), MotionEvent.ACTION_MOVE, panTouchX + dPos, panTouchY, 0));
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				onTouchEvent(MotionEvent.obtain(1, System.currentTimeMillis(), MotionEvent.ACTION_MOVE, panTouchX - dPos, panTouchY, 0));
				return true;
			case KeyEvent.KEYCODE_DPAD_UP:
				onTouchEvent(MotionEvent.obtain(1, System.currentTimeMillis(), MotionEvent.ACTION_MOVE, panTouchX, panTouchY + dPos, 0));
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				onTouchEvent(MotionEvent.obtain(1, System.currentTimeMillis(), MotionEvent.ACTION_MOVE, panTouchX, panTouchY - dPos, 0));
				return true;
			}
		}
		if (vncCanvas.processLocalKeyEvent(keyCode, evt))
			return true;
		return super.onKeyDown(keyCode, evt);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent evt) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			return super.onKeyUp(keyCode, evt);
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if (isFitToScreen())
				// Do nothing in Fit-to-Screen mode
				return super.onKeyUp(keyCode, evt);
			panningMode = !panningMode;
			showPanningState();
			return true;
		}
		if (panningMode) {
			// Ignore KeyUp events for DPAD keys in Panning Mode
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				return true;
			case KeyEvent.KEYCODE_DPAD_UP:
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				return true;
			}
		}
		if (vncCanvas.processLocalKeyEvent(keyCode, evt))
			return true;
		return super.onKeyDown(keyCode, evt);
	}

	public void showPanningState() {
		String msg = "Desktop Panning Mode";
		if (!panningMode)
			msg = "Mouse Pointer Control Mode";
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private boolean isFitToScreen() {
		return vncCanvas.getScaleType() == ScaleType.FIT_CENTER;
	}

	private float panTouchX, panTouchY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Ignore touches for fit-to-screen scaling
		if (isFitToScreen())
			return super.onTouchEvent(event);

		if (panningMode) {
			// Panning Mode
			// User may move view of desktop.
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				panTouchX = event.getX();
				panTouchY = event.getY();
				return true;
			case MotionEvent.ACTION_MOVE:
				pan(event);
				panTouchX = event.getX();
				panTouchY = event.getY();
				return true;
			case MotionEvent.ACTION_UP:
				pan(event);
				return true;
			}
		} else {
			// Mouse Pointer Control Mode
			// Pointer event is absolute coordinates.

			// Adjust coordinates for Android notification bar.
			event.offsetLocation(0, -1f * vncCanvas.getTop());

			// Adjust coordinates for panning position.
			event.offsetLocation(absoluteXPosition, absoluteYPosition);
			if (vncCanvas.processPointerEvent(event))
				return true;
		}
		return super.onTouchEvent(event);
	}

	private void pan(MotionEvent event) {
		float curX = event.getX();
		float curY = event.getY();
		int dX = (int) (panTouchX - curX);
		int dY = (int) (panTouchY - curY);

		// Prevent panning left or above desktop image
		if (absoluteXPosition + dX < 0)
			// dX = diff to 0
			dX = absoluteXPosition * -1;
		if (absoluteYPosition + dY < 0)
			// dY = diff to 0
			dY = absoluteYPosition * -1;

		// Prevent panning right or below desktop image
		if (absoluteXPosition + vncCanvas.getWidth() + dX > vncCanvas.getImageWidth())
			dX = 0;
		if (absoluteYPosition + vncCanvas.getHeight() + dY > vncCanvas.getImageHeight())
			dY = 0;

		absoluteXPosition += dX;
		absoluteYPosition += dY;
		vncCanvas.scrollBy(dX, dY);
	}

	@Override
	public void onStop() {
		vncCanvas.closeConnection();
		super.onStop();
	}

	private void selectColorModel() {
		// Stop repainting the desktop
		// because the display is composited!
		vncCanvas.disableRepaints();
		
		String[] choices = new String[COLORMODEL.values().length];
		int currentSelection = -1;
		for (int i = 0; i < choices.length; i++) {
			COLORMODEL cm = COLORMODEL.values()[i];
			choices[i] = cm.toString();
			if (vncCanvas.isColorModel(cm))
				currentSelection = i;
		}

		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		ListView list = new ListView(this);
		list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, choices));
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		list.setItemChecked(currentSelection, true);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				dialog.dismiss();
				COLORMODEL cm = COLORMODEL.values()[arg2];
				vncCanvas.setColorModel(cm);
				Toast.makeText(VncCanvasActivity.this, "Updating Color Model to " + cm.toString(), Toast.LENGTH_SHORT).show();
			}
		});
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				Log.i(TAG, "Color Model Selector dismissed");
				// Restore desktop repaints
				vncCanvas.enableRepaints();
			}
		});
		dialog.setContentView(list);
		dialog.show();		
	}
}
