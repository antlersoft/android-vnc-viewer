//
//  Copyright (C) 2004 Horizon Wimba.  All Rights Reserved.
//  Copyright (C) 2001-2003 HorizonLive.com, Inc.  All Rights Reserved.
//  Copyright (C) 2001,2002 Constantin Kaplinsky.  All Rights Reserved.
//  Copyright (C) 2000 Tridia Corporation.  All Rights Reserved.
//  Copyright (C) 1999 AT&T Laboratories Cambridge.  All Rights Reserved.
//
//  This is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This software is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this software; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
//  USA.
//

//
// VncCanvas is a subclass of android.view.ImageView which draws a VNC
// desktop on it.
//

//
//  Copyright (C) 2004 Horizon Wimba.  All Rights Reserved.
//  Copyright (C) 2001-2003 HorizonLive.com, Inc.  All Rights Reserved.
//  Copyright (C) 2001,2002 Constantin Kaplinsky.  All Rights Reserved.
//  Copyright (C) 2000 Tridia Corporation.  All Rights Reserved.
//  Copyright (C) 1999 AT&T Laboratories Cambridge.  All Rights Reserved.
//
//  This is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This software is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this software; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
//  USA.
//

//
// VncCanvas is a subclass of android.view.SurfaceView which draws a VNC
// desktop on it.
//

package android.androidVNC;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.Inflater;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

public class VncCanvas extends ImageView {
	private final static String TAG = "VncCanvas";

	// User-provided connection settings
	private String server;
	private int port;
	private String password;
	private String repeaterID;

	// Runtime control flags
	private boolean maintainConnection = true;
	private boolean showDesktopInfo = true;
	private boolean repaintsEnabled = true;

	// Color Model settings
	private COLORMODEL pendingColorModel = COLORMODEL.C64;
	private COLORMODEL colorModel = null;
	private int bytesPerPixel = 0;
	private int[] colorPalette = null;

	// VNC protocol connection
	public RfbProto rfb;

	// Internal bitmap data
	private Bitmap mbitmap;
	private int bitmapPixels[];
	private Canvas memGraphics;
	public Handler handler = new Handler();

	// VNC Encoding parameters
	private boolean useCopyRect = false; // TODO CopyRect is not working
	private int preferredEncoding = -1;

	// Unimplemented VNC encoding parameters
	private boolean requestCursorUpdates = false;
	private boolean ignoreCursorUpdates = true;

	// Unimplemented TIGHT encoding parameters
	private int compressLevel = -1;
	private int jpegQuality = -1;

	// Used to determine if encoding update is necessary
	private int[] encodingsSaved = new int[20];
	private int nEncodingsSaved = 0;

	// ZRLE encoder's data.
	private byte[] zrleBuf;
	private int zrleBufLen = 0;
	private int[] zrleTilePixels;
	private ZlibInStream zrleInStream;

	// Zlib encoder's data.
	private byte[] zlibBuf;
	private int zlibBufLen = 0;
	private Inflater zlibInflater;

