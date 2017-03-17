package se.datasektionen.cashflow;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;

public class MainActivity extends AppCompatActivity {

    final String cashflow_domain = "http://85.229.242.121:8000/";
    private CookieStore cookieStore = new CashFlowCookiestore();
    private JSONObject budget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.webkit.CookieManager.getInstance().setAcceptCookie(true);

        CookieManager cookieManager = new CookieManager(cookieStore,CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

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

                    new AsyncCashflow(new CashflowCallback() {
                        @Override
                        void lambda(String body) {

                            setContentView(R.layout.activity_main);
                            getBudget();
                        }
                    }).execute(url);
                }
            }
        });

    }

    private void getBudget() {
        new AsyncCashflow(new CashflowCallback() {
            @Override
            void lambda(String body) {
                try {
                    budget = new JSONObject(body);
                    System.out.println(budget);
                } catch (JSONException e) {
                    Log.e("MainActivity-budget","Error when parsing: " + body, e);
                    e.printStackTrace();
                }
            }
        }).execute(cashflow_domain+"api/budget/");
    }
}
