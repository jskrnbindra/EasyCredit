package com.easycredit.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.easycredit.R;
import com.easycredit.data.Http;
import com.easycredit.data.model.EasyCreditUser;
import com.easycredit.data.model.UserTransaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class TransactionsFragment extends Fragment implements HomeActivity.RefreshButtonListener {

    private static final String TAG = "TransactionsFragment";
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

    private Context ctx;
    private Http http;
    private String userId;
    private List<UserTransaction> transactions = new ArrayList<>();
    private TransactionRecyclerViewAdapter transactionAdapter;

    public TransactionsFragment() {
        HomeActivity.setRefreshButtonListener(this);
    }

    public static TransactionsFragment newInstance() {
        TransactionsFragment fragment = new TransactionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_list, container, false);
        ctx = view.getContext();
        Intent intent = ((Activity) ctx).getIntent();
        userId = intent.getStringExtra(getString(R.string.user_id_extra));
        http = Http.getInstance(ctx);

        populateTransactions();
        transactionAdapter = new TransactionRecyclerViewAdapter(ctx, transactions);

        if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
            recyclerView.setAdapter(transactionAdapter);
        }
        return view;
    }

    private void populateTransactions()
    {
        String url = String.format("%s/users?code=%s&id=%s",
                getString(R.string.base_url),
                getString(R.string.users_func_key), userId);
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                userFetched(), requestFailed("getUser"));
        http.add(request);
    }


    // Callbacks

    private Response.Listener<JSONObject> userFetched()
    {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                transactions.clear();
                Log.d(this.getClass().toString(), "User fetched -> " + userId);
                EasyCreditUser user = GSON.fromJson(response.toString(), EasyCreditUser.class);
                transactions.addAll(user.getTransactions() == null ?
                        new ArrayList<UserTransaction>() : user.getTransactions());

                transactionAdapter.notifyDataSetChanged();
            }
        };
    }

    private Response.ErrorListener requestFailed(final String requestName)
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(this.getClass().toString(), requestName + " failed: " + error.getMessage());
                Toast.makeText(ctx, requestName + " failed!", Toast.LENGTH_LONG).show();
            }
        };
    }

    @Override
    public void refreshButtonClicked() {
        populateTransactions();
    }
}
