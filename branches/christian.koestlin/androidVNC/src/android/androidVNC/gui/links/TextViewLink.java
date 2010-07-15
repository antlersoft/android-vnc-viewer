package android.androidVNC.gui.links;

import android.androidVNC.Provider.VncSettings;
import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.widget.TextView;

public class TextViewLink implements Link {
    
    private final Activity fActivity;
    private final TextView fView;
    private final Uri fUri;
    private final String fKey;

    public TextViewLink(Activity activity, VncSettings helper, TextView view, Uri uri, String key) {
      fActivity = activity;
      fView = view;
      fUri = uri;
      fKey = key;
      view.setText(helper.getString(key));
    }

    @Override
    public void copyFromGui2Model() {
      ContentValues contentValues = new ContentValues();
      contentValues.put(fKey, fView.getText().toString());
      fActivity.getContentResolver().update(fUri, contentValues, null, null);
    }
  }