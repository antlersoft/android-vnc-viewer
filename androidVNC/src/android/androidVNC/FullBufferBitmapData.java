/**
 * Copyright (c) 2010 Michael A. MacDonald
 */
package android.androidVNC;

import java.io.IOException;
import java.util.Arrays;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

/**
 * @author Michael A. MacDonald
 *
 */
class FullBufferBitmapData extends AbstractBitmapData {

	int xoffset;
	int yoffset;
	int displaywidth;
	int displayheight;
	
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

		/* (non-Javadoc)
		 * @see android.graphics.drawable.DrawableContainer#draw(android.graphics.Canvas)
		 */
		@Override
		public void draw(Canvas canvas) {
			Log.i("FBBM", "xoffset "+xoffset+" yoffset "+ yoffset);
			canvas.drawBitmap(data.bitmapPixels, offset(xoffset, yoffset), data.framebufferwidth, xoffset, yoffset, displaywidth, displayheight, false, null);
		}

	}

	/**
	 * Multiply this times total number of pixels to get estimate of process size with all buffers plus
	 * safety factor
	 */
	static final int CAPACITY_MULTIPLIER = 17;
	
	/**
	 * @param p
	 * @param c
	 */
	public FullBufferBitmapData(RfbProto p, VncCanvas c, int displayWidth, int displayHeight, int capacity) {
		super(p, c);
		framebufferwidth=rfb.framebufferWidth;
		framebufferheight=rfb.framebufferHeight;
		bitmapwidth=framebufferwidth;
		bitmapheight=framebufferheight;
		displaywidth = displayWidth;
		displayheight = displayHeight;
		android.util.Log.i("FBBM", "bitmapsize = ("+bitmapwidth+","+bitmapheight+") display = ( "+displaywidth+ "," + displayheight+ ")");
		bitmapPixels = new int[framebufferwidth * framebufferheight];
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
		return new Drawable(this);
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#drawRect(int, int, int, int, android.graphics.Paint)
	 */
	@Override
	void drawRect(int x, int y, int w, int h, Paint paint) {
		int color = paint.getColor();
		int offset = offset(x,y);
		if (w > 10)
		{
			for (int j = 0; j < h; j++, offset += framebufferwidth)
			{
				Arrays.fill(bitmapPixels, offset, offset + w, color);
			}
		}
		else
		{
			for (int j = 0; j < h; j++, offset += framebufferwidth - w)
			{
				for (int k = 0; k < w; k++, offset++)
				{
					bitmapPixels[offset] = color;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#offset(int, int)
	 */
	@Override
	int offset(int x, int y) {
		return x + y * framebufferwidth;
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#scrollChanged(int, int)
	 */
	@Override
	void scrollChanged(int newx, int newy) {
		xoffset = newx;
		yoffset = newy;
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#syncScroll()
	 */
	@Override
	void syncScroll() {
		// Don't need to do anything here

	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#updateBitmap(int, int, int, int)
	 */
	@Override
	void updateBitmap(int x, int y, int w, int h) {
		// Don't need to do anything here

	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#validDraw(int, int, int, int)
	 */
	@Override
	boolean validDraw(int x, int y, int w, int h) {
		return true;
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#writeFullUpdateRequest(boolean)
	 */
	@Override
	void writeFullUpdateRequest(boolean incremental) throws IOException {
		rfb.writeFramebufferUpdateRequest(0, 0, framebufferwidth, framebufferheight, incremental);
	}

}
