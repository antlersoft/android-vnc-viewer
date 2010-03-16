/**
 * 
 */
package android.androidVNC;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author Michael A. MacDonald
 *
 */
class RepeaterDialog extends Dialog {
	private ConnectionBean _connection;
	private EditText _repeaterId;
	private androidVNC _configurationDialog;

	RepeaterDialog(androidVNC context) {
		super(context);
		setOwnerActivity((Activity)context);
		_configurationDialog = context;
	}
	
	void saveRepeaterInfo(String info)
	{
		_configurationDialog.updateRepeaterInfo(info);
	}

	/* (non-Javadoc)
	 * @see android.app.Dialog#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.repeater_dialog_title);
	
		setContentView(R.layout.repeater_dialog);
		_repeaterId=(EditText)findViewById(R.id.textRepeaterInfo);
		((TextView)findViewById(R.id.textRepeaterCaption)).setText(Html.fromHtml(getContext().getString(R.string.repeater_caption)));
		((Button)findViewById(R.id.buttonSaveRepeater)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveRepeaterInfo(_repeaterId.getText().toString());
				dismiss();
			}
		});
		((Button)findViewById(R.id.buttonClearRepeater)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveRepeaterInfo("");
				dismiss();
			}
		});
	}
}
