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

import android.os.Bundle;
import android.view.Window;
import android.app.Activity;
import android.util.Log;

public class CanvasView extends Activity {

	private VncCanvas vnccanvas;
	
    @Override
    public void onCreate(Bundle icicle) {

    	super.onCreate(icicle);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
    	Bundle extras = getIntent().getExtras();    	
    	String ip = extras.getString("IP");
    	int port = extras.getInt("PORT");
    	
        vnccanvas = new VncCanvas(this, ip, port);
        setContentView(vnccanvas);
        	
    }
	
}
