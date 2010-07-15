/**
 * Copyright (c) 2010 Michael A. MacDonald
 */
package android.androidVNC;

import java.io.IOException;

import com.antlersoft.android.drawing.RectList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * @author Michael A. MacDonald
 *
 */
class FullBufferBitmapData extends AbstractBitmapData {

	
	/**
	 * @author Michael A. MacDonald
	 *
	 */
	class Drawable extends AbstractBitmapDrawable {

		/**
		 * @param data
		 */
		public Drawable(AbstractBitmapData data) {
			super(data);
			// TODO Auto-generated constructor stub
		}

	}

	/**
	 * Multiply this times total number of pixels to get estimate of process size with all buffers plus
	 * safety factor
	 */
	static final int CAPACITY_MULTIPLIER = 17;
	
	int xoffset;
	int yoffset;
	int scrolledToX;
	int scrolledToY;
	private Rect bitmapRect;
	private Paint defaultPaint;
	/**
	 * @param p
	 * @param c
	 */
	public FullBufferBitmapData(RfbProto p, VncCanvas c, int displayWidth, int displayHeight, int capacity) {
		super(p, c);
		framebufferwidth=rfb.framebufferWidth;
		framebufferheight=rfb.framebufferHeight;
		bitmapwidth=displayWidth;
		bitmapheight=displayHeight;
		android.util.Log.i("FBBM", "bitmapsize = ("+bitmapwidth+","+bitmapheight+")");
		bitmapPixels = new int[bitmapwidth * bitmapheight];
		bitmapRect=new Rect(0,0,bitmapwidth,bitmapheight);
		defaultPaint = new Paint();
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#copyRect(android.graphics.Rect, android.graphics.Rect, android.graphics.Paint)
	 */
	@Override
	void copyRect(Rect src, Rect dest, Paint paint) {
		// TODO copy rect working?
		throw new RuntimeException( "copyrect Not implemented");
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#createDrawable()
	 */
	@Override
	AbstractBitmapDrawable createDrawable() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#drawRect(int, int, int, int, android.graphics.Paint)
	 */
	@Override
	void drawRect(int x, int y, int w, int h, Paint paint) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#offset(int, int)
	 */
	@Override
	int offset(int x, int y) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#scrollChanged(int, int)
	 */
	@Override
	void scrollChanged(int newx, int newy) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#syncScroll()
	 */
	@Override
	void syncScroll() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#updateBitmap(int, int, int, int)
	 */
	@Override
	void updateBitmap(int x, int y, int w, int h) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#validDraw(int, int, int, int)
	 */
	@Override
	boolean validDraw(int x, int y, int w, int h) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#writeFullUpdateRequest(boolean)
	 */
	@Override
	void writeFullUpdateRequest(boolean incremental) throws IOException {
		// TODO Auto-generated method stub

	}

}
