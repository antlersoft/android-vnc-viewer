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

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.io.IOException;

public class VncCanvas extends SurfaceView implements SurfaceHolder.Callback{
	SurfaceHolder holder;
	private boolean hasSurface;
	
	private final static String TAG = "AndroidVNC";

	private String server;
	private int port;
	
	private RfbProto rfb;

	private int bytesPerPixel = 4;
	private Bitmap mbitmap;
	private int pixels24[];
	
	
    public Handler handler = new Handler();		
	final Runnable notifyFinished = new Runnable()
	{
		public void run(){

			Canvas canvas = holder.lockCanvas();    			
			if(canvas == null)
				Log.v(TAG, "canvas is null in handler");
			canvas.drawColor(Color.BLACK);
			
			Paint textFormat = new Paint(1);
			textFormat.setColor(Color.LTGRAY);
			canvas.drawText("Connecting. Please wait...", 20, 30, textFormat);
			
			holder.unlockCanvasAndPost(canvas);

			try {        	
	        	connectAndAuthenticate();
	            doProtocolInitialisation();
	    		processNormalProtocol();
	        }
	        catch (Exception e){
	        	Log.v(TAG, e.toString());
	        }
			
		}
	};
	
	
	public VncCanvas(Context context, String serverIP, int serverPort){
		super(context);
		server = serverIP;
		port = serverPort;
		init();
	}
	
	/*
	public VncCanvas(Context context, AttributeSet attrs, Map inflateParams){
		super(context, attrs, inflateParams);
		init();
	}

	public VncCanvas(Context context, AttributeSet attrs, Map inflateParams, int defStyle){
		super(context, attrs, inflateParams, defStyle);
		init();
	}
	*/
	
	void init(){		
		holder = getHolder();
		holder.addCallback(this);
		hasSurface = false;						
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
		;
	}
	
	public void surfaceCreated(SurfaceHolder holder){		    		
		hasSurface = true;
		handler.post(notifyFinished);
    }

	public void surfaceDestroyed(SurfaceHolder holder){
		hasSurface = false;
	}

	
    void connectAndAuthenticate() throws Exception
    {

      // Log.v(TAG, "Initializing...");
    

      Log.v(TAG, "Connecting to " + server + ", port " + port + "...");
      
      rfb = new RfbProto(server, port);
      Log.v(TAG, "Connected to server");
      
      rfb.readVersionMsg();
      Log.v(TAG, "RFB server supports protocol version " + 
    		  rfb.serverMajor + "." + rfb.serverMinor);
      
      rfb.writeVersionMsg();
      Log.v(TAG, "Using RFB protocol version " +
    		  rfb.clientMajor + "." + rfb.clientMinor);
      
      int secType = rfb.negotiateSecurity();
      int authType;
      if (secType == RfbProto.SecTypeTight) {
        //showConnectionStatus("Enabling TightVNC protocol extensions");
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
        /*
    	showConnectionStatus("Performing standard VNC authentication");
        if (passwordParam != null) {
          rfb.authenticateVNC(passwordParam);
        } else {
          String pw = askPassword();
          rfb.authenticateVNC(pw);
        }
        */
    	Log.v(TAG, "VNC authentication needed");
        break;
      default:
        throw new Exception("Unknown authentication scheme " + authType);
      }
    }

    void doProtocolInitialisation() throws IOException
    {
      rfb.writeClientInit();
      rfb.readServerInit();
      
      Log.v(TAG, "Desktop name is " + rfb.desktopName);
      Log.v(TAG, "Desktop size is " + rfb.framebufferWidth + " x " + rfb.framebufferHeight);
      
      mbitmap = Bitmap.createBitmap(rfb.framebufferWidth, rfb.framebufferHeight, Bitmap.Config.RGB_565);
      
      if(bytesPerPixel == 4)
    	  pixels24 = new int [rfb.framebufferWidth * rfb.framebufferHeight];
      
      //- setEncodings();
      // force using true color now
      if(bytesPerPixel == 4)
    	  rfb.writeSetPixelFormat(32, 24, false, true, 255, 255, 255, 16, 8, 0);
      // else if(bytesPerPixel == 2)
      //  rfb.writeSetPixelFormat(16, 16, false, true, 31, 31, 31, 0, 5, 10);

      
      
      //- showConnectionStatus(null);
    }

