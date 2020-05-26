package com.easycredit.data;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.easycredit.data.model.EasyCreditUser;
import com.easycredit.data.model.LoggedInUser;

import org.json.JSONObject;

import java.io.IOException;

import static com.easycredit.ui.login.LoginActivity.http;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private static final String BASE_URL = "";

    public Result<LoggedInUser> login(String phone, String password) {
        try {
            Log.i(this.getClass().toString(), "here in login inner");

            LoggedInUser fakeUser =
                    new LoggedInUser(new EasyCreditUser("","","","", null), "email");
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}
