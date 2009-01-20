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
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

public class androidVNC extends Activity {
	private EditText ipText;
	private EditText portText;
	private EditText passwordText;
	private Button goButton;
	private Spinner colorSpinner;
	private CheckBox checkboxForceFullScreen;
	private Spinner spinnerConnection;
	private MostRecentBean mostRecent;
	private VncDatabase database;
	private ConnectionBean selected;
	private EditText textNickname;

	@Override
	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		setContentView(R.layout.main);

		ipText = (EditText) findViewById(R.id.textIP);
		portText = (EditText) findViewById(R.id.textPORT);
		passwordText = (EditText) findViewById(R.id.textPASSWORD);
		textNickname = (EditText) findViewById(R.id.textNickname);
		goButton = (Button) findViewById(R.id.buttonGO);
		colorSpinner = (Spinner)findViewById(R.id.colorformat);
		COLORMODEL[] models=COLORMODEL.values();
		ArrayAdapter<COLORMODEL> colorSpinnerAdapter = new ArrayAdapter<COLORMODEL>(this, android.R.layout.simple_spinner_item, models);
		checkboxForceFullScreen = (CheckBox)findViewById(R.id.checkboxForceFullScreen);
		colorSpinner.setAdapter(colorSpinnerAdapter);
		colorSpinner.setSelection(0);
		spinnerConnection = (Spinner)findViewById(R.id.spinnerConnection);
		spinnerConnection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> ad, View view, int itemIndex, long id) {
				selected = (ConnectionBean)ad.getSelectedItem();
				updateViewFromSelected();
			}
			@Override
			public void onNothingSelected(AdapterView<?> ad) {
				selected = null;
			}
		});
		spinnerConnection.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			/* (non-Javadoc)
			 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
			 */
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				spinnerConnection.setSelection(arg2);
				selected = (ConnectionBean)spinnerConnection.getItemAtPosition(arg2);
				canvasStart();
				return true;
			}
			
		});

		goButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				canvasStart();
			}
		});
		
		database = new VncDatabase(this);
	}
	
	protected void onDestroy() {
		database.close();
		super.onDestroy();
	}
	
	private void updateViewFromSelected() {
		if (selected==null)
			return;
		ipText.setText(selected.getAddress());
		portText.setText(Integer.toString(selected.getPort()));
		passwordText.setText(selected.getPassword());
		checkboxForceFullScreen.setChecked(selected.getForceFull());
		textNickname.setText(selected.getNickname());
		COLORMODEL cm = COLORMODEL.valueOf(selected.getColorModel());
		COLORMODEL[] colors=COLORMODEL.values();
		for (int i=0; i<colors.length; ++i)
		{
			if (colors[i] == cm) {
				colorSpinner.setSelection(i);
				break;
			}
		}
	}
	
	private void updateSelectedFromView() {
		if (selected==null) {
			return;
		}
		selected.setAddress(ipText.getText().toString());
		try
		{
			selected.setPort(Integer.parseInt(portText.getText().toString()));
		}
		catch (NumberFormatException nfe)
		{
			
		}
		selected.setNickname(textNickname.getText().toString());
		selected.setForceFull(checkboxForceFullScreen.isChecked());
		selected.setPassword(passwordText.getText().toString());
		selected.setColorModel(((COLORMODEL)colorSpinner.getSelectedItem()).nameString());
	}
	
	protected void onStart() {
		super.onStart();
		ArrayList<ConnectionBean> connections=new ArrayList<ConnectionBean>();
		connections.add(new ConnectionBean());
		int connectionIndex=0;
		ConnectionBean.getAll(database.getReadableDatabase(), ConnectionBean.GEN_TABLE_NAME, connections, ConnectionBean.newInstance);
		if ( connections.size()>1)
		{
			ArrayList<MostRecentBean> recents=new ArrayList<MostRecentBean>(1);
			MostRecentBean.getAll(database.getReadableDatabase(),MostRecentBean.GEN_TABLE_NAME,recents,MostRecentBean.GEN_NEW);
			if (recents.size()>0)
			{
				mostRecent=recents.get(0);
				for ( int i=1; i<connections.size(); ++i)
				{
					if (connections.get(i).get_Id() == mostRecent.getConnectionId())
					{
						connectionIndex=i;
						break;
					}
				}
			}
		}
		spinnerConnection.setAdapter(new ArrayAdapter<ConnectionBean>(this,android.R.layout.simple_spinner_item,
				connections.toArray(new ConnectionBean[connections.size()])));
		spinnerConnection.setSelection(connectionIndex,false);
		selected=connections.get(connectionIndex);
		updateViewFromSelected();
	}
	
	protected void onStop() {
		super.onStop();
		if ( selected == null ) {
			return;
		}
		updateSelectedFromView();
		updateInDb();
	}
	
	private void updateInDb()
	{
		SQLiteDatabase db=database.getWritableDatabase();
		if (selected.isNew()) {
			selected.Gen_insert(db);
		} else {
			selected.Gen_update(db);
		}		
	}

	private void canvasStart() {
		if (selected == null) return;
		MemoryInfo info = Utils.getMemoryInfo(this);
		if (info.lowMemory) {
			// Low Memory situation.  Prompt.
			Utils.showYesNoPrompt(this, "Continue?", "Android reports low system memory.\nContinue with VNC connection?", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					vnc();
				}
			}, null);
		} else
			vnc();
	}
	
	private void vnc() {
		updateSelectedFromView();
		updateInDb();
		mostRecent = new MostRecentBean();
		mostRecent.setConnectionId(selected.get_Id());
		SQLiteDatabase db=database.getWritableDatabase();
		db.beginTransaction();
		try
		{
			db.execSQL("DELETE FROM "+MostRecentBean.GEN_TABLE_NAME);
			mostRecent.Gen_insert(db);
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}
		Intent intent = new Intent(this, VncCanvasActivity.class);
		intent.putExtra(VncConstants.CONNECTION,selected.Gen_getValues());
		startActivity(intent);
	}
}
