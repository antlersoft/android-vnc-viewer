/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package android.androidVNC;

import java.io.IOException;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

/**
 * @author Michael A. MacDonald
 *
 */
class EnterTextDialog extends Dialog {
	
	private VncCanvasActivity _canvasActivity;
	
	private EditText _textEnterText;
	
	private ArrayList<String> _history;
	
	private int _historyIndex;
	
	private ImageButton _buttonNextEntry;
	private ImageButton _buttonPreviousEntry;

	public EnterTextDialog(Context context) {
		super(context);
		setOwnerActivity((Activity)context);
		_canvasActivity = (VncCanvasActivity)context;
		_history = new ArrayList<String>();
		_historyIndex = 0;
	}
	
	private String saveText(boolean wasSent)
	{
		CharSequence cs = _textEnterText.getText();
		if (cs.length()==0)
			return "";
		String s = cs.toString();
		if (wasSent || _historyIndex>=_history.size() || ! s.equals(_history.get(_historyIndex)))
		{
			_history.add(s);
		}
		return s;
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
		_buttonNextEntry = (ImageButton)findViewById(R.id.buttonNextEntry);
		_buttonNextEntry.setOnClickListener(new View.OnClickListener() {

			/* (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				int oldSize = _history.size();
				if (_historyIndex < oldSize)
				{
					saveText(false);
					_historyIndex++;
					if (_history.size()>oldSize && _historyIndex==oldSize)
						_historyIndex++;
					if (_historyIndex < _history.size())
					{
					    _textEnterText.setText(_history.get(_historyIndex));
					}
					else
					{
						_textEnterText.setText("");
					}
				}
				updateButtons();
			}
			
		});
		_buttonPreviousEntry = (ImageButton)findViewById(R.id.buttonPreviousEntry);
		_buttonPreviousEntry.setOnClickListener(new View.OnClickListener() {

			/* (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				if (_historyIndex > 0)
				{
					saveText(false);
					_historyIndex--;
				    _textEnterText.setText(_history.get(_historyIndex));
				}
				updateButtons();
			}
			
		});
		((Button)findViewById(R.id.buttonSendText)).setOnClickListener(new View.OnClickListener() {

			/* (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				RfbProto rfb = _canvasActivity.vncCanvas.rfb;
				String s = saveText(true);
				int l = s.length();
				for (int i = 0; i<l; i++)
				{
					char c = s.charAt(i);
					int meta = 0;
					int keysym = c;
					if (Character.isISOControl(c))
					{
						if (c=='\n')
							keysym = MetaKeyBean.keysByKeyCode.get(KeyEvent.KEYCODE_ENTER).keySym;
						else
							continue;
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
				_textEnterText.setText("");
				_historyIndex = _history.size();
				updateButtons();
				dismiss();
			}
			
		});
		
		updateButtons();
	}

	private void updateButtons()
	{
		_buttonPreviousEntry.setEnabled(_historyIndex > 0);
		_buttonNextEntry.setEnabled(_historyIndex <_history.size());
	}
	
	/* (non-Javadoc)
	 * @see android.app.Dialog#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
	}
}
