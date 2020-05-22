package com.easycredit.data;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton for HTTP request queue.
 */
public class Http {

    private RequestQueue queue;
    private static Http instance;

    private Http(Context ctx) {
        queue = Volley.newRequestQueue(ctx.getApplicationContext());
    }

    public static synchronized Http getInstance(Context ctx)
    {
        if (instance == null)
        {
            instance = new Http(ctx);
        }
        return instance;
    }

    public void add(Request request)
    {
        queue.add(request);
    }
}
