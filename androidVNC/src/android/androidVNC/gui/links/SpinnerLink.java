package android.androidVNC.gui.links;

import android.androidVNC.Provider.VncSettings;
import android.androidVNC.gui.EditVncSettingsActivity;
import android.content.ContentValues;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class SpinnerLink<T> implements Link {
public SpinnerLink(final EditVncSettingsActivity activity, VncSettings helper, Spinner spinner, final Uri uri, final String key, final BijectiveMapping<Integer, T> bijectiveMapping) {
    selectObject(spinner, bijectiveMapping.to(helper.getInt(key)));
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> adapterView, View view, int index, long arg) {
        int value = bijectiveMapping.from((T)adapterView.getSelectedItem());
        ContentValues contentValues = new ContentValues();
        contentValues.put(key, value);
        activity.getContentResolver().update(uri, contentValues, null, null);
      }
      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });
  }
  
  private void selectObject(Spinner spinner, Object object) {
    for (int i=0; i<spinner.getCount(); ++i) {
      if (spinner.getItemAtPosition(i) == object) {
        spinner.setSelection(i);
        break;
      }
    }
  }

  @Override
  public void copyFromGui2Model() {
  }

}