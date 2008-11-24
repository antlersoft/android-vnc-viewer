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
// androidVNC is the Activity for setting VNC server IP and port.
//

package android.androidVNC;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;

public class androidVNC extends Activity {
    
	private EditText ipText;
	private EditText portText;
	private Button goButton;
	
	public androidVNC() {
    }
	
	@Override
    public void onCreate(Bundle icicle) {

    	super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
                
        ipText = (EditText) findViewById(R.id.textIP);
        portText = (EditText) findViewById(R.id.textPORT);
        goButton = (Button) findViewById(R.id.buttonGO);
        
        goButton.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View view){        		
        		canvasStart();
        	}
        });
        
    }
    
    private void canvasStart(){
    	String ip = ipText.getText().toString();
    	int port = Integer.parseInt(portText.getText().toString());
    	
    	Intent intent = new Intent(androidVNC.this, CanvasView.class);
    	intent.putExtra("IP", ip);
    	intent.putExtra("PORT", port);
    	startActivity(intent);
    	
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    }
    

}