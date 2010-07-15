package android.androidVNC.gui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

public class DeleteVncSettingsActivity extends Activity {
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Uri uri = getIntent().getData();
    getContentResolver().delete(uri, null, null);
    finish();
  }

}
