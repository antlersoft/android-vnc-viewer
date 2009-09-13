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

import java.text.MessageFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.database.Cursor;
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
	
	VncDatabase database;

	private MenuItem[] inputModeMenuItems;
	private AbstractInputHandler inputModeHandlers[];
	private ConnectionBean connection;
	private boolean trackballButtonDown;
	private static final int inputModeIds[] = { R.id.itemInputFitToScreen, R.id.itemInputMouse, R.id.itemInputPan, R.id.itemInputTouchPanTrackballMouse };

	@Override
	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		database = new VncDatabase(this);

		Bundle extras = getIntent().getExtras();
		
		connection=new ConnectionBean();
		connection.Gen_populate((ContentValues)extras.getParcelable(VncConstants.CONNECTION));
		if (connection.getPort() == 0)
			connection.setPort(5900);

		// Parse a HOST:PORT entry
		String host=connection.getAddress();
		if (host.indexOf(':') > -1) {
			String p = host.substring(host.indexOf(':') + 1);
			try {
				connection.setPort( Integer.parseInt(p) );
			} catch (Exception e) {
			}
			connection.setAddress( host.substring(0, host.indexOf(':')));
		}

		vncCanvas = new VncCanvas(this, connection, new Runnable() {
			public void run() {
				setModes();
			}
		});
		setContentView(vncCanvas);
		
		inputHandler=getInputHandlerById(R.id.itemInputFitToScreen);
	}
	
	/**
	 * Set modes on start to match what is specified in the ConnectionBean; color mode (already done)
	 * scaling, input mode
	 */
	void setModes() {
		// If scale mode is not default
		if ( connection.getScaleMode() != ScaleType.FIT_CENTER)
		{
			vncCanvas.setScaleType(connection.getScaleMode());
			vncCanvas.scrollTo(-vncCanvas.getCenteredXOffset(),-vncCanvas.getCenteredYOffset());
			AbstractInputHandler input = getInputHandlerByName(connection.getInputMode());
			if (input != null) {
				inputHandler = input;
			}
			updateInputMenu();
		}
	}
	
	ConnectionBean getConnection()
	{
		return connection;
	}
	
	/**
	 * Make sure mouse is visible on displayable part of screen
	 */
	void panToMouse()
	{
		if (! connection.getFollowMouse() || isFitToScreen())
			return;
		int x = vncCanvas.mouseX;
		int y = vncCanvas.mouseY;
		boolean panned = false;
		Display display = getWindowManager().getDefaultDisplay();
		int w = display.getWidth();
		int h = display.getHeight();
		AbstractBitmapData bitmapData = vncCanvas.bitmapData;
		
		int newX = absoluteXPosition;
		int newY = absoluteYPosition;
		
		if (x - newX >= w)
		{
			newX = x - w/2;
			if (newX + w > bitmapData.framebufferwidth)
				newX = bitmapData.framebufferwidth - w;
		}
		else if (x < newX)
		{
			newX = x - w/2;
			if (newX < 0)
				newX = 0;
		}
		if ( newX != absoluteXPosition ) {
			newX = newX - absoluteXPosition;
			absoluteXPosition += newX;
			panned = true;
		} else {
			newX = 0;
		}
		if (y - newY >= h)
		{
			newY = y - h/2;
			if (newY + h > bitmapData.framebufferheight)
				newY = bitmapData.framebufferheight - h;
		}
		else if (y < newY)
		{
			newY = y - h/2;
			if (newY < 0)
				newY = 0;
		}
		if ( newY != absoluteYPosition ) {
			newY = newY - absoluteYPosition;;
			absoluteYPosition += newY;
			panned = true;
		}
		else {
			newY = 0;
		}
		if (panned)
		{
			vncCanvas.scrollBy(newX, newY);
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		return new MetaKeyDialog(this);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		((MetaKeyDialog)dialog).setConnection(connection);
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
		
		menu.findItem(isFitToScreen() ? R.id.itemFitToScreen : R.id.itemOneToOne).setChecked(true);
			
		
		Menu inputMenu = menu.findItem( R.id.itemInputMode).getSubMenu();
		
		inputModeMenuItems = new MenuItem[inputModeIds.length];
		for ( int i=0; i<inputModeIds.length; i++)
		{
			inputModeMenuItems[i]=inputMenu.findItem(inputModeIds[i]);
		}
		updateInputMenu();
		menu.findItem(R.id.itemFollowMouse).setChecked(connection.getFollowMouse());
		return true;
	}
	
	/**
	 * Change the input mode sub-menu to reflect change in scaling
	 */
	void updateInputMenu()
	{
		if ( inputModeMenuItems == null ) {
			return;
		}
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
				if (inputHandler == getInputHandlerById(id))
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
					}
				}
				return inputModeHandlers[i];
			}
		}
		return null;
	}
	
	AbstractInputHandler getInputHandlerByName(String name)
	{
		AbstractInputHandler result = null;
		for (int id : inputModeIds) {
			AbstractInputHandler handler = getInputHandlerById( id );
			if (handler.getName().equals(name)) {
				result = handler;
				break;
			}
		}
		return result;
	}

	int absoluteXPosition = 0, absoluteYPosition = 0;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		vncCanvas.afterMenu = true;
		switch (item.getItemId()) {
		case R.id.itemInfo:
			vncCanvas.showConnectionInfo();
			return true;
		case R.id.itemSpecialKeys:
			showDialog(R.layout.metakey);
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
			connection.setScaleMode(ScaleType.CENTER);
			updateInputMenu();
			// Reset the pan position to (0,0)
			vncCanvas.scrollTo(-vncCanvas.getCenteredXOffset(),-vncCanvas.getCenteredYOffset());
			connection.setInputMode(inputHandler.getName());
			connection.save(database.getWritableDatabase());
			return true;
		case R.id.itemFitToScreen:
			inputHandler = getInputHandlerById(R.id.itemInputFitToScreen);
			item.setChecked(true);
			vncCanvas.setScaleType(ScaleType.FIT_CENTER);
			connection.setScaleMode(ScaleType.FIT_CENTER);
			absoluteXPosition = 0;
			absoluteYPosition = 0;
			updateInputMenu();
			vncCanvas.scrollTo(absoluteXPosition, absoluteYPosition);
			connection.setInputMode(inputHandler.getName());
			connection.save(database.getWritableDatabase());
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
			vncCanvas.sendMetaKey(MetaKeyBean.keyCtrlAltDel);
			return true;
		case R.id.itemFollowMouse:
			boolean newFollow = ! connection.getFollowMouse();
			item.setChecked(newFollow);
			connection.setFollowMouse(newFollow);
			if (newFollow) {
				panToMouse();
			}
			connection.save(database.getWritableDatabase());
			return true;
		case R.id.itemArrowLeft :
			vncCanvas.sendMetaKey(MetaKeyBean.keyArrowLeft);
			return true;
		case R.id.itemArrowUp :
			vncCanvas.sendMetaKey(MetaKeyBean.keyArrowUp);
			return true;
		case R.id.itemArrowRight :
			vncCanvas.sendMetaKey(MetaKeyBean.keyArrowRight);
			return true;
		case R.id.itemArrowDown :
			vncCanvas.sendMetaKey(MetaKeyBean.keyArrowDown);
			return true;
		case R.id.itemSendKeyAgain :
			sendSpecialKeyAgain();
			return true;
		default :
			AbstractInputHandler input=getInputHandlerById(item.getItemId());
			if ( input != null)
			{
				inputHandler=input;
				connection.setInputMode(input.getName());
				item.setChecked(true);
				showPanningState();
				connection.save(database.getWritableDatabase());
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	private MetaKeyBean lastSentKey;
	
	private void sendSpecialKeyAgain()
	{
		if (lastSentKey == null || lastSentKey.get_Id() != connection.getLastMetaKeyId())
		{
			ArrayList<MetaKeyBean> keys = new ArrayList<MetaKeyBean>();
			Cursor c = database.getReadableDatabase().rawQuery(
					MessageFormat.format("SELECT * FROM {0} WHERE {1} = {2}",
							MetaKeyBean.GEN_TABLE_NAME,
							MetaKeyBean.GEN_FIELD__ID,
							connection.getLastMetaKeyId()),
					MetaKeyDialog.EMPTY_ARGS);
			MetaKeyBean.Gen_populateFromCursor(
					c,
					keys,
					MetaKeyBean.NEW);
			c.close();
			if (keys.size() > 0) {
				lastSentKey = keys.get(0);
			}
			else {
				lastSentKey = null;
			}
		}
		if (lastSentKey != null)
			vncCanvas.sendMetaKey(lastSentKey);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing())
		{
			vncCanvas.closeConnection();
			vncCanvas.onDestroy();
			database.close();
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
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			trackballButtonDown = true;
			break;
		case MotionEvent.ACTION_UP:
			trackballButtonDown = false;
			break;
		}
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
				connection.setColorModel(cm.nameString());
				connection.save(database.getWritableDatabase());
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
		
		if (vncCanvas.processPointerEvent(evt,trackballButtonDown)) {
			return true;
		}
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
				connection.setInputMode(inputHandler.getName());
				connection.save(database.getWritableDatabase());
				updateInputMenu();
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

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#getName()
		 */
		@Override
		public String getName() {
			return "PAN_MODE";
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

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#getName()
		 */
		@Override
		public String getName() {
			return "TOUCH_PAN_TRACKBALL_MOUSE";
		}
		
	}

	static final String FIT_SCREEN_NAME = "FIT_SCREEN";
	
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

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#getName()
		 */
		@Override
		public String getName() {
			return FIT_SCREEN_NAME;
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
				showPanningState();
				connection.setInputMode(inputHandler.getName());
				connection.save(database.getWritableDatabase());
				updateInputMenu(); 
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
			if (vncCanvas.processPointerEvent(event,true))
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

		/* (non-Javadoc)
		 * @see android.androidVNC.AbstractInputHandler#getName()
		 */
		@Override
		public String getName() {
			return "MOUSE";
		}
		
	}
}
