package android.androidVNC.gui;

import android.androidVNC.COLORMODEL;
import android.androidVNC.R;
import android.androidVNC.Provider.VncSettings;
import android.androidVNC.gui.links.Colormodel2IntegerMapping;
import android.androidVNC.gui.links.SpinnerLink;
import android.androidVNC.gui.links.TextViewLink;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class EditVncSettingsActivity extends Activity {
  private TextView fHost;
  private TextView fPort;
  private TextView fPassword;
  private Spinner fColorformat;
  
  View2UriLinker fLinker = new View2UriLinker();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.edit_setting);
    
    Uri uri = getIntent().getData();
    VncSettings helper = VncSettings.getHelper(this, uri);
    fHost = (TextView)findViewById(R.id.edit_ip);
    fLinker.add(new TextViewLink(this, helper, fHost, uri, VncSettings.HOST));
    
    fPort = (TextView)findViewById(R.id.edit_port);
    fLinker.add(new TextViewLink(this, helper, fPort, uri, VncSettings.PORT));
    
    fPassword = (TextView)findViewById(R.id.edit_password);
    fLinker.add(new TextViewLink(this, helper, fPassword, uri, VncSettings.PASSWORD));
    
    fColorformat = (Spinner)findViewById(R.id.edit_colorformat);
    ArrayAdapter<COLORMODEL> colorSpinnerAdapter = new ArrayAdapter<COLORMODEL>(this, android.R.layout.simple_spinner_item, COLORMODEL.values());
    fColorformat.setAdapter(colorSpinnerAdapter);
    fLinker.add(new SpinnerLink<COLORMODEL>(this, helper, fColorformat, uri, VncSettings.COLORMODEL, new Colormodel2IntegerMapping()));
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    
    fLinker.copyFromGui2Model();
  }

}
