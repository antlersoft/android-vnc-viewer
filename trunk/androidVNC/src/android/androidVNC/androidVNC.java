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
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class androidVNC extends Activity {
	private EditText ipText;
	private EditText portText;
	private EditText passwordText;
	private Button goButton;

	@Override
	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		setContentView(R.layout.main);

		ipText = (EditText) findViewById(R.id.textIP);
		portText = (EditText) findViewById(R.id.textPORT);
		passwordText = (EditText) findViewById(R.id.textPASSWORD);
		goButton = (Button) findViewById(R.id.buttonGO);

		goButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				canvasStart();
			}
		});
	}

	private void canvasStart() {
		String ip = ipText.getText().toString();
		int port = Integer.parseInt(portText.getText().toString());
		String password = passwordText.getText().toString();
		vnc(this, ip, port, password, null);
	}
	
	
	private void vnc(final Context _context, final String host, final int port, final String password, final String repeaterID) {
		MemoryInfo info = Utils.getMemoryInfo(_context);
		if (info.lowMemory) {
			// Low Memory situation.  Prompt.
			Utils.showYesNoPrompt(_context, "Continue?", "Android reports low system memory.\nContinue with VNC connection?", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					vnc_(_context, host, port, password, repeaterID);
				}
			}, null);
		} else
			vnc_(_context, host, port, password, repeaterID);
	}
		
	private void vnc_(Context _context, String host, int port, String password, String repeaterID) {
		 Intent intent = new Intent(_context, VncCanvasActivity.class);
		 intent.putExtra("HOST", host);
		 intent.putExtra("PORT", port);
		 intent.putExtra("PASSWORD", password);
		 intent.putExtra("ID", repeaterID);
		 _context.startActivity(intent);
	}
}