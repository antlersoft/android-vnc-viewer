/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package android.androidVNC;

import java.io.IOException;

import android.graphics.drawable.DrawableContainer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.widget.ImageView;

/**
 * @author Michael A. MacDonald
 *
 */
class LargeBitmapData extends AbstractBitmapData {
	
	int xoffset;
	int yoffset;
	int scrolledToX;
	int scrolledToY;
	int origwidth;
	int origheight;
	private LargeBitmapDrawable drawable;
	private Rect invalidRect;
	private Rect bitmapRect;
	
	class LargeBitmapDrawable extends DrawableContainer
	{

		/* (non-Javadoc)
		 * @see android.graphics.drawable.DrawableContainer#draw(android.graphics.Canvas)
		 */
		@Override
		public void draw(Canvas canvas) {
			//android.util.Log.i("LBM", "Drawing "+xoffset+" "+yoffset);
			int xoff, yoff;
			synchronized ( LargeBitmapDrawable.this )
			{
				xoff=xoffset;
				yoff=yoffset;
			}
			canvas.drawBitmap(mbitmap, xoff, yoff, new Paint());
		}

		/* (non-Javadoc)
		 * @see android.graphics.drawable.DrawableContainer#getIntrinsicHeight()
		 */
		@Override
		public int getIntrinsicHeight() {
			return framebufferheight;
		}

		/* (non-Javadoc)
		 * @see android.graphics.drawable.DrawableContainer#getIntrinsicWidth()
		 */
		@Override
		public int getIntrinsicWidth() {
			return framebufferwidth;
		}

		/* (non-Javadoc)
		 * @see android.graphics.drawable.DrawableContainer#getOpacity()
		 */
		@Override
		public int getOpacity() {
			return PixelFormat.OPAQUE;
		}

		/* (non-Javadoc)
		 * @see android.graphics.drawable.DrawableContainer#isStateful()
		 */
		@Override
		public boolean isStateful() {
			return false;
		}
	}
	
	LargeBitmapData(RfbProto p, int displayWidth, int displayHeight)
	{
		super(p);
		framebufferwidth=rfb.framebufferWidth;
		framebufferheight=rfb.framebufferHeight;
		origwidth=displayWidth;
		origheight=displayHeight;
		bitmapwidth=origwidth * 2;
		bitmapheight=origheight * 2;
		mbitmap = Bitmap.createBitmap(bitmapwidth, bitmapheight, Bitmap.Config.RGB_565);
		memGraphics = new Canvas(mbitmap);
		bitmapPixels = new int[bitmapwidth * bitmapheight];
		drawable=new LargeBitmapDrawable();
		invalidRect=new Rect();
		bitmapRect=new Rect(0,0,bitmapwidth,bitmapheight);
	}
	
