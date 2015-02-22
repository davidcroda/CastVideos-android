package com.google.sample.cast.refplayer.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.google.sample.cast.refplayer.R;
import com.google.sample.cast.refplayer.VideoBrowserActivity;
import com.google.sample.cast.refplayer.browser.VideoProvider;

public class LoginActivity extends ActionBarActivity {

    private SharedPreferences pref;
    private SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        String token = intent.getStringExtra("token");

        pref = getSharedPreferences("AUTH",MODE_PRIVATE);

        if(token != null) {
            prefEditor = pref.edit();

            Log.d("LoginActivity","Got token " + token);

            prefEditor.putString("token", token);
            prefEditor.commit();

            Intent i = new Intent(this, VideoBrowserActivity.class);
            startActivity(i);

        } else {

            String url = VideoProvider.BASE_URL + "#/login?redirect=/oauth2";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
