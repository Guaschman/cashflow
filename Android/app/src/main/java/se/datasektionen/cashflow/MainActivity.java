package se.datasektionen.cashflow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.Response;

public class MainActivity extends AppCompatActivity {

    final static String cashflow_domain = "http://85.229.242.121:8000/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        APIConnection.setUp(this);

        login();
    }


    private void login() {
        WebView webView = new WebView(this);
        setContentView(webView);
        //webView.getSettings().setJavaScriptEnabled(true); //do we need this?
        webView.loadUrl("https://login2.datasektionen.se/login?callback=" + cashflow_domain + "api/login/");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.startsWith(cashflow_domain + "api/login/")) {
                    // We have been logged in and can now use the entire url with the token to grab cookie
                    view.stopLoading();
                    System.err.println("Attempting to log in");
                    APIConnection.makeGetRequest(url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            System.err.println("Logged in now!!");
                            CashflowFirebase.sendFirebaseToken();
                            Intent newExpenseForm = new Intent(getApplicationContext(), ExpenseForm.class);
                            System.err.println("Intent created");
                            startActivity(newExpenseForm);
                            System.err.println("Intent fired");
                        }
                    });
                }
            }
        });

    }

}
