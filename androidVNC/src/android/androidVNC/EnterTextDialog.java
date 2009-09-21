/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package android.androidVNC;

import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author Michael A. MacDonald
 *
 */
class EnterTextDialog extends Dialog {
	
	private VncCanvasActivity _canvasActivity;
	
	private EditText _textEnterText;

	public EnterTextDialog(Context context) {
		super(context);
		setOwnerActivity((Activity)context);
		_canvasActivity = (VncCanvasActivity)context;
	}

	/* (non-Javadoc)
	 * @see android.app.Dialog#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entertext);
		setTitle(R.string.enter_text_title);
		_textEnterText = (EditText)findViewById(R.id.textEnterText);
		((Button)findViewById(R.id.buttonSendText)).setOnClickListener(new View.OnClickListener() {

			/* (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				RfbProto rfb = _canvasActivity.vncCanvas.rfb;
				String s = _textEnterText.getText().toString();
				int l = s.length();
				boolean prevControl = false;
				for (int i = 0; i<l; i++)
				{
					char c = s.charAt(i);
					int meta = 0;
					int keysym = c;
					if (Character.isISOControl(c))
					{
						if (prevControl)
							continue;
						prevControl = true;
						keysym = MetaKeyBean.keysByKeyCode.get(KeyEvent.KEYCODE_ENTER).keySym;
					}
					else
					{
						prevControl = false;
						if (Character.isUpperCase(c))
						{
							meta = VncCanvas.SHIFT_MASK;
							keysym = Character.toLowerCase(c);
						}
					}
					try
					{
						rfb.writeKeyEvent(keysym, meta, true);
						rfb.writeKeyEvent(keysym, meta, false);
					}
					catch (IOException ioe)
					{
						// TODO: log this
					}
				}
				dismiss();
			}
			
		});
	}
}
