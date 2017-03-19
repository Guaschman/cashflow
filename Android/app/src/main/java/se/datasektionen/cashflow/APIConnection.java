package se.datasektionen.cashflow;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by viklu on 2017-03-17.
 */

public class APIConnection {

    private static RequestQueue requestQueue;
    private static CookieManager cookieManager;

    static void setUp(Context context) {
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);

        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        requestQueue = Volley.newRequestQueue(context);
    }

    static void makeGetRequest(String url, final Response.Listener callback) {
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                callback, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onResponse(error.toString());
            }
        });
        requestQueue.add(stringRequest);
    }

    static void makePostRequest(String url, Response.Listener callback, final Map<String,String> params) {


        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                callback, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cashflow-API",error.toString(),error);
                error.printStackTrace();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<>();

                for (HttpCookie cookie: cookieManager.getCookieStore().getCookies()) {
                    System.out.println("CSRF-token: " + cookie + " from " + cookie.getDomain());
                    if (cookie.getName().equals("csrftoken")) {
                        headers.put("X-CSRFToken",cookie.getValue());
                    }
                }
                System.out.println(headers);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    static void makeJSONPostRequest(String url, Response.Listener callback, final JSONObject json) {


        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                callback, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cashflow-API",error.toString(),error);
                error.printStackTrace();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<>();

                for (HttpCookie cookie: cookieManager.getCookieStore().getCookies()) {
                    System.out.println("CSRF-token: " + cookie + " from " + cookie.getDomain());
                    if (cookie.getName().equals("csrftoken")) {
                        headers.put("X-CSRFToken",cookie.getValue());
                    }
                }
                System.out.println(headers);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return json.toString().getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        requestQueue.add(stringRequest);
    }
}
