package com.easycredit.ui.send;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.easycredit.R;
import com.easycredit.data.Http;
import com.easycredit.data.model.EasyCreditUser;
import com.easycredit.data.model.RzpCreateLinkRequest;
import com.easycredit.data.model.RzpCustomer;
import com.easycredit.data.model.UserTransaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.makeText;
import static java.lang.Integer.parseInt;

public class SendMoneyActivity extends AppCompatActivity {

    private static final String TAG = "SendMoneyActivity";
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


    public static Http http;

    private EasyCreditUser user;
    private EasyCreditUser beneficiary;

    private Button verifyButton;
    private Button sendButton;
    private Button confirmButton;
    private Button cancelInVerified;
    private Button cancelInSend;
    private EditText beneficiaryPhoneEditText;
    private EditText amountEditText;
    private ProgressBar topProgressBar;
    private TextView header;
    private TextView verificationMessage;
    private TextView beneficiaryName;
    private TextView beneficiaryPhone;
    private TextView beneficiaryNameInSend;
    private TextView beneficiaryPhoneInSend;
    private TextView paymentLinkInstruction;
    private TextView paymentRemark;
    private TextView sendingSms;
    private TextView smsSent;
    private ConstraintLayout verifyBox;
    private ConstraintLayout verifiedBox;
    private ConstraintLayout sendBox;
    private ConstraintLayout sendingMoneyBox;


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