    //
    // Send current encoding list to the RFB server.
    //
    /*
    int[] encodingsSaved;
    int nEncodingsSaved;

    void setEncodings()        { setEncodings(false); }
    void autoSelectEncodings() { setEncodings(true); }

    void setEncodings(boolean autoSelectOnly) {
      if (options == null || rfb == null || !rfb.inNormalProtocol)
        return;

      int preferredEncoding = options.preferredEncoding;
      if (preferredEncoding == -1) {
        long kbitsPerSecond = rfb.kbitsPerSecond();
        if (nEncodingsSaved < 1) {
          // Choose Tight or ZRLE encoding for the very first update.
          System.out.println("Using Tight/ZRLE encodings");
          preferredEncoding = RfbProto.EncodingTight;
        } else if (kbitsPerSecond > 2000 &&
                   encodingsSaved[0] != RfbProto.EncodingHextile) {
          // Switch to Hextile if the connection speed is above 2Mbps.
          System.out.println("Throughput " + kbitsPerSecond +
                             " kbit/s - changing to Hextile encoding");
          preferredEncoding = RfbProto.EncodingHextile;
        } else if (kbitsPerSecond < 1000 &&
                   encodingsSaved[0] != RfbProto.EncodingTight) {
          // Switch to Tight/ZRLE if the connection speed is below 1Mbps.
          System.out.println("Throughput " + kbitsPerSecond +
                             " kbit/s - changing to Tight/ZRLE encodings");
          preferredEncoding = RfbProto.EncodingTight;
        } else {
          // Don't change the encoder.
          if (autoSelectOnly)
            return;
          preferredEncoding = encodingsSaved[0];
        }
      } else {
        // Auto encoder selection is not enabled.
        if (autoSelectOnly)
          return;
      }

      int[] encodings = new int[20];
      int nEncodings = 0;

      encodings[nEncodings++] = preferredEncoding;
      if (options.useCopyRect) {
        encodings[nEncodings++] = RfbProto.EncodingCopyRect;
      }

      if (preferredEncoding != RfbProto.EncodingTight) {
        encodings[nEncodings++] = RfbProto.EncodingTight;
      }
      if (preferredEncoding != RfbProto.EncodingZRLE) {
        encodings[nEncodings++] = RfbProto.EncodingZRLE;
      }
      if (preferredEncoding != RfbProto.EncodingHextile) {
        encodings[nEncodings++] = RfbProto.EncodingHextile;
      }
      if (preferredEncoding != RfbProto.EncodingZlib) {
        encodings[nEncodings++] = RfbProto.EncodingZlib;
      }
      if (preferredEncoding != RfbProto.EncodingCoRRE) {
        encodings[nEncodings++] = RfbProto.EncodingCoRRE;
      }
      if (preferredEncoding != RfbProto.EncodingRRE) {
        encodings[nEncodings++] = RfbProto.EncodingRRE;
      }

      if (options.compressLevel >= 0 && options.compressLevel <= 9) {
        encodings[nEncodings++] =
          RfbProto.EncodingCompressLevel0 + options.compressLevel;
      }
      if (options.jpegQuality >= 0 && options.jpegQuality <= 9) {
        encodings[nEncodings++] =
          RfbProto.EncodingQualityLevel0 + options.jpegQuality;
      }

      if (options.requestCursorUpdates) {
        encodings[nEncodings++] = RfbProto.EncodingXCursor;
        encodings[nEncodings++] = RfbProto.EncodingRichCursor;
        if (!options.ignoreCursorUpdates)
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
          if (vc != null) {
            vc.softCursorFree();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        encodingsSaved = encodings;
        nEncodingsSaved = nEncodings;
      }
    }
	*/
  
