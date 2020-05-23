package com.easycredit.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.easycredit.R;
import com.easycredit.data.Http;
import com.easycredit.ui.home.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    public static Http http;

    private TextView msgTextView;
    private ProgressBar loadingProgressBar;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        http = Http.getInstance(this);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        msgTextView = findViewById(R.id.message);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    performLogin();
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }});
    }

    private void performLogin() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        loginButton.setActivated(false);
        msgTextView.setVisibility(View.GONE);
        Log.i(this.getClass().toString(), "here in login");
        String phone = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        String url = String.format("%s/login?phone=%s&password=%s&code=%s",
                getString(R.string.base_url),
                phone, password,
                getString(R.string.login_func_key));

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                loginDone(this), loginFailed());
        http.add(request);
    }

    private Response.Listener<JSONObject> loginDone(final Context ctx) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(this.getClass().toString(), "login response ->" + response.toString());

                String userId = "error";
                String phone = "error";
                String sessionId = "error";
                try {
                    userId = response.getString("id");
                    phone = response.getString("phone");
                    sessionId = response.getString("sessionId");
                } catch (JSONException e) {
                    Log.e(this.getClass().toString(), "failed to get from JSON");
                    msgTextView.setText("Failed to get from JSON");
                    msgTextView.setText("Unexpected error");
                    msgTextView.setTextColor(0xffff0000);
                    loadingProgressBar.setVisibility(View.GONE);
                    loginButton.setActivated(true);
                    msgTextView.setVisibility(View.VISIBLE);
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                loginButton.setActivated(true);

                Intent showHomeScreen = new Intent(ctx, HomeActivity.class);
                showHomeScreen.putExtra(getString(R.string.user_id_extra), userId);
                showHomeScreen.putExtra(getString(R.string.session_id_extra), sessionId);
                startActivity(showHomeScreen);
            }
        };
    }

    private Response.ErrorListener loginFailed() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(this.getClass().toString(), "login failed " + error.getMessage());

                msgTextView.setText(getString(R.string.login_failed));
                msgTextView.setTextColor(0xffff0000);
                loadingProgressBar.setVisibility(View.GONE);
                loginButton.setActivated(true);
                msgTextView.setVisibility(View.VISIBLE);
            }
        };
    }
}
