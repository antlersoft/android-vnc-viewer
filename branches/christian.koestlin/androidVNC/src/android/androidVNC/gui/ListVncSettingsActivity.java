package android.androidVNC.gui;

import android.androidVNC.Provider;
import android.androidVNC.R;
import android.androidVNC.Provider.VncSettings;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ListVncSettingsActivity extends ListActivity {

  private static final int MENU_ITEM_NEW = Menu.FIRST;

  private static final int MENU_ITEM_EDIT = MENU_ITEM_NEW+1;

  private static final int MENU_ITEM_DELETE = MENU_ITEM_EDIT+1;

  private static final int INSERT_NEW = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getListView().setOnCreateContextMenuListener(this);

    Intent intent = getIntent();
    if (intent.getData() == null) {
      intent.setData(Provider.CONTENT_URI);
    }
    Cursor cursor = managedQuery(getIntent().getData(), null, null, null, null);
    SimpleCursorAdapter adapter = 
      new SimpleCursorAdapter(this, 
          R.layout.vnc_setting_row, 
          cursor, 
          new String[] { Provider.VncSettings.HOST, Provider.VncSettings.PORT }, 
          new int[] { R.id.vnc_setting_host, R.id.vnc_setting_port });
    setListAdapter(adapter);
  }
  
  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    view(ContentUris.withAppendedId(Provider.CONTENT_URI, id));
  }
  
  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
    menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(VncSettings.HOST)));
    menu.add(0, MENU_ITEM_EDIT, 0, R.string.menu_edit);
    menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_delete);
  }
  
  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

    Uri uri = ContentUris.withAppendedId(Provider.CONTENT_URI, info.id);
    switch (item.getItemId()) {
      case MENU_ITEM_NEW:
        makeNew();
        return true;
      case MENU_ITEM_EDIT:
        edit(uri);
        return true;
      case MENU_ITEM_DELETE:
        delete(uri);
        return true;
    }
    return false;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    boolean res = super.onCreateOptionsMenu(menu);
    if (res) {
      menu.add(0, MENU_ITEM_NEW, 0, getString(R.string.menu_new)).setIcon(android.R.drawable.ic_menu_add);
    }
    return res;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    switch (item.getItemId()) {
      case MENU_ITEM_NEW:
        makeNew();
        return true;
    }
    return super.onMenuItemSelected(featureId, item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case INSERT_NEW:
        edit(data.getData());
        break;
    }
  }

  private void makeNew() {
    startActivityForResult(new Intent(Intent.ACTION_INSERT, Provider.CONTENT_URI), INSERT_NEW);
  }

  private void view(Uri uri) {
    startActivity(new Intent(Intent.ACTION_VIEW, uri));
  }
  private void edit(Uri uri) {
    startActivity(new Intent(Intent.ACTION_EDIT, uri));
  }

  private void delete(Uri uri) {
    startActivity(new Intent(Intent.ACTION_DELETE, uri));
  }

}
