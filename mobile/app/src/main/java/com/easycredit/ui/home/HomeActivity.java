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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.easycredit.R;
import com.easycredit.ui.send.SendMoneyActivity;
import com.easycredit.data.Http;
import com.easycredit.data.model.EasyCreditUser;
import com.easycredit.ui.login.LoginActivity;
import com.google.gson.Gson;

import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final Gson GSON = new Gson();

    private Http http;
    private String userId = "noUser";
    private String sessionId = "noSession";
    private ProgressBar topProgressBar;
    private Button refreshButton;
    private EasyCreditUser user;

    private TextView welcomeText;
    private static RefreshButtonListener refreshButtonListener;


    // Listeners

    private View.OnClickListener refreshBtnListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            refreshButton.setEnabled(false);
            topProgressBar.setVisibility(View.VISIBLE);

            refreshButtonListener.refreshButtonClicked();

            refreshButton.setEnabled(true);
            topProgressBar.setVisibility(View.GONE);
        }
    };

    private View.OnClickListener sendMoneyListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent sendMoneyIntent = new Intent(getApplicationContext(), SendMoneyActivity.class);
            sendMoneyIntent.putExtra(getString(R.string.user_id_extra), userId);
            startActivity(sendMoneyIntent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public static void setRefreshButtonListener(RefreshButtonListener listener) {
        refreshButtonListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        http = Http.getInstance(this);

        Intent intent = getIntent();
        userId = intent.getStringExtra(getString(R.string.user_id_extra));
        sessionId = intent.getStringExtra(getString(R.string.session_id_extra));
        topProgressBar = findViewById(R.id.progressBar);
        welcomeText = findViewById(R.id.welcome);
        refreshButton = findViewById(R.id.refreshButton);

        populateUserDetails();

        Button sendMoneyButton = findViewById(R.id.sendMoneyButton);
        sendMoneyButton.setOnClickListener(sendMoneyListener);

        refreshButton.setOnClickListener(refreshBtnListener);
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

    private void populateUserDetails()
    {
        topProgressBar.setVisibility(View.VISIBLE);
        String url = String.format("%s/users?code=%s&id=%s",
                getString(R.string.base_url),
                getString(R.string.users_func_key), userId);

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                userFetched(), requestFailed(this, "getUser"));
        http.add(request);
    }

    private void performLogout()
    {
        topProgressBar.setVisibility(View.VISIBLE);
        String url = String.format("%s/logout?code=%s&sessionId=%s",
                getString(R.string.base_url),
                getString(R.string.logout_func_key), sessionId);

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                loggedOut(this), requestFailed(this, "signOut"));
        http.add(request);
    }

    // Callbacks

    private Response.Listener<JSONObject> userFetched()
    {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                user = GSON.fromJson(response.toString(), EasyCreditUser.class);
                Log.d(this.getClass().toString(), "User fetched -> " + user);
                String displayName = user.getName();
                topProgressBar.setVisibility(View.GONE);
                welcomeText.setText(displayName);
            }
        };
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

    private Response.ErrorListener requestFailed(final Context ctx, final String requestName)
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(this.getClass().toString(), requestName + " failed: " + error.getMessage());
                topProgressBar.setVisibility(View.GONE);
                Toast.makeText(ctx, requestName + " failed!", Toast.LENGTH_LONG).show();
            }
        };
    }



    public interface RefreshButtonListener{
        void refreshButtonClicked();
    }
}
