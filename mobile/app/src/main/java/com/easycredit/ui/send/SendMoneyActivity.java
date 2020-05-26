package com.easycredit.ui.send;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.easycredit.R;
import com.easycredit.data.Http;
import com.easycredit.data.model.EasyCreditUser;
import com.google.gson.Gson;

import org.json.JSONObject;

public class SendMoneyActivity extends AppCompatActivity {

    private static final String TAG = "SendMoneyActivity";
    private static final Gson GSON = new Gson();

    public static Http http;

    private String userId;

    Button verifyButton;
    Button sendButton;
    EditText beneficiaryPhoneEditText;
    EditText amountEditText;
    ProgressBar topProgressBar;
    TextView header;


    // Listeners
    TextWatcher beneficiaryPhoneTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            verifyButton.setEnabled(s.length() == 10);
        }
    };

    View.OnClickListener verifyOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "onClick: verify phone");
            topProgressBar.setVisibility(View.VISIBLE);
            beneficiaryPhoneEditText.setActivated(false);
            verifyButton.setActivated(false);

            String beneficiaryPhone = beneficiaryPhoneEditText.getText().toString();
            String url = String.format("%s/users?code=%s&phone=%s",
                    getString(R.string.base_url),
                    getString(R.string.users_func_key),
                    beneficiaryPhone);
            JsonObjectRequest request = new JsonObjectRequest(url, null,
                    userFetched(), requestFailed(getApplicationContext(),
                    "getUserByPhone"));
            http.add(request);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);

        http = Http.getInstance(this);
        userId = getIntent().getStringExtra(getString(R.string.user_id_extra));


        verifyButton = findViewById(R.id.verifyButton);
        sendButton = findViewById(R.id.sendButton);
        beneficiaryPhoneEditText = findViewById(R.id.beneficiaryPhone);
        amountEditText = findViewById(R.id.amount);
        topProgressBar = findViewById(R.id.sendProgressBar);
        header = findViewById(R.id.header);

        beneficiaryPhoneEditText.addTextChangedListener(beneficiaryPhoneTextWatcher);
        verifyButton.setOnClickListener(verifyOnClickListener);
    }

    private Response.Listener<JSONObject> userFetched()
    {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                EasyCreditUser beneficiary = GSON.fromJson(response.toString(), EasyCreditUser.class);
                Log.d(TAG, "beneficiary fetched -> " + beneficiary);
                header.setText(beneficiary.getName());
                verifyButton.setVisibility(View.GONE);
                beneficiaryPhoneEditText.setVisibility(View.GONE);
                sendButton.setVisibility(View.VISIBLE);
                amountEditText.setVisibility(View.VISIBLE);
                topProgressBar.setVisibility(View.INVISIBLE);
            }
        };
    }

    private Response.ErrorListener requestFailed(final Context ctx, final String requestName)
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, requestName + " failed: " + error.getMessage());
                header.setText("Not found!");
                verifyButton.setActivated(true);
                beneficiaryPhoneEditText.setActivated(true);
                topProgressBar.setVisibility(View.INVISIBLE);
            }
        };
    }

}
