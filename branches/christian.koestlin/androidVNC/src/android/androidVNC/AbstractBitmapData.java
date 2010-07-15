/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package android.androidVNC;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.ImageView;

/**
 * Abstract interface between the VncCanvas and the bitmap and pixel data buffers that actually contain
 * the data.
 * This allows for implementations that use smaller bitmaps or buffers to save memory. 
 * @author Michael A. MacDonald
 *
 */
abstract class AbstractBitmapData {
	int framebufferwidth;
	int framebufferheight;
	int bitmapwidth;
	int bitmapheight;
	RfbProto rfb;
	Bitmap mbitmap;
	int bitmapPixels[];
	Canvas memGraphics;
	boolean waitingForInput;
	
	AbstractBitmapData( RfbProto p)
	{
		rfb=p;
	}
	
	synchronized void doneWaiting()
	{
		waitingForInput=false;
	}
	
	/**
	 * Send a request through the protocol to get the data for the currently held bitmap
	 * @param incremental True if we want incremental update; false for full update
	 */
	abstract void writeFullUpdateRequest( boolean incremental) throws IOException;
	
	/**
	 * Determine if a rectangle in full-frame coordinates can be drawn in the existing buffer
	 * @param x Top left x
	 * @param y Top left y
	 * @param w width (pixels)
	 * @param h height (pixels)
	 * @return True if entire rectangle fits into current screen buffer, false otherwise
	 */
	abstract boolean validDraw( int x, int y, int w, int h);
	
	/**
	 * Return an offset in the bitmapPixels array of a point int full-frame coordinates
	 * @param x
	 * @param y
	 * @return Offset in bitmapPixels array of color data for that point
	 */
	abstract int offset( int x, int y);
	
	/**
	 * Update pixels in the bitmap with data from the bitmapPixels array, positioned
	 * in full-frame coordinates
	 * @param x Top left x
	 * @param y Top left y
	 * @param w width (pixels)
	 * @param h height (pixels)
	 */
	abstract void updateBitmap( int x, int y, int w, int h);
	
	/**
	 * Call in UI thread; tell ImageView we've changed
	 * @param v ImageView displaying bitmap data
	 */
	abstract void updateView(ImageView v);
	
	/**
	 * Copy a rectangle from one part of the bitmap to another
	 * @param src Rectangle in full-frame coordinates to be copied
	 * @param dest Destination rectangle in full-frame coordinates
	 * @param paint Paint specifier
	 */
	abstract void copyRect( Rect src, Rect dest, Paint paint);
	
	/**
	 * Draw a rectangle in the bitmap with coordinates given in full frame
	 * @param x Top left x
	 * @param y Top left y
	 * @param w width (pixels)
	 * @param h height (pixels)
	 * @param paint How to draw
	 */
	abstract void drawRect( int x, int y, int w, int h, Paint paint);
	
	/**
	 * Scroll position has changed.
	 * <p>
	 * This method is called in the UI thread-- it updates internal status, but does
	 * not change the bitmap data or send a network request until syncScroll is called
	 * @param newx Position of left edge of visible part
	 * @param newy Position of right edge of visible part
	 */
	abstract void scrollChanged( int newx, int newy);
	
	/**
	 * Sync scroll -- called from network thread; copies scroll changes from UI to network state
	 */
	abstract void syncScroll();

	/**
	 * Release resources
	 */
	void dispose()
	{
		if ( mbitmap!=null )
			mbitmap.recycle();
		memGraphics = null;
		bitmapPixels = null;
	}
}
