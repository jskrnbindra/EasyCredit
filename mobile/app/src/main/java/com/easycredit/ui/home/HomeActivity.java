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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                    JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });

        GSON = builder.create();
    }


    private Http http;
    private String userId = "noUser";
    private String sessionId = "noSession";
    private ProgressBar topProgressBar;
    private Button refreshButton;
    private EasyCreditUser user;

    private TextView welcomeText;
    private TextView phoneText;
    private static RefreshButtonListener refreshButtonListener;


    // Listeners

    private View.OnClickListener refreshBtnListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            refreshButton.setEnabled(false);
            topProgressBar.setVisibility(View.VISIBLE);

            refreshButtonListener.refreshButtonClicked();

            try {
                // Just so that it feels like it got refreshed.
                // Otherwise it's too fast and looks like the refresh isn't working.
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            refreshButton.setEnabled(true);
            topProgressBar.setVisibility(View.INVISIBLE);
        }
    };

    private View.OnClickListener sendMoneyListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent sendMoneyIntent = new Intent(getApplicationContext(), SendMoneyActivity.class);
            sendMoneyIntent.putExtra(getString(R.string.user_id_extra), userId);
            sendMoneyIntent.putExtra(getString(R.string.user_phone_extra), user.getPhone());
            sendMoneyIntent.putExtra(getString(R.string.user_name_extra), user.getName());
            sendMoneyIntent.putExtra(getString(R.string.user_email_extra), user.getEmail());
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
        phoneText = findViewById(R.id.phoneTV);
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
                Log.d(this.getClass().toString(), "User fetched -> " +
                        user.getTransactions().get(0).getTimestamp().getTime());
                String displayName = user.getName();
                topProgressBar.setVisibility(View.INVISIBLE);
                welcomeText.setText(displayName);
                phoneText.setText("+91 " + user.getPhone());
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
                topProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(ctx, requestName + " failed!", Toast.LENGTH_LONG).show();
            }
        };
    }



    public interface RefreshButtonListener{
        void refreshButtonClicked();
    }
}
