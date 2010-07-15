package android.androidVNC.gui;

import android.androidVNC.Provider;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class InsertVncSettingsActivity extends Activity {
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ContentValues values = new ContentValues();
    Uri uri = getContentResolver().insert(Provider.CONTENT_URI, values);
    if (uri == null) {
      setResult(RESULT_CANCELED);
      finish();
      return;
    } 
    setResult(RESULT_OK, new Intent().setData(uri));
    finish();
  }

}
