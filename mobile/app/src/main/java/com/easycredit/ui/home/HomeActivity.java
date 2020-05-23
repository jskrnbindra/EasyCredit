package com.easycredit.ui.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.easycredit.R;
import com.easycredit.data.Http;
import com.easycredit.ui.login.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    private Http http;
    private String userId = "noUser";
    private String sessionId = "noSession";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        http = Http.getInstance(this);

        Intent intent = getIntent();
        userId = intent.getStringExtra(getString(R.string.user_id_extra));
        sessionId = intent.getStringExtra(getString(R.string.session_id_extra));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            Log.i(this.getClass().toString(), "logging out");
            performLogout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void performLogout()
    {
        String url = String.format("%s/logout?code=%s&sessionId=%s",
                getString(R.string.base_url),
                getString(R.string.logout_func_key), sessionId);

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                loggedOut(this), logOutFailed(this));
        http.add(request);
    }

    private Response.Listener<JSONObject> loggedOut(final Context ctx)
    {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(this.getClass().toString(), "logged out: " + sessionId);
                Intent showLoginScreen = new Intent(ctx, LoginActivity.class);
                showLoginScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(showLoginScreen);
                Toast.makeText(ctx, "Signed out", Toast.LENGTH_SHORT).show();
                finish();
            }
        };
    }

    private Response.ErrorListener logOutFailed(final Context ctx)
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(this.getClass().toString(), "login failed " + error.getMessage());
                Toast.makeText(ctx, "Sign out failed!", Toast.LENGTH_LONG).show();
            }
        };
    }
}
