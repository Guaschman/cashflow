package se.datasektionen.cashflow;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by viklu on 2017-03-17.
 */

public class CashflowFirebase extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        //Get the updated token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("CASHFLOW-FIREBASE", "Refreshed token: " + refreshedToken);
        sendFirebaseToken();
    }

    static void sendFirebaseToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        HashMap<String,String> params = new HashMap<>();
        params.put("firebase_token",token);
        APIConnection.makePostRequest(MainActivity.cashflow_domain + "api/firebase_instance_id/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("Firebase token updated: " + response);
                    }
                }, params);
    }
}