    TextWatcher amountTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            sendButton.setEnabled(s.length() > 0);
        }
    };

    View.OnClickListener verifyOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard();
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
                    beneficiaryFetched(), verificationRequestFailed("getUserByPhone"));
            http.add(request);
        }
    };

    View.OnClickListener cancelInVerifiedOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard();
            verifyBox.setVisibility(View.VISIBLE);
            verifiedBox.setVisibility(View.GONE);
            verificationMessage.setVisibility(View.INVISIBLE);
        }
    };

    View.OnClickListener confirmOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard();
            verifiedBox.setVisibility(View.GONE);
            sendBox.setVisibility(View.VISIBLE);
        }
    };

    View.OnClickListener sendOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard();
            topProgressBar.setVisibility(View.VISIBLE);
            sendBox.setVisibility(View.GONE);
            sendingMoneyBox.setVisibility(View.VISIBLE);
            int amountToSend = parseInt(amountEditText.getText().toString());
            String remark = paymentRemark.getText().toString();
            createAndSendPaymentLink(beneficiary, remark, amountToSend);
        }
    };

    View.OnClickListener cancelInSendOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard();
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
        String userId = getIntent().getStringExtra(getString(R.string.user_id_extra));
        String userPhone = getIntent().getStringExtra(getString(R.string.user_phone_extra));
        String userName = getIntent().getStringExtra(getString(R.string.user_name_extra));
        String userEmail = getIntent().getStringExtra(getString(R.string.user_email_extra));

        user = new EasyCreditUser(userId, userEmail, userName, userPhone,
                new ArrayList<UserTransaction>());


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
        sendingSms = findViewById(R.id.sendingSms);
        smsSent = findViewById(R.id.smsSentTV);
        sendingMoneyBox = findViewById(R.id.sendingMoneyBox);
        paymentLinkInstruction = findViewById(R.id.paymentLinkInstruction);
        paymentRemark = findViewById(R.id.paymentRemark);

        beneficiaryPhoneEditText.addTextChangedListener(beneficiaryPhoneTextWatcher);
        verifyButton.setOnClickListener(verifyOnClickListener);
        cancelInVerified.setOnClickListener(cancelInVerifiedOnClickListener);
        confirmButton.setOnClickListener(confirmOnClickListener);
        cancelInSend.setOnClickListener(cancelInSendOnClickListener);
        sendButton.setOnClickListener(sendOnClickListener);
        amountEditText.addTextChangedListener(amountTextWatcher);
    }

    private void createAndSendPaymentLink(EasyCreditUser beneficiary, String description, int amountToSend) {
        amountToSend *= 100;
        String timestamp =  String.valueOf(new Date().getTime()).substring(0, 10);
        String receipt = String.format("%s-%s-%s", user.getPhone(), beneficiary.getPhone(), timestamp);
        String url = String.format("%s/invoices/", getString(R.string.rzp_base_url));

        RzpCreateLinkRequest createLinkRequest = new RzpCreateLinkRequest(amountToSend, description,
                new RzpCustomer(user.getName(), user.getEmail(), user.getPhone()), receipt);
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson = new JSONObject(GSON.toJson(createLinkRequest));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(url, bodyJson,
                paymentLinkSent(), paymentLinkRequestFailed(getApplicationContext(),
                "createAndSendPaymentLink")) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", getString(R.string.rzp_auth_header));
                return headers;
            }
        };
        http.add(request);
    }

    private void startTransaction(EasyCreditUser beneficiary, int amountToSend, String linkId, String receipt) {
        String url = String.format("%s/transactions?code=%s&from_user=%s&to_user=%s&amount=%s&receipt=%s&linkId=%s",
                getString(R.string.base_url),
                getString(R.string.transactions_func_key),
                user.getId(), beneficiary.getId(), amountToSend, receipt, linkId);
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                transactionStarted(), startTransactionRequestFailed(getApplicationContext(),
                "startTransaction"));
        http.add(request);
    }


    // Callbacks

    private Response.Listener<JSONObject> beneficiaryFetched()
    {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                beneficiary = GSON.fromJson(response.toString(), EasyCreditUser.class);
                Log.d(TAG, "beneficiary fetched -> " + beneficiary);

                beneficiaryName.setText(beneficiary.getName());
                beneficiaryPhone.setText(beneficiary.getPhone());
                beneficiaryNameInSend.setText(beneficiary.getName());
                beneficiaryPhoneInSend.setText(beneficiary.getPhone());
                paymentLinkInstruction.setText(paymentLinkInstruction.getText()
                        .toString().replace(getString(R.string.beneficiary_placeholder), beneficiary.getName()));

                verifyBox.setVisibility(View.GONE);
                verifiedBox.setVisibility(View.VISIBLE);
                topProgressBar.setVisibility(View.INVISIBLE);
            }
        };
    }

    private Response.Listener<JSONObject> transactionStarted()
    {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Transaction started -> " + response);
                topProgressBar.setVisibility(View.INVISIBLE);
            }
        };
    }

    private Response.ErrorListener verificationRequestFailed(final String requestName)
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

    private Response.Listener<JSONObject> paymentLinkSent()
    {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "payment link sent -> " + response);
                sendingSms.setVisibility(View.GONE);
                smsSent.setVisibility(View.VISIBLE);
                String linkId = "not-expected";
                String receipt = "not-expected";
                int amountToSend = 0;
                try {
                    linkId = response.getString("id");
                    receipt = response.getString("receipt");
                    amountToSend = response.getInt("amount");
                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: Unexpected response from RazorPay");
                    e.printStackTrace();
                }

                startTransaction(beneficiary, amountToSend/100, linkId, receipt);
            }
        };
    }

    private Response.ErrorListener paymentLinkRequestFailed(final Context ctx, final String requestName)
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, requestName + " failed: " + new String(error.networkResponse.data));
                sendBox.setVisibility(View.VISIBLE);
                sendingMoneyBox.setVisibility(View.GONE);
                makeText(ctx, "Link creation failed!", Toast.LENGTH_LONG).show();
                topProgressBar.setVisibility(View.INVISIBLE);
            }
        };
    }

    private Response.ErrorListener startTransactionRequestFailed(final Context ctx, final String requestName)
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, requestName + " failed: " + new String(error.networkResponse.data));
                sendBox.setVisibility(View.VISIBLE);
                sendingMoneyBox.setVisibility(View.GONE);
                makeText(ctx, "Could not start transaction!", Toast.LENGTH_LONG).show();
                topProgressBar.setVisibility(View.INVISIBLE);
            }
        };
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.d(TAG, "hideKeyboard: already hidden");
        }
    }
}