	void clearInvalid()
	{
		invalidRect.bottom=invalidRect.top=yoffset;
		invalidRect.left=invalidRect.right=xoffset;
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
	 * @see android.androidVNC.AbstractBitmapData#drawRect(int, int, int, int, android.graphics.Paint)
	 */
	@Override
	void drawRect(int x, int y, int w, int h, Paint paint) {
		x-=xoffset;
		y-=yoffset;
		memGraphics.drawRect(x, y, x+w, y+h, paint);
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#offset(int, int)
	 */
	@Override
	int offset(int x, int y) {
		return (y - yoffset) * bitmapwidth + x - xoffset;
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#scrollChanged(int, int)
	 */
	@Override
	synchronized void scrollChanged(int newx, int newy) {
		//android.util.Log.i("LBM","scroll "+newx+" "+newy);
		newx+=(framebufferwidth-origwidth)/2;
		newy+=(framebufferheight-origheight)/2;
		if ( ! ( newx-xoffset>=0 && newx-xoffset+origwidth<=bitmapwidth && newy-yoffset>=0 && newy-yoffset+origheight<=bitmapheight))
		{
			//android.util.Log.i("LBM","scroll "+newx+" "+newy);
			int xindex, yindex;
			xindex = -1;
			yindex = -1;
			if ( newx<xoffset)
			{
				xindex=newx/origwidth-1;
				if ( xindex<0)
					xindex=0;
			}
			else if ( newx-xoffset+origwidth>bitmapwidth)
			{
				xindex=newx/origwidth;
			}
			if ( newy<yoffset)
			{
				yindex=newy/origheight-1;
				if ( yindex<0)
					yindex=0;
			}
			else if ( newy-yoffset+origheight>bitmapheight)
			{
				yindex=newy/origheight;
			}
			if ( xindex != -1)
			{
				scrolledToX=xindex*origwidth;
				if ( scrolledToX+bitmapwidth>framebufferwidth)
					scrolledToX=framebufferwidth-bitmapwidth;
			}
			if ( yindex != -1)
			{
				scrolledToY=yindex*origheight;
				if ( scrolledToY+bitmapheight>framebufferheight)
					scrolledToY=framebufferheight-bitmapheight;
			}
			if ( waitingForInput)
				syncScroll();
		}
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#updateBitmap(int, int, int, int)
	 */
	@Override
	void updateBitmap(int x, int y, int w, int h) {
		mbitmap.setPixels(bitmapPixels, offset(x,y), bitmapwidth, x-xoffset, y-yoffset, w, h);
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#updateView(android.widget.ImageView)
	 */
	@Override
	void updateView(ImageView v) {
		//android.util.Log.i("LBM","Setting drawable");
		v.setImageDrawable(drawable);
		v.invalidate();
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#validDraw(int, int, int, int)
	 */
	@Override
	boolean validDraw(int x, int y, int w, int h) {
		//android.util.Log.i("LBM", "Validate Drawing "+x+" "+y+" "+w+" "+h+" "+xoffset+" "+yoffset+" "+(x-xoffset>=0 && x-xoffset+w<=bitmapwidth && y-yoffset>=0 && y-yoffset+h<=bitmapheight));
		boolean result = x-xoffset>=0 && x-xoffset+w<=bitmapwidth && y-yoffset>=0 && y-yoffset+h<=bitmapheight;
		if ( ! result)
		{
			//android.util.Log.i("LBM", "Invalid "+x+" "+y+" "+w+" "+h+" "+xoffset+" "+yoffset);
			invalidRect.union( x, y, x+w, y+h);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#writeFullUpdateRequest(boolean)
	 */
	@Override
	void writeFullUpdateRequest(boolean incremental) throws IOException {
		rfb.writeFramebufferUpdateRequest(xoffset, yoffset, bitmapwidth, bitmapheight, incremental);
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractBitmapData#syncScroll()
	 */
	@Override
	synchronized void syncScroll() {
		if ( xoffset!=scrolledToX || yoffset!=scrolledToY)
		{
			xoffset=scrolledToX;
			yoffset=scrolledToY;
			bitmapRect.top=scrolledToY;
			bitmapRect.bottom=scrolledToY+bitmapheight;
			bitmapRect.left=scrolledToX;
			bitmapRect.right=scrolledToX+bitmapwidth;
			try
			{
				//android.util.Log.i("LBM","update req "+xoffset+" "+yoffset);
				writeFullUpdateRequest(false);
			}
			catch ( IOException ioe)
			{
				// TODO log this
			}
		}
		else
		{			
			if ( invalidRect.intersect( bitmapRect))
			{
				try
				{
					//android.util.Log.i("LBM","invalid rect "+invalidRect.toString()+" "+bitmapRect.toString());
					rfb.writeFramebufferUpdateRequest(invalidRect.left, invalidRect.top, invalidRect.right-invalidRect.left, invalidRect.bottom-invalidRect.top, false);
				}
				catch ( IOException ioe)
				{
					// TODO log this
				}
			}
			clearInvalid();
		}
		waitingForInput=true;
	}
}