	public VncCanvas(final Context context, String serverIP, int serverPort, String serverPassword, String repeaterid) {
		super(context);
		this.server = serverIP;
		this.port = serverPort;
		this.password = serverPassword;
		this.repeaterID = repeaterid;

		// Startup the RFB thread with a nifty progess dialog
		final ProgressDialog pd = ProgressDialog.show(context, "Connecting...", "Establishing handshake.\nPlease wait...", true, true, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				closeConnection();
				handler.post(new Runnable() {
					public void run() {
						Utils.showErrorMessage(context, "VNC connection aborted!");
					}
				});
			}
		});
		Thread t = new Thread() {
			public void run() {
				try {
					if (repeaterID != null && !repeaterID.equals("")) {
						// Connect to Repeater Session
						// Passwords are irrelevant.
						connectAndAuthenticate("");
					} else {
						connectAndAuthenticate(password);
					}
					doProtocolInitialisation();
					handler.post(new Runnable() {
						public void run() {
							pd.setMessage("Downloading first frame.\nPlease wait...");
						}
					});
					processNormalProtocol(context, pd);
				} catch (Throwable e) {
					if (maintainConnection) {
						Log.v(TAG, e.toString());
						e.printStackTrace();
						// Ensure we dismiss the progress dialog
						// before we fatal error finish
						if (pd.isShowing())
							pd.dismiss();
						if (e instanceof OutOfMemoryError) {
							// TODO  Not sure if this will happen but...
							// figure out how to gracefully notify the user
							// Instantiating an alert dialog here doesn't work
							// because we are ouf of memory. :(
						} else {
							if (e.getMessage().indexOf("authentication") > -1) {
								handler.post(new Runnable() {
									public void run() {
										Utils.showFatalErrorMessage(context, "VNC authentication failed!");
									}
								});
							} else {
								handler.post(new Runnable() {
									public void run() {
										Utils.showFatalErrorMessage(context, "VNC connection failed!");
									}
								});
							}
						}
					}
				}
			}
		};
		t.start();
	}

	void connectAndAuthenticate(String pw) throws Exception {
		Log.v(TAG, "Connecting to " + server + ", port " + port + "...");

		rfb = new RfbProto(server, port);
		Log.v(TAG, "Connected to server");

		// <RepeaterMagic>
		if (repeaterID != null && !repeaterID.equals("")) {
			Log.v(TAG, "Negotiating repeater/proxy connection");
			byte[] protocolMsg = new byte[12];
			rfb.is.read(protocolMsg);
			byte[] buffer = new byte[250];
			System.arraycopy(repeaterID.getBytes(), 0, buffer, 0, repeaterID.length());
			rfb.os.write(buffer);
		}
		// </RepeaterMagic>

		rfb.readVersionMsg();
		Log.v(TAG, "RFB server supports protocol version " + rfb.serverMajor + "." + rfb.serverMinor);

		rfb.writeVersionMsg();
		Log.v(TAG, "Using RFB protocol version " + rfb.clientMajor + "." + rfb.clientMinor);

		int secType = rfb.negotiateSecurity();
		int authType;
		if (secType == RfbProto.SecTypeTight) {
			rfb.initCapabilities();
			rfb.setupTunneling();
			authType = rfb.negotiateAuthenticationTight();
		} else {
			authType = secType;
		}

		switch (authType) {
		case RfbProto.AuthNone:
			Log.v(TAG, "No authentication needed");
			rfb.authenticateNone();
			break;
		case RfbProto.AuthVNC:
			Log.v(TAG, "VNC authentication needed");
			rfb.authenticateVNC(pw);
			break;
		default:
			throw new Exception("Unknown authentication scheme " + authType);
		}
	}

	void doProtocolInitialisation() throws IOException {
		rfb.writeClientInit();
		rfb.readServerInit();

		Log.v(TAG, "Desktop name is " + rfb.desktopName);
		Log.v(TAG, "Desktop size is " + rfb.framebufferWidth + " x " + rfb.framebufferHeight);

		mbitmap = Bitmap.createBitmap(rfb.framebufferWidth, rfb.framebufferHeight, Bitmap.Config.RGB_565);
		memGraphics = new Canvas(mbitmap);
		bitmapPixels = new int[rfb.framebufferWidth * rfb.framebufferHeight];

		setPixelFormat();
	}

	private void setPixelFormat() throws IOException {
		pendingColorModel.setPixelFormat(rfb);
		bytesPerPixel = pendingColorModel.bpp();
		colorPalette = pendingColorModel.palette();
		colorModel = pendingColorModel;
		pendingColorModel = null;
	}

	public void setColorModel(COLORMODEL cm) {
		// Only update if color model changes
		if (colorModel == null || !colorModel.equals(cm))
			pendingColorModel = cm;
	}

	public boolean isColorModel(COLORMODEL cm) {
		return (colorModel != null) && colorModel.equals(cm);
	}

	public void processNormalProtocol(Context context, ProgressDialog pd) throws Exception {
		try {
			rfb.writeFramebufferUpdateRequest(0, 0, rfb.framebufferWidth, rfb.framebufferHeight, false);

			//
			// main dispatch loop
			//
			while (maintainConnection) {

				// Read message type from the server.
				int msgType = rfb.readServerMessageType();

				// Process the message depending on its type.
				switch (msgType) {
				case RfbProto.FramebufferUpdate:
					rfb.readFramebufferUpdate();
					@SuppressWarnings("unused")
					boolean cursorPosReceived = false;

					for (int i = 0; i < rfb.updateNRects; i++) {
						rfb.readFramebufferUpdateRectHdr();
						int rx = rfb.updateRectX, ry = rfb.updateRectY;
						int rw = rfb.updateRectW, rh = rfb.updateRectH;

						if (rfb.updateRectEncoding == RfbProto.EncodingLastRect) {
							Log.v(TAG, "rfb.EncodingLastRect");
							break;
						}

						if (rfb.updateRectEncoding == RfbProto.EncodingNewFBSize) {
							rfb.setFramebufferSize(rw, rh);
							// - updateFramebufferSize();
							Log.v(TAG, "rfb.EncodingNewFBSize");
							break;
						}

						if (rfb.updateRectEncoding == RfbProto.EncodingXCursor || rfb.updateRectEncoding == RfbProto.EncodingRichCursor) {
							// - handleCursorShapeUpdate(rfb.updateRectEncoding,
							// rx,
							// ry, rw, rh);
							Log.v(TAG, "rfb.EncodingCursor");
							continue;

						}

						if (rfb.updateRectEncoding == RfbProto.EncodingPointerPos) {
							// - softCursorMove(rx, ry);
							cursorPosReceived = true;
							Log.v(TAG, "rfb.EncodingPointerPos");
							continue;
						}

						rfb.startTiming();

						switch (rfb.updateRectEncoding) {
						case RfbProto.EncodingRaw:
							handleRawRect(rx, ry, rw, rh);
							break;
						case RfbProto.EncodingCopyRect:
							handleCopyRect(rx, ry, rw, rh);
							Log.v(TAG, "CopyRect is Buggy!");
							break;
						case RfbProto.EncodingRRE:
							handleRRERect(rx, ry, rw, rh);
							break;
						case RfbProto.EncodingCoRRE:
							handleCoRRERect(rx, ry, rw, rh);
							break;
						case RfbProto.EncodingHextile:
							handleHextileRect(rx, ry, rw, rh);
							break;
						case RfbProto.EncodingZRLE:
							handleZRLERect(rx, ry, rw, rh);
							break;
						case RfbProto.EncodingZlib:
							handleZlibRect(rx, ry, rw, rh);
							break;
						default:
							Log.e(TAG, "Unknown RFB rectangle encoding " + rfb.updateRectEncoding + " (0x" + Integer.toHexString(rfb.updateRectEncoding) + ")");
						}

						rfb.stopTiming();

						// Hide progress dialog
						if (pd.isShowing())
							pd.dismiss();
					}

					boolean fullUpdateNeeded = false;

					if (pendingColorModel != null) {
						setPixelFormat();
						fullUpdateNeeded = true;
					}

					setEncodings(true);
					rfb.writeFramebufferUpdateRequest(0, 0, rfb.framebufferWidth, rfb.framebufferHeight, !fullUpdateNeeded);

					break;

				case RfbProto.SetColourMapEntries:
					throw new Exception("Can't handle SetColourMapEntries message");

				case RfbProto.Bell:
					Utils.notify(context, "VNC Beep!");
					break;

				case RfbProto.ServerCutText:
					String s = rfb.readServerCutText();
					if (s != null && s.length() > 0) {
						// TODO implement cut & paste
					}
					break;

				case RfbProto.TextChat:
					// UltraVNC extension
					String msg = rfb.readTextChatMsg();
					if (msg != null && msg.length() > 0) {
						// TODO implement chat interface
					}
					break;

				default:
					throw new Exception("Unknown RFB message type " + msgType);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Log.v(TAG, "Closing VNC Connection");
			rfb.close();
		}
	}

	public void onDestroy() {
		Log.v(TAG, "Cleaning up resources");
		mbitmap.recycle();
		memGraphics = null;
		bitmapPixels = null;
	}

	void handleRawRect(int x, int y, int w, int h) throws IOException {
		handleRawRect(x, y, w, h, true);
	}

	void handleRawRect(int x, int y, int w, int h, boolean paint) throws IOException {
		int firstoffset = y * rfb.framebufferWidth + x;
		if (bytesPerPixel == 1) {
			// 1 byte per pixel. Use palette lookup table.
			byte[] buf = new byte[w];
			int i, offset;
			for (int dy = y; dy < y + h; dy++) {
				rfb.readFully(buf);

				offset = dy * rfb.framebufferWidth + x;
				for (i = 0; i < w; i++) {
					bitmapPixels[offset + i] = colorPalette[0xFF & buf[i]];
				}
			}
		} else {
			// 4 bytes per pixel (argb) 24-bit color
			byte[] buf = new byte[w * 4];
			int i, offset;
			for (int dy = y; dy < y + h; dy++) {
				rfb.readFully(buf);

				offset = dy * rfb.framebufferWidth + x;
				for (i = 0; i < w; i++) {
					bitmapPixels[offset + i] = // 0xFF << 24 |
					(buf[i * 4 + 2] & 0xff) << 16 | (buf[i * 4 + 1] & 0xff) << 8 | (buf[i * 4] & 0xff);
				}
			}
		}

		mbitmap.setPixels(bitmapPixels, firstoffset, rfb.framebufferWidth, x, y, w, h);

		if (paint)
			reDraw();
	}

	private Runnable reDraw = new Runnable() {
		public void run() {
			if (showDesktopInfo) {
				// Show a Toast with the desktop info on first frame draw.
				showDesktopInfo = false;
				showConnectionInfo();
			}
			setImageBitmap(mbitmap);
		}
	};

	private void reDraw() {
		if (repaintsEnabled)
			handler.post(reDraw);
	}

	public void disableRepaints() {
		repaintsEnabled = false;
	}

	public void enableRepaints() {
		repaintsEnabled = true;
	}

	public void showConnectionInfo() {
		String msg = rfb.desktopName;
		int idx = rfb.desktopName.indexOf("(");
		if (idx > -1) {
			// Breakup actual desktop name from IP addresses for improved
			// readability
			String dn = rfb.desktopName.substring(0, idx).trim();
			String ip = rfb.desktopName.substring(idx).trim();
			msg = dn + "\n" + ip;
		}
		msg += "\n" + rfb.framebufferWidth + "x" + rfb.framebufferHeight;
		String enc = getEncoding();
		// Encoding might not be set when we display this message
		if (enc != null && !enc.equals(""))
			msg += ", " + getEncoding() + " encoding, " + colorModel.toString();
		else
			msg += ", " + colorModel.toString();
		Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
	}

	private String getEncoding() {
		switch (preferredEncoding) {
		case RfbProto.EncodingRaw:
			return "RAW";
		case RfbProto.EncodingTight:
			return "TIGHT";
		case RfbProto.EncodingCoRRE:
			return "CoRRE";
		case RfbProto.EncodingHextile:
			return "HEXTILE";
		case RfbProto.EncodingRRE:
			return "RRE";
		case RfbProto.EncodingZlib:
			return "ZLIB";
		case RfbProto.EncodingZRLE:
			return "ZRLE";
		}
		return "";
	}

	public boolean processPointerEvent(MotionEvent evt) {
		if (rfb != null && rfb.inNormalProtocol) {
			synchronized (rfb) {
				try {
					rfb.writePointerEvent(evt);
				} catch (Exception e) {
					e.printStackTrace();
				}
				rfb.notify();
			}
			return true;
		}
		return false;
	}

	public boolean processLocalKeyEvent(int keyCode, KeyEvent evt) {
		if (keyCode == KeyEvent.KEYCODE_MENU)
			// Ignore menu key
			return true;
		if (rfb != null && rfb.inNormalProtocol) {
			boolean result = false;
			synchronized (rfb) {
				try {
					result = rfb.writeKeyEvent(keyCode, evt);
				} catch (Exception e) {
					e.printStackTrace();
				}
				rfb.notify();
			}
			return result;
		}
		return false;
	}

	public void closeConnection() {
		maintainConnection = false;
	}

	public int getImageWidth() {
		return mbitmap.getWidth();
	}

	public int getImageHeight() {
		return mbitmap.getHeight();
	}

	public int getCenteredXOffset() {
		int xoffset = (mbitmap.getWidth() - getWidth()) / 2;
		return xoffset;
	}

	public int getCenteredYOffset() {
		int yoffset = (mbitmap.getHeight() - getHeight()) / 2;
		return yoffset;
	}

	/**
	 * Additional Encodings
	 * 
	 */

	private void setEncodings(boolean autoSelectOnly) {
		if (rfb == null || !rfb.inNormalProtocol)
			return;

		if (preferredEncoding == -1) {
			// Preferred format is ZRLE
			preferredEncoding = RfbProto.EncodingZRLE;
		} else {
			// Auto encoder selection is not enabled.
			if (autoSelectOnly)
				return;
		}

		int[] encodings = new int[20];
		int nEncodings = 0;

		encodings[nEncodings++] = preferredEncoding;
		if (useCopyRect)
			encodings[nEncodings++] = RfbProto.EncodingCopyRect;
		// if (preferredEncoding != RfbProto.EncodingTight)
		// encodings[nEncodings++] = RfbProto.EncodingTight;
		if (preferredEncoding != RfbProto.EncodingZRLE)
			encodings[nEncodings++] = RfbProto.EncodingZRLE;
		if (preferredEncoding != RfbProto.EncodingHextile)
			encodings[nEncodings++] = RfbProto.EncodingHextile;
		if (preferredEncoding != RfbProto.EncodingZlib)
			encodings[nEncodings++] = RfbProto.EncodingZlib;
		if (preferredEncoding != RfbProto.EncodingCoRRE)
			encodings[nEncodings++] = RfbProto.EncodingCoRRE;
		if (preferredEncoding != RfbProto.EncodingRRE)
			encodings[nEncodings++] = RfbProto.EncodingRRE;

		if (compressLevel >= 0 && compressLevel <= 9)
			encodings[nEncodings++] = RfbProto.EncodingCompressLevel0 + compressLevel;
		if (jpegQuality >= 0 && jpegQuality <= 9)
			encodings[nEncodings++] = RfbProto.EncodingQualityLevel0 + jpegQuality;

		if (requestCursorUpdates) {
			encodings[nEncodings++] = RfbProto.EncodingXCursor;
			encodings[nEncodings++] = RfbProto.EncodingRichCursor;
			if (!ignoreCursorUpdates)
				encodings[nEncodings++] = RfbProto.EncodingPointerPos;
		}

		encodings[nEncodings++] = RfbProto.EncodingLastRect;
		encodings[nEncodings++] = RfbProto.EncodingNewFBSize;

		boolean encodingsWereChanged = false;
		if (nEncodings != nEncodingsSaved) {
			encodingsWereChanged = true;
		} else {
			for (int i = 0; i < nEncodings; i++) {
				if (encodings[i] != encodingsSaved[i]) {
					encodingsWereChanged = true;
					break;
				}
			}
		}

		if (encodingsWereChanged) {
			try {
				rfb.writeSetEncodings(encodings, nEncodings);
			} catch (Exception e) {
				e.printStackTrace();
			}
			encodingsSaved = encodings;
			nEncodingsSaved = nEncodings;
		}
	}

	//
	// Handle a CopyRect rectangle.
	//

	private void handleCopyRect(int x, int y, int w, int h) throws IOException {

		/**
		 * This does not work properly yet.
		 */

		rfb.readCopyRect();
		Paint paint = new Paint();
		// Source Coordinates
		int leftSrc = rfb.copyRectSrcX;
		int topSrc = rfb.copyRectSrcY;
		int rightSrc = topSrc + w;
		int bottomSrc = topSrc + h;

		// Change
		int dx = x - rfb.copyRectSrcX;
		int dy = y - rfb.copyRectSrcY;

		// Destination Coordinates
		int leftDest = leftSrc + dx;
		int topDest = topSrc + dy;
		int rightDest = rightSrc + dx;
		int bottomDest = bottomSrc + dy;

		memGraphics.drawBitmap(mbitmap, new Rect(leftSrc, topSrc, rightSrc, bottomSrc), new Rect(leftDest, topDest, rightDest, bottomDest), paint);

		reDraw();
	}

	//
	// Handle an RRE-encoded rectangle.
	//

	private void handleRRERect(int x, int y, int w, int h) throws IOException {

		int nSubrects = rfb.is.readInt();

		byte[] bg_buf = new byte[bytesPerPixel];
		rfb.readFully(bg_buf);
		int pixel;
		if (bytesPerPixel == 1) {
			pixel = colorPalette[0xFF & bg_buf[0]];
		} else {
			pixel = Color.rgb(bg_buf[2] & 0xFF, bg_buf[1] & 0xFF, bg_buf[0] & 0xFF);
		}
		Paint paint = new Paint();
		paint.setColor(pixel);
		paint.setStyle(Paint.Style.FILL);
		memGraphics.drawRect(x, y, x + w, y + h, paint);

		byte[] buf = new byte[nSubrects * (bytesPerPixel + 8)];
		rfb.readFully(buf);
		DataInputStream ds = new DataInputStream(new ByteArrayInputStream(buf));

		int sx, sy, sw, sh;

		for (int j = 0; j < nSubrects; j++) {
			if (bytesPerPixel == 1) {
				pixel = colorPalette[0xFF & ds.readUnsignedByte()];
			} else {
				ds.skip(4);
				pixel = Color.rgb(buf[j * 12 + 2] & 0xFF, buf[j * 12 + 1] & 0xFF, buf[j * 12] & 0xFF);
			}
			sx = x + ds.readUnsignedShort();
			sy = y + ds.readUnsignedShort();
			sw = ds.readUnsignedShort();
			sh = ds.readUnsignedShort();

			paint.setColor(pixel);
			memGraphics.drawRect(sx, sy, sx + sw, sy + sh, paint);
		}

		reDraw();
	}

	//
	// Handle a CoRRE-encoded rectangle.
	//

	private void handleCoRRERect(int x, int y, int w, int h) throws IOException {
		int nSubrects = rfb.is.readInt();

		byte[] bg_buf = new byte[bytesPerPixel];
		rfb.readFully(bg_buf);
		int pixel;
		if (bytesPerPixel == 1) {
			pixel = colorPalette[0xFF & bg_buf[0]];
		} else {
			pixel = Color.rgb(bg_buf[2] & 0xFF, bg_buf[1] & 0xFF, bg_buf[0] & 0xFF);
		}
		Paint paint = new Paint();
		paint.setColor(pixel);
		paint.setStyle(Paint.Style.FILL);
		memGraphics.drawRect(x, y, x + w, y + h, paint);

		byte[] buf = new byte[nSubrects * (bytesPerPixel + 4)];
		rfb.readFully(buf);

		int sx, sy, sw, sh;
		int i = 0;

		for (int j = 0; j < nSubrects; j++) {
			if (bytesPerPixel == 1) {
				pixel = colorPalette[0xFF & buf[i++]];
			} else {
				pixel = Color.rgb(buf[i + 2] & 0xFF, buf[i + 1] & 0xFF, buf[i] & 0xFF);
				i += 4;
			}
			sx = x + (buf[i++] & 0xFF);
			sy = y + (buf[i++] & 0xFF);
			sw = buf[i++] & 0xFF;
			sh = buf[i++] & 0xFF;

			paint.setColor(pixel);
			memGraphics.drawRect(sx, sy, sx + sw, sy + sh, paint);
		}

		reDraw();
	}

	//
	// Handle a Hextile-encoded rectangle.
	//

	// These colors should be kept between handleHextileSubrect() calls.
	private int hextile_bg, hextile_fg;

	private void handleHextileRect(int x, int y, int w, int h) throws IOException {

		hextile_bg = Color.BLACK;
		hextile_fg = Color.BLACK;

		for (int ty = y; ty < y + h; ty += 16) {
			int th = 16;
			if (y + h - ty < 16)
				th = y + h - ty;

			for (int tx = x; tx < x + w; tx += 16) {
				int tw = 16;
				if (x + w - tx < 16)
					tw = x + w - tx;

				handleHextileSubrect(tx, ty, tw, th);
			}

			// Finished with a row of tiles, now let's show it.
			reDraw();
		}
	}

	//
	// Handle one tile in the Hextile-encoded data.
	//

	private void handleHextileSubrect(int tx, int ty, int tw, int th) throws IOException {

		int subencoding = rfb.is.readUnsignedByte();

		// Is it a raw-encoded sub-rectangle?
		if ((subencoding & RfbProto.HextileRaw) != 0) {
			handleRawRect(tx, ty, tw, th, false);
			return;
		}

		// Read and draw the background if specified.
		byte[] cbuf = new byte[bytesPerPixel];
		if ((subencoding & RfbProto.HextileBackgroundSpecified) != 0) {
			rfb.readFully(cbuf);
			if (bytesPerPixel == 1) {
				hextile_bg = colorPalette[0xFF & cbuf[0]];
			} else {
				hextile_bg = Color.rgb(cbuf[2] & 0xFF, cbuf[1] & 0xFF, cbuf[0] & 0xFF);
			}
		}
		Paint paint = new Paint();
		paint.setColor(hextile_bg);
		paint.setStyle(Paint.Style.FILL);
		memGraphics.drawRect(tx, ty, tx + tw, ty + th, paint);

		// Read the foreground color if specified.
		if ((subencoding & RfbProto.HextileForegroundSpecified) != 0) {
			rfb.readFully(cbuf);
			if (bytesPerPixel == 1) {
				hextile_fg = colorPalette[0xFF & cbuf[0]];
			} else {
				hextile_fg = Color.rgb(cbuf[2] & 0xFF, cbuf[1] & 0xFF, cbuf[0] & 0xFF);
			}
		}

		// Done with this tile if there is no sub-rectangles.
		if ((subencoding & RfbProto.HextileAnySubrects) == 0)
			return;

		int nSubrects = rfb.is.readUnsignedByte();
		int bufsize = nSubrects * 2;
		if ((subencoding & RfbProto.HextileSubrectsColoured) != 0) {
			bufsize += nSubrects * bytesPerPixel;
		}
		byte[] buf = new byte[bufsize];
		rfb.readFully(buf);

		int b1, b2, sx, sy, sw, sh;
		int i = 0;
		if ((subencoding & RfbProto.HextileSubrectsColoured) == 0) {

			// Sub-rectangles are all of the same color.
			paint.setColor(hextile_fg);
			for (int j = 0; j < nSubrects; j++) {
				b1 = buf[i++] & 0xFF;
				b2 = buf[i++] & 0xFF;
				sx = tx + (b1 >> 4);
				sy = ty + (b1 & 0xf);
				sw = (b2 >> 4) + 1;
				sh = (b2 & 0xf) + 1;
				memGraphics.drawRect(sx, sy, sx + sw, sy + sh, paint);
			}
		} else if (bytesPerPixel == 1) {

			// BGR233 (8-bit color) version for colored sub-rectangles.
			for (int j = 0; j < nSubrects; j++) {
				hextile_fg = colorPalette[0xFF & buf[i++]];
				b1 = buf[i++] & 0xFF;
				b2 = buf[i++] & 0xFF;
				sx = tx + (b1 >> 4);
				sy = ty + (b1 & 0xf);
				sw = (b2 >> 4) + 1;
				sh = (b2 & 0xf) + 1;
				paint.setColor(hextile_fg);
				memGraphics.drawRect(sx, sy, sx + sw, sy + sh, paint);
			}

		} else {

			// Full-color (24-bit) version for colored sub-rectangles.
			for (int j = 0; j < nSubrects; j++) {
				hextile_fg = Color.rgb(buf[i + 2] & 0xFF, buf[i + 1] & 0xFF, buf[i] & 0xFF);
				i += 4;
				b1 = buf[i++] & 0xFF;
				b2 = buf[i++] & 0xFF;
				sx = tx + (b1 >> 4);
				sy = ty + (b1 & 0xf);
				sw = (b2 >> 4) + 1;
				sh = (b2 & 0xf) + 1;
				paint.setColor(hextile_fg);
				memGraphics.drawRect(sx, sy, sx + sw, sy + sh, paint);
			}

		}
	}

	//
	// Handle a ZRLE-encoded rectangle.
	//

	private void handleZRLERect(int x, int y, int w, int h) throws Exception {

		if (zrleInStream == null)
			zrleInStream = new ZlibInStream();

		int nBytes = rfb.is.readInt();
		if (nBytes > 64 * 1024 * 1024)
			throw new Exception("ZRLE decoder: illegal compressed data size");

		if (zrleBuf == null || zrleBufLen < nBytes) {
			zrleBufLen = nBytes + 4096;
			zrleBuf = new byte[zrleBufLen];
		}

		rfb.readFully(zrleBuf, 0, nBytes);

		zrleInStream.setUnderlying(new MemInStream(zrleBuf, 0, nBytes), nBytes);

		for (int ty = y; ty < y + h; ty += 64) {

			int th = Math.min(y + h - ty, 64);

			for (int tx = x; tx < x + w; tx += 64) {

				int tw = Math.min(x + w - tx, 64);

				int mode = zrleInStream.readU8();
				boolean rle = (mode & 128) != 0;
				int palSize = mode & 127;
				int[] palette = new int[128];

				readZrlePalette(palette, palSize);

				if (palSize == 1) {
					int pix = palette[0];
					int c = (bytesPerPixel == 1) ? colorPalette[0xFF & pix] : (0xFF000000 | pix);
					Paint paint = new Paint();
					paint.setColor(c);
					paint.setStyle(Paint.Style.FILL);
					memGraphics.drawRect(tx, ty, tx + tw, ty + th, paint);
					continue;
				}

				if (!rle) {
					if (palSize == 0) {
						readZrleRawPixels(tw, th);
					} else {
						readZrlePackedPixels(tw, th, palette, palSize);
					}
				} else {
					if (palSize == 0) {
						readZrlePlainRLEPixels(tw, th);
					} else {
						readZrlePackedRLEPixels(tw, th, palette);
					}
				}
				handleUpdatedZrleTile(tx, ty, tw, th);
			}
		}

		zrleInStream.reset();

		reDraw();
	}

	//
	// Handle a Zlib-encoded rectangle.
	//

	private void handleZlibRect(int x, int y, int w, int h) throws Exception {

		int nBytes = rfb.is.readInt();

		if (zlibBuf == null || zlibBufLen < nBytes) {
			zlibBufLen = nBytes * 2;
			zlibBuf = new byte[zlibBufLen];
		}

		rfb.readFully(zlibBuf, 0, nBytes);

		if (zlibInflater == null) {
			zlibInflater = new Inflater();
		}
		zlibInflater.setInput(zlibBuf, 0, nBytes);

		if (bytesPerPixel == 1) {
			// 1 byte per pixel. Use palette lookup table.
			byte[] buf = new byte[w];
			int i, offset;
			for (int dy = y; dy < y + h; dy++) {
				zlibInflater.inflate(buf);
				offset = dy * rfb.framebufferWidth + x;
				for (i = 0; i < w; i++) {
					bitmapPixels[offset + i] = colorPalette[0xFF & buf[i]];
				}
			}
		} else {
			// 24-bit color (ARGB) 4 bytes per pixel.
			byte[] buf = new byte[w * 4];
			int i, offset;
			for (int dy = y; dy < y + h; dy++) {
				zlibInflater.inflate(buf);
				offset = dy * rfb.framebufferWidth + x;
				for (i = 0; i < w; i++) {
					bitmapPixels[offset + i] = (buf[i * 4 + 2] & 0xFF) << 16 | (buf[i * 4 + 1] & 0xFF) << 8 | (buf[i * 4] & 0xFF);
				}
			}
		}
		int firstoffset = y * rfb.framebufferWidth + x;
		mbitmap.setPixels(bitmapPixels, firstoffset, rfb.framebufferWidth, x, y, w, h);

		reDraw();
	}

	private int readPixel(InStream is) throws Exception {
		int pix;
		if (bytesPerPixel == 1) {
			pix = is.readU8();
		} else {
			int p1 = is.readU8();
			int p2 = is.readU8();
			int p3 = is.readU8();
			pix = (p3 & 0xFF) << 16 | (p2 & 0xFF) << 8 | (p1 & 0xFF);
		}
		return pix;
	}

	private void readPixels(InStream is, int[] dst, int count) throws Exception {
		if (bytesPerPixel == 1) {
			byte[] buf = new byte[count];
			is.readBytes(buf, 0, count);
			for (int i = 0; i < count; i++) {
				dst[i] = (int) buf[i] & 0xFF;
			}
		} else {
			byte[] buf = new byte[count * 3];
			is.readBytes(buf, 0, count * 3);
			for (int i = 0; i < count; i++) {
				dst[i] = ((buf[i * 3 + 2] & 0xFF) << 16 | (buf[i * 3 + 1] & 0xFF) << 8 | (buf[i * 3] & 0xFF));
			}
		}
	}

	private void readZrlePalette(int[] palette, int palSize) throws Exception {
		readPixels(zrleInStream, palette, palSize);
	}

	private void readZrleRawPixels(int tw, int th) throws Exception {
		int len = tw * th;
		if (zrleTilePixels == null || zrleTilePixels.length != len)
			zrleTilePixels = new int[len];
		readPixels(zrleInStream, zrleTilePixels, tw * th); // /
	}

	private void readZrlePackedPixels(int tw, int th, int[] palette, int palSize) throws Exception {

		int bppp = ((palSize > 16) ? 8 : ((palSize > 4) ? 4 : ((palSize > 2) ? 2 : 1)));
		int ptr = 0;
		int len = tw * th;
		if (zrleTilePixels == null || zrleTilePixels.length != len)
			zrleTilePixels = new int[len];

		for (int i = 0; i < th; i++) {
			int eol = ptr + tw;
			int b = 0;
			int nbits = 0;

			while (ptr < eol) {
				if (nbits == 0) {
					b = zrleInStream.readU8();
					nbits = 8;
				}
				nbits -= bppp;
				int index = (b >> nbits) & ((1 << bppp) - 1) & 127;
				if (bytesPerPixel == 1) {
					if (index >= colorPalette.length)
						Log.e(TAG, "zrlePlainRLEPixels palette lookup out of bounds " + index + " (0x" + Integer.toHexString(index) + ")");
					zrleTilePixels[ptr++] = colorPalette[0xFF & palette[index]];
				} else {
					zrleTilePixels[ptr++] = palette[index];
				}
			}
		}
	}

	private void readZrlePlainRLEPixels(int tw, int th) throws Exception {
		int ptr = 0;
		int end = ptr + tw * th;
		if (zrleTilePixels == null || zrleTilePixels.length != end)
			zrleTilePixels = new int[end];
		while (ptr < end) {
			int pix = readPixel(zrleInStream);
			int len = 1;
			int b;
			do {
				b = zrleInStream.readU8();
				len += b;
			} while (b == 255);

			if (!(len <= end - ptr))
				throw new Exception("ZRLE decoder: assertion failed" + " (len <= end-ptr)");

			if (bytesPerPixel == 1) {
				while (len-- > 0)
					zrleTilePixels[ptr++] = colorPalette[0xFF & pix];
			} else {
				while (len-- > 0)
					zrleTilePixels[ptr++] = pix;
			}
		}
	}

	private void readZrlePackedRLEPixels(int tw, int th, int[] palette) throws Exception {

		int ptr = 0;
		int end = ptr + tw * th;
		if (zrleTilePixels == null || zrleTilePixels.length != end)
			zrleTilePixels = new int[end];
		while (ptr < end) {
			int index = zrleInStream.readU8();
			int len = 1;
			if ((index & 128) != 0) {
				int b;
				do {
					b = zrleInStream.readU8();
					len += b;
				} while (b == 255);

				if (!(len <= end - ptr))
					throw new Exception("ZRLE decoder: assertion failed" + " (len <= end - ptr)");
			}

			index &= 127;
			int pix = palette[index];

			if (bytesPerPixel == 1) {
				while (len-- > 0)
					zrleTilePixels[ptr++] = colorPalette[0xFF & pix];
			} else {
				while (len-- > 0)
					zrleTilePixels[ptr++] = pix;
			}
		}
	}

	//
	// Copy pixels from zrleTilePixels8 or zrleTilePixels24, then update.
	//

	private void handleUpdatedZrleTile(int x, int y, int w, int h) {
		int offsetSrc = 0;
		int offsetDst = (y * rfb.framebufferWidth + x);
		for (int j = 0; j < h; j++) {
			System.arraycopy(zrleTilePixels, offsetSrc, bitmapPixels, offsetDst, w);
			offsetSrc += w;
			offsetDst += rfb.framebufferWidth;
		}

		int firstoffset = y * rfb.framebufferWidth + x;
		mbitmap.setPixels(bitmapPixels, firstoffset, rfb.framebufferWidth, x, y, w, h);
	}
}
