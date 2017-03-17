package se.datasektionen.cashflow;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by viklu on 2017-03-17.
 */

class AsyncCashflow extends AsyncTask<String, Void, String> {
    private CashflowCallback callback;

    AsyncCashflow(CashflowCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        int tries = 0;
        String url = params[0];
        while (true) {
            try {
                tries++;

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder body = new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
                return body.toString();
            } catch (IOException e) {
                if (tries == 5) { //try 5 times
                    Log.e("MainActivity", "Could not load resource: " + url, e);
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(50 * (1 << tries)); //longer and longer waiting time if things don't work out
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        callback.lambda(s);
    }
}
