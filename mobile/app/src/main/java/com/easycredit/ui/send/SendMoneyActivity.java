package com.easycredit.ui.send;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
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
    Button confirmButton;
    Button cancelInVerified;
    Button cancelInSend;
    EditText beneficiaryPhoneEditText;
    EditText amountEditText;
    ProgressBar topProgressBar;
    TextView header;
    TextView verificationMessage;
    TextView beneficiaryName;
    TextView beneficiaryPhone;
    TextView beneficiaryNameInSend;
    TextView beneficiaryPhoneInSend;
    ConstraintLayout verifyBox;
    ConstraintLayout verifiedBox;
    ConstraintLayout sendBox;


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
            Log.d(TAG, "onClick: verify phone");
            topProgressBar.setVisibility(View.VISIBLE);
            beneficiaryPhoneEditText.setActivated(false);
            verifyButton.setActivated(false);

            String beneficiaryPhone = beneficiaryPhoneEditText.getText().toString();
            String url = String.format("%s/users?code=%s&phone=%s",
                    getString(R.string.base_url),
                    getString(R.string.users_func_key),
                    beneficiaryPhone);
            JsonObjectRequest request = new JsonObjectRequest(url, null,
                    beneficiaryFetched(), requestFailed(getApplicationContext(),
                    "getUserByPhone"));
            http.add(request);
        }
    };

    View.OnClickListener cancelInVerifiedOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            verifyBox.setVisibility(View.VISIBLE);
            verifiedBox.setVisibility(View.GONE);
            verificationMessage.setVisibility(View.INVISIBLE);
        }
    };

    View.OnClickListener confirmOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            verifiedBox.setVisibility(View.GONE);
            sendBox.setVisibility(View.VISIBLE);
        }
    };

    View.OnClickListener sendOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), R.string.send_dough, Toast.LENGTH_LONG).show();
        }
    };

    View.OnClickListener cancelInSendOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            verifyBox.setVisibility(View.VISIBLE);
            verifiedBox.setVisibility(View.GONE);
            sendBox.setVisibility(View.GONE);
            verificationMessage.setVisibility(View.INVISIBLE);
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
        cancelInVerified = findViewById(R.id.cancelButton);
        cancelInSend = findViewById(R.id.cancelButtonInSendBox);
        beneficiaryPhoneEditText = findViewById(R.id.beneficiaryPhone);
        amountEditText = findViewById(R.id.amount);
        topProgressBar = findViewById(R.id.sendProgressBar);
        header = findViewById(R.id.header);
        verifyBox = findViewById(R.id.verifyBox);
        verifiedBox = findViewById(R.id.verifiedBox);
        sendBox = findViewById(R.id.sendBox);
        verificationMessage = findViewById(R.id.verificationMessage);
        beneficiaryName = findViewById(R.id.beneficiaryName);
        beneficiaryPhone = findViewById(R.id.beneficiaryPhoneTV);
        confirmButton = findViewById(R.id.confirmButton);
        beneficiaryNameInSend = findViewById(R.id.beneficiaryNameInSendBox);
        beneficiaryPhoneInSend = findViewById(R.id.beneficiaryPhoneTVInSendBox);

        beneficiaryPhoneEditText.addTextChangedListener(beneficiaryPhoneTextWatcher);
        verifyButton.setOnClickListener(verifyOnClickListener);
        cancelInVerified.setOnClickListener(cancelInVerifiedOnClickListener);
        confirmButton.setOnClickListener(confirmOnClickListener);
        cancelInSend.setOnClickListener(cancelInSendOnClickListener);
        sendButton.setOnClickListener(sendOnClickListener);
    }

    private Response.Listener<JSONObject> beneficiaryFetched()
    {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                EasyCreditUser beneficiary = GSON.fromJson(response.toString(), EasyCreditUser.class);
                Log.d(TAG, "beneficiary fetched -> " + beneficiary);

                beneficiaryName.setText(beneficiary.getName());
                beneficiaryPhone.setText(beneficiary.getPhone());
                beneficiaryNameInSend.setText(beneficiary.getName());
                beneficiaryPhoneInSend.setText(beneficiary.getPhone());

                verifyBox.setVisibility(View.GONE);
                verifiedBox.setVisibility(View.VISIBLE);
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
                verifyButton.setActivated(true);
                beneficiaryPhoneEditText.setActivated(true);
                verificationMessage.setVisibility(View.VISIBLE);
                topProgressBar.setVisibility(View.INVISIBLE);
            }
        };
    }

}
