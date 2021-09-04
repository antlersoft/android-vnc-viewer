/**
 * Copyright (c) 2010 Michael A. MacDonald
 */
package android.androidVNC;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.antlersoft.android.bc.BCFactory;
import com.antlersoft.android.contentxml.SqliteElement;
import com.antlersoft.android.contentxml.SqliteElement.ReplaceStrategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.xml.sax.SAXException;

/**
 * @author Michael A. MacDonald
 *
 */
class ImportExportDialog extends Dialog {

	private androidVNC _configurationDialog;
	private EditText _textLoadUrl;
	private EditText _textSaveUrl;
	
	
	/**
	 * @param context
	 */
	public ImportExportDialog(androidVNC context) {
		super(context);
		setOwnerActivity((Activity)context);
		_configurationDialog = context;
	}

	/* (non-Javadoc)
	 * @see android.app.Dialog#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.importexport);
		setTitle(R.string.import_export_settings);
		_textLoadUrl = (EditText)findViewById(R.id.textImportUrl);
		_textSaveUrl = (EditText)findViewById(R.id.textExportPath);
		
		File f = BCFactory.getInstance().getStorageContext().getExternalStorageDir(_configurationDialog, null);
		// Sdcard not mounted; nothing else to do
		if (f == null)
			return;
		
		f = new File(f, "vnc_settings.xml");
		
		_textSaveUrl.setText(f.getAbsolutePath());
		try {
			_textLoadUrl.setText(f.toURL().toString());
		} catch (MalformedURLException e) {
			// Do nothing; default value not set
		}
		
		Button export = (Button)findViewById(R.id.buttonExport);
		export.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					File f = new File(_textSaveUrl.getText().toString());
					Writer writer = new OutputStreamWriter(new FileOutputStream(f, false));
					SqliteElement.exportDbAsXmlToStream(_configurationDialog.getDatabaseHelper().getReadableDatabase(), writer);
					writer.close();
					dismiss();
				}
				catch (IOException ioe)
				{
					errorNotify("I/O Exception exporting config", ioe);
				} catch (SAXException e) {
					errorNotify("XML Exception exporting config", e);
				}
			}
			
		});
		
		((Button)findViewById(R.id.buttonImport)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try
				{
					URL url = new URL(_textLoadUrl.getText().toString());
					URLConnection connection = url.openConnection();
					connection.connect();
					Reader reader = new InputStreamReader(connection.getInputStream());
					SqliteElement.importXmlStreamToDb(
							_configurationDialog.getDatabaseHelper().getWritableDatabase(),
							reader,
							ReplaceStrategy.REPLACE_EXISTING);
					dismiss();
					_configurationDialog.arriveOnPage();
				}
				catch (MalformedURLException mfe)
				{
					errorNotify("Improper URL given: " + _textLoadUrl.getText(), mfe);
				}
				catch (IOException ioe)
				{
					errorNotify("I/O error reading configuration", ioe);
				}
				catch (SAXException e)
				{
					errorNotify("XML or format error reading configuration", e);
				}
			}
			
		});
	}
	
	private void errorNotify(String msg, Throwable t)
	{
		Log.i("android.androidVNC.ImportExportDialog", msg, t);
		Utils.showErrorMessage(this.getContext(), msg + ":" + t.getMessage());
	}

}