    public void processNormalProtocol() throws Exception {

        // Start/stop session recording if necessary.
        //- viewer.checkRecordingStatus();

        rfb.writeFramebufferUpdateRequest(0, 0, rfb.framebufferWidth,
    				      rfb.framebufferHeight, false);

        //
        // main dispatch loop
        //

        while (true) {

          // Read message type from the server.
          int msgType = rfb.readServerMessageType();
          
          // Process the message depending on its type.
          switch (msgType) {
          	case RfbProto.FramebufferUpdate:
        	  // Log.v(TAG, "FrameBuffer Update...");
          	  rfb.readFramebufferUpdate();
        	  //boolean cursorPosReceived = false;

        	  for (int i = 0; i < rfb.updateNRects; i++) {
        		  rfb.readFramebufferUpdateRectHdr();
        		  // Log.v(TAG, "readFramebufferUpdateRectHdr");
        		  int rx = rfb.updateRectX, ry = rfb.updateRectY;
        		  int rw = rfb.updateRectW, rh = rfb.updateRectH;

        		  if (rfb.updateRectEncoding == RfbProto.EncodingLastRect){
        			  Log.v(TAG, "rfb.EncodingLastRect");
        			  break;
        		  }
        			  
        		  if (rfb.updateRectEncoding == RfbProto.EncodingNewFBSize) {
        			  rfb.setFramebufferSize(rw, rh);
        			  	//- updateFramebufferSize();
        			  Log.v(TAG, "rfb.EncodingNewFBSize");
        			  break;
        		  }

        		  if (rfb.updateRectEncoding == RfbProto.EncodingXCursor ||
        				  rfb.updateRectEncoding == RfbProto.EncodingRichCursor) {
        			  //- handleCursorShapeUpdate(rfb.updateRectEncoding, rx, ry, rw, rh);
        			  Log.v(TAG, "rfb.EncodingCursor");
        			  continue;
        			  
        		  }

        		  if (rfb.updateRectEncoding == RfbProto.EncodingPointerPos) {
        			  //- softCursorMove(rx, ry);
        			  //cursorPosReceived = true;
        			  Log.v(TAG, "rfb.EncodingPointerPos");
        			  continue;
        		  }

        		  rfb.startTiming();

        		  switch (rfb.updateRectEncoding) {
        		  	case RfbProto.EncodingRaw:
        		  		handleRawRect(rx, ry, rw, rh);
        		  		// Log.v(TAG, "EncodingRaw");
        		  		break;
        		  	case RfbProto.EncodingCopyRect:
        		  		//- handleCopyRect(rx, ry, rw, rh);
        		  		break;
        		  	case RfbProto.EncodingRRE:
        		  		//- handleRRERect(rx, ry, rw, rh);
        		  		break;
        		  	case RfbProto.EncodingCoRRE:
        		  		//- handleCoRRERect(rx, ry, rw, rh);
        		  		break;
        		  	case RfbProto.EncodingHextile:
        		  		//- handleHextileRect(rx, ry, rw, rh);
        		  		Log.v(TAG, "EncodingHextile");
        		  		break;
        		  	case RfbProto.EncodingZRLE:
        		  		//- handleZRLERect(rx, ry, rw, rh);
        		  		Log.v(TAG, "EncodingZRLE");
        		  		break;
        		  	case RfbProto.EncodingZlib:
        		  		//- handleZlibRect(rx, ry, rw, rh);
        		  		Log.v(TAG, "EncodingZlib");
        		  		break;
        		  	case RfbProto.EncodingTight:
        		  		//- handleTightRect(rx, ry, rw, rh);
        		  		Log.v(TAG, "EncodingTight");
        		  		break;
        		  	default:
        		  		// Log.v(TAG, "Unknown RFB encoding");
        		  		throw new Exception("Unknown RFB rectangle encoding " +
        		  				rfb.updateRectEncoding);
        		  }

        		  rfb.stopTiming();
        	  }

        	  boolean fullUpdateNeeded = false;


        	  // Defer framebuffer update request if necessary. But wake up
        	  // immediately on keyboard or mouse event. Also, don't sleep
        	  // if there is some data to receive, or if the last update
        	  // included a PointerPos message.
	    	/*
	    	if (viewer.deferUpdateRequests > 0 &&
	    	    rfb.is.available() == 0 && !cursorPosReceived) {
	    	  synchronized(rfb) {
	    	    try {
	    	      rfb.wait(viewer.deferUpdateRequests);
	    	    } catch (InterruptedException e) {
	    	    }
	    	  }
	    	}
			*/
	    	// Before requesting framebuffer update, check if the pixel
	    	// format should be changed. If it should, request full update
	    	// instead of an incremental one.
	    	/*
	    	if (viewer.options.eightBitColors != (bytesPixel == 1)) {
	    	  setPixelFormat();
	    	  fullUpdateNeeded = true;
	    	}
	
	            viewer.autoSelectEncodings();
			*/
        	  rfb.writeFramebufferUpdateRequest(0, 0, rfb.framebufferWidth,
        			  		rfb.framebufferHeight,
        			  		!fullUpdateNeeded);

        	  break;

          case RfbProto.SetColourMapEntries:        	  
        	  throw new Exception("Can't handle SetColourMapEntries message");

          case RfbProto.Bell:
            //- Toolkit.getDefaultToolkit().beep();
        	  Log.v(TAG, "Bell");
        	  break;

          case RfbProto.ServerCutText:
        	  Log.v(TAG, "ServerCutText");
        	  //String s = rfb.readServerCutText();
        	  //- viewer.clipboard.setCutText(s);
        	  break;

          default:
        	  throw new Exception("Unknown RFB message type " + msgType);
          }
        }
      }

    
    void handleRawRect(int x, int y, int w, int h) throws IOException {
    	handleRawRect(x, y, w, h, true);
    }

