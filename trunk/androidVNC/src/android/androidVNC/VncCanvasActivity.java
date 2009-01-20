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

import android.androidVNC.Provider.VncSettings;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
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

	AbstractInputHandler inputHandler;
	
	VncCanvas vncCanvas;
	

	private MenuItem[] inputModeMenuItems;
	private AbstractInputHandler inputModeHandlers[];
	private static final int inputModeIds[] = { R.id.itemInputFitToScreen, R.id.itemInputMouse, R.id.itemInputPan, R.id.itemInputTouchPanTrackballMouse };

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Intent intent = getIntent();
		String host = null;
		int port = 5900;
		String password = null;
		COLORMODEL colorModel = COLORMODEL.C64;
		String repeaterID = null;
		
		if (intent.getData() != null) {
		  VncSettings settings = VncSettings.getHelper(this, intent.getData());
		  host = settings.getString(VncSettings.HOST);
		  port = Integer.parseInt(settings.getString(VncSettings.PORT));
		  password = settings.getString(VncSettings.PASSWORD);
		  colorModel = COLORMODEL.values()[settings.getInt(VncSettings.COLORMODEL)];
		  repeaterID = "";
		} else {
	    Bundle extras = getIntent().getExtras();
	    host = extras.getString(VncConstants.HOST);
	    if (host == null) 
	      host = extras.getString(VncConstants.IP);
	    port = extras.getInt(VncConstants.PORT);
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
	    password = extras.getString(VncConstants.PASSWORD);
	    repeaterID = extras.getString(VncConstants.ID);
	    colorModel = (COLORMODEL)extras.getSerializable(VncConstants.COLORMODEL);
		}
		vncCanvas = new VncCanvas(this, host, port, password, repeaterID, colorModel);
		setContentView(vncCanvas);
		
		inputHandler=getInputHandlerById(R.id.itemInputFitToScreen);
	}
	
	@Override 
    public void onConfigurationChanged(Configuration newConfig) { 
      // ignore orientation/keyboard change 
      super.onConfigurationChanged(newConfig); 
    } 

	@Override 
    protected void onStop() { 
      vncCanvas.disableRepaints(); 
      super.onStop();       
    } 

	@Override 
    protected void onRestart() { 
      vncCanvas.enableRepaints(); 
      super.onRestart();       
    } 

	/** {@inheritDoc} */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.vnccanvasactivitymenu, menu);
		
		Menu inputMenu = menu.findItem( R.id.itemInputMode).getSubMenu();
		
		inputModeMenuItems = new MenuItem[inputModeIds.length];
		for ( int i=0; i<inputModeIds.length; i++)
		{
			inputModeMenuItems[i]=inputMenu.findItem(inputModeIds[i]);
		}
		updateInputMenu();
		return true;
	}
	
	/**
	 * Change the input mode sub-menu to reflect change in scaling
	 */
	void updateInputMenu()
	{
		if ( isFitToScreen())
		{
			for ( MenuItem item : inputModeMenuItems)
			{
				item.setEnabled(false);
				if ( item.getItemId()==R.id.itemInputFitToScreen)
					item.setChecked(true);
			}
		}
		else
		{
			for ( MenuItem item : inputModeMenuItems)
			{
				int id=item.getItemId();
				item.setEnabled( id!=R.id.itemInputFitToScreen);
				if ( id==R.id.itemInputPan)
				{
					item.setChecked(true);
				}
			}
		}
	}
	
	AbstractInputHandler getInputHandlerById( int id)
	{
		if ( inputModeHandlers==null)
		{
			inputModeHandlers=new AbstractInputHandler[inputModeIds.length];
		}
		for ( int i=0; i<inputModeIds.length; ++i)
		{
			if ( inputModeIds[i]==id)
			{
				if ( inputModeHandlers[i]==null)
				{
					switch ( id )
					{
					case R.id.itemInputFitToScreen :
						inputModeHandlers[i]=new FitToScreenMode();
						break;
					case R.id.itemInputPan :
						inputModeHandlers[i]=new PanMode();
						break;
					case R.id.itemInputMouse :
						inputModeHandlers[i]=new MouseMode();
						break;
					case R.id.itemInputTouchPanTrackballMouse :
						inputModeHandlers[i]=new TouchPanTrackballMouse();
						break;
					default:
						throw new IllegalArgumentException("unknown id" + id);
					}
				}
				return inputModeHandlers[i];
			}
		}
		return null;
	}

	int absoluteXPosition = 0, absoluteYPosition = 0;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemInfo:
			vncCanvas.showConnectionInfo();
			return true;
		case R.id.itemColorMode:
			selectColorModel();
			return true;
		case R.id.itemOneToOne:
			inputHandler = getInputHandlerById(R.id.itemInputPan);
			item.setChecked(true);
			showPanningState();
			// Change to 1:1 scaling (which auto-centers)
			vncCanvas.setScaleType(ScaleType.CENTER);
			updateInputMenu();

			// Reset the pan position to (0,0)
			vncCanvas.scrollTo(-vncCanvas.getCenteredXOffset(),-vncCanvas.getCenteredYOffset());
			return true;
		case R.id.itemFitToScreen:
			inputHandler = getInputHandlerById(R.id.itemFitToScreen);
			item.setChecked(true);
			vncCanvas.setScaleType(ScaleType.FIT_CENTER);
			absoluteXPosition = 0;
			absoluteYPosition = 0;
			updateInputMenu();
			vncCanvas.scrollTo(absoluteXPosition, absoluteYPosition);
			return true;
		case R.id.itemCenterMouse:
			Display display=getWindowManager().getDefaultDisplay();
			vncCanvas.warpMouse( absoluteXPosition + display.getWidth()/2, absoluteYPosition + display.getHeight()/2);
			return true;
		case R.id.itemDisconnect:
			vncCanvas.closeConnection();
			finish();
			return true;
		case R.id.itemCtrlAltDel:
			vncCanvas.ctrlAltDel();
			return true;
		default :
			AbstractInputHandler input=getInputHandlerById(item.getItemId());
			if ( input != null)
			{
				inputHandler=input;
				item.setChecked(true);
				showPanningState();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing())
		{
			vncCanvas.closeConnection();
			vncCanvas.onDestroy();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent evt) {
		if (keyCode == KeyEvent.KEYCODE_MENU)
			return super.onKeyDown( keyCode, evt);
		
		return inputHandler.onKeyDown(keyCode, evt);
	}
		
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent evt) {
		if (keyCode == KeyEvent.KEYCODE_MENU)
			return super.onKeyUp( keyCode, evt);
		
		return inputHandler.onKeyUp(keyCode, evt);
	}

	public void showPanningState() {
		Toast.makeText(this, inputHandler.getHandlerDescription(), Toast.LENGTH_SHORT).show();
	}

	private boolean isFitToScreen() {
		return vncCanvas.getScaleType() == ScaleType.FIT_CENTER;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onTrackballEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		return inputHandler.onTrackballEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return inputHandler.onTouchEvent(event);
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
	
	float panTouchX, panTouchY;

	/**
	 * Pan based on touch motions
	 * @param event
	 */
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

	boolean defaultKeyDownHandler( int keyCode, KeyEvent evt)
	{
		if ( vncCanvas.processLocalKeyEvent(keyCode, evt))
			return true;
		return super.onKeyDown(keyCode, evt);
	}
	
	boolean defaultKeyUpHandler( int keyCode, KeyEvent evt)
	{
		if ( vncCanvas.processLocalKeyEvent(keyCode, evt))
			return true;
		return super.onKeyUp(keyCode, evt);
	}
	
	boolean touchPan( MotionEvent event)
	{
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			panTouchX = event.getX();
			panTouchY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			pan(event);
			panTouchX = event.getX();
			panTouchY = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			pan(event);
			break;
		}
		return true;		
	}
	
	boolean trackballMouse( MotionEvent evt)
	{
		int dx = (int)(evt.getX() * 6.01);
		int dy = (int)(evt.getY() * 6.01);
		
		dx = dx * dx * dx;
		dy = dy * dy * dy;
		
		evt.offsetLocation( vncCanvas.mouseX + dx - evt.getX(), vncCanvas.mouseY + dy - evt.getY());
		
		if (vncCanvas.processPointerEvent(evt))
			return true;
		return VncCanvasActivity.super.onTouchEvent(evt);		
	}
	
	/**
	 * Touches and dpad (trackball) pan the screen
	 * @author Michael A. MacDonald
	 *
	 */
	class PanMode implements AbstractInputHandler
	{

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onKeyDown(int, android.view.KeyEvent)
		 */
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent evt) {
			// DPAD KeyDown events are move MotionEvents in Panning Mode
			final int dPos = 100;
			boolean result=false;
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
				result=true;
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				onTouchEvent(MotionEvent.obtain(1, System.currentTimeMillis(), MotionEvent.ACTION_MOVE, panTouchX + dPos, panTouchY, 0));
				result=true;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				onTouchEvent(MotionEvent.obtain(1, System.currentTimeMillis(), MotionEvent.ACTION_MOVE, panTouchX - dPos, panTouchY, 0));
				result=true;
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				onTouchEvent(MotionEvent.obtain(1, System.currentTimeMillis(), MotionEvent.ACTION_MOVE, panTouchX, panTouchY + dPos, 0));
				result=true;
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				onTouchEvent(MotionEvent.obtain(1, System.currentTimeMillis(), MotionEvent.ACTION_MOVE, panTouchX, panTouchY - dPos, 0));
				result=true;
				break;
			default:
				result=defaultKeyDownHandler( keyCode, evt);
				break;
			}
			return result;
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onKeyUp(int, android.view.KeyEvent)
		 */
		@Override
		public boolean onKeyUp(int keyCode, KeyEvent evt) {
			// Ignore KeyUp events for DPAD keys in Panning Mode; trackball button switches to mouse mode
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER :
				inputHandler=getInputHandlerById(R.id.itemInputMouse);
				for ( MenuItem item : inputModeMenuItems)
				{
					if ( item.getItemId()==R.id.itemInputMouse) {
						item.setChecked( true );
						break;
					}
				}
				showPanningState();
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				return true;
			case KeyEvent.KEYCODE_DPAD_UP:
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				return true;
			}
			return defaultKeyUpHandler( keyCode, evt);
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onTouchEvent(android.view.MotionEvent)
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			return touchPan(event);
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onTrackballEvent(android.view.MotionEvent)
		 */
		@Override
		public boolean onTrackballEvent(MotionEvent evt) {
			return false;
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#handlerDescription()
		 */
		@Override
		public CharSequence getHandlerDescription() {
			return getResources().getText(R.string.input_mode_panning);
		}
		
	}
	
	/**
	 * The touchscreen pans the screen; the trackball moves and clicks the mouse.
	 * @author Michael A. MacDonald
	 *
	 */
	public class TouchPanTrackballMouse implements AbstractInputHandler {

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onKeyDown(int, android.view.KeyEvent)
		 */
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent evt) {
			return defaultKeyDownHandler(keyCode, evt);
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onKeyUp(int, android.view.KeyEvent)
		 */
		@Override
		public boolean onKeyUp(int keyCode, KeyEvent evt) {
			return defaultKeyUpHandler(keyCode, evt);
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onTouchEvent(android.view.MotionEvent)
		 */
		@Override
		public boolean onTouchEvent(MotionEvent evt) {
			return touchPan(evt);
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onTrackballEvent(android.view.MotionEvent)
		 */
		@Override
		public boolean onTrackballEvent(MotionEvent evt) {
			return trackballMouse(evt);
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#handlerDescription()
		 */
		@Override
		public CharSequence getHandlerDescription() {
			return getResources().getText(R.string.input_mode_touchpad_pan_trackball_mouse);
		}
		
	}

	/**
	 * In fit-to-screen mode, no panning.  Trackball and touchscreen work as mouse.
	 * @author Michael A. MacDonald
	 *
	 */
	public class FitToScreenMode implements AbstractInputHandler {

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onKeyDown(int, android.view.KeyEvent)
		 */
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent evt) {
			return defaultKeyDownHandler(keyCode, evt);
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onKeyUp(int, android.view.KeyEvent)
		 */
		@Override
		public boolean onKeyUp(int keyCode, KeyEvent evt) {
			return defaultKeyUpHandler(keyCode, evt);
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onTouchEvent(android.view.MotionEvent)
		 */
		@Override
		public boolean onTouchEvent(MotionEvent evt) {
			// TODO Auto-generated method stub
			return false;
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onTrackballEvent(android.view.MotionEvent)
		 */
		@Override
		public boolean onTrackballEvent(MotionEvent evt) {
			return trackballMouse(evt);
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#handlerDescription()
		 */
		@Override
		public CharSequence getHandlerDescription() {
			return getResources().getText(R.string.input_mode_fit_to_screen);
		}
		
	}

	/**
	 * Touch screen controls, clicks the mouse.
	 * @author Michael A. MacDonald
	 *
	 */
	class MouseMode implements AbstractInputHandler {

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onKeyDown(int, android.view.KeyEvent)
		 */
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent evt) {
			if ( keyCode==KeyEvent.KEYCODE_DPAD_CENTER)
				return true;
			return defaultKeyDownHandler( keyCode, evt);
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onKeyUp(int, android.view.KeyEvent)
		 */
		@Override
		public boolean onKeyUp(int keyCode, KeyEvent evt) {
			if ( keyCode==KeyEvent.KEYCODE_DPAD_CENTER)
			{
				inputHandler=getInputHandlerById( R.id.itemInputPan);
				for ( MenuItem item : inputModeMenuItems)
				{
					if ( item.getItemId()==R.id.itemInputPan) {
						item.setChecked( true );
						break;
					}
				}
				showPanningState();
				return true;
			}
			return defaultKeyDownHandler( keyCode, evt);
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onTouchEvent(android.view.MotionEvent)
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			// Mouse Pointer Control Mode
			// Pointer event is absolute coordinates.

			// Adjust coordinates for Android notification bar.
			event.offsetLocation(0, -1f * vncCanvas.getTop());

			// Adjust coordinates for panning position.
			event.offsetLocation(absoluteXPosition, absoluteYPosition);
			if (vncCanvas.processPointerEvent(event))
				return true;
			return VncCanvasActivity.super.onTouchEvent(event);
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#onTrackballEvent(android.view.MotionEvent)
		 */
		@Override
		public boolean onTrackballEvent(MotionEvent evt) {
			return false;
		}

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#handlerDescription()
		 */
		@Override
		public CharSequence getHandlerDescription() {
			return getResources().getText(R.string.input_mode_mouse);
		}
		
	}
}
