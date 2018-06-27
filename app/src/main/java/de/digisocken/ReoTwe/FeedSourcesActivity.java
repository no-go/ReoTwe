package de.digisocken.ReoTwe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;

public class FeedSourcesActivity extends AppCompatActivity {
    private SharedPreferences _pref;
    private ArrayList<String> _urls;
    private LinearLayout _linearLayout;
    private ArrayList<EditText> _urlEdit;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                storeUrls();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_sources);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(getString(R.string.usersAndTags));
        }
        _pref = App.mPreferences;
        loadUrls();
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            int id = _urlEdit.size();
            EditText editText = new EditText(this);
            editText.setText(data.toString());
            _urlEdit.add(id, editText);

            LinearLayout dummy = new LinearLayout(App.getContextOfApplication());
            dummy.setOrientation(LinearLayout.HORIZONTAL);
            dummy.addView(editText, 0);
            editText.setMinWidth(60);
            _linearLayout.addView(dummy, id);
            storeUrls();
            editText.requestFocus();
        }
    }

    public void addLine(View v) {
        int id = _urlEdit.size();
        _urlEdit.add(id, new EditText(this));

        LinearLayout dummy = new LinearLayout(App.getContextOfApplication());
        dummy.setOrientation(LinearLayout.HORIZONTAL);
        dummy.addView(_urlEdit.get(id), 0);
        _urlEdit.get(id).setMinWidth(60);
        _linearLayout.addView(dummy, id);
    }

    private void loadUrls() {
        _linearLayout = (LinearLayout) findViewById(R.id.feedsourceList);
        _linearLayout.removeAllViews();
        String urls[] = _pref.getString("STARTUSERS", getString(R.string.defaultStarts)).split(",");
        _urlEdit  = new ArrayList<>();

        for (int i=0; i < urls.length + 5; i++) {
            _urlEdit.add(i, new EditText(this));
            if (i < urls.length) {
                _urlEdit.get(i).setText(urls[i]);
            }

            LinearLayout dummy = new LinearLayout(App.getContextOfApplication());
            dummy.setOrientation(LinearLayout.HORIZONTAL);
            dummy.addView(_urlEdit.get(i), 0);
            _urlEdit.get(i).setMinWidth(60);
            _linearLayout.addView(dummy, i);
        }
    }

    private void storeUrls() {
        String newurls = "";
        for (int i=0; i < _urlEdit.size(); i++) {
            String tmp = _urlEdit.get(i).getText().toString().trim().replace(" ", "%20");
            if (tmp != null && !tmp.equals("")) {
                newurls += tmp + ",";
            }
        }

        newurls = newurls.trim();
        newurls = newurls.substring(0, newurls.length() - 1);
        _pref.edit().putString("STARTUSERS", newurls).commit();
    }

    @Override
    public void onBackPressed() {
        storeUrls();
        super.onBackPressed();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loadUrls();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        storeUrls();
        super.onSaveInstanceState(outState);
    }
}