    void handleRawRect(int x, int y, int w, int h, boolean paint) throws IOException {
        // Log.v(TAG, "handleRawRect");
                
        byte[] buf = new byte[w * 4];
        int i, offset;
    	int firstoffset = y * rfb.framebufferWidth + x;
    	
    	    	
    	if(bytesPerPixel == 4){
        	for (int dy = y; dy < y + h; dy++) { 

        		rfb.readFully(buf);
     	        	
	        	offset = dy * rfb.framebufferWidth + x;	        		        		        	
	        	for(i =0; i<w;i++){	       			        		
	        			        		
	        		pixels24[offset + i] = // 0xFF << 24 |
	        							(buf[i * 4 + 2] & 0xff) << 16 |
	        							(buf[i * 4 + 1] & 0xff) << 8 |
	        							(buf[i * 4] & 0xff); 		        								        		
	        	}	        		        	
	        }
        	//mbitmap.setPixels(pixels24, 0, rfb.framebufferWidth, 0, 0, rfb.framebufferWidth, rfb.framebufferHeight);
    	}
    	
    	mbitmap.setPixels(pixels24, firstoffset, rfb.framebufferWidth, x, y, w, h);
    	
    	reDraw();
    	
    	// Log.v(TAG, "handleRawRect finished");
    }
	
    private void reDraw(){
    	
    	if(!hasSurface)
    		Log.v (TAG, "surface has not created");
    	
    	Canvas canvas = holder.lockCanvas();
    	canvas.drawBitmap(mbitmap, 0.0f, 0.0f, null);
    	holder.unlockCanvasAndPost(canvas);
    
    }
    
}
