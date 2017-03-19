package se.datasektionen.cashflow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ExpenseForm extends AppCompatActivity {

    private JSONObject budget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.err.println("Created new expense form activity");
        setContentView(R.layout.activity_expense_form);
        getBudget();
    }

    private void getBudget() {
        APIConnection.makeGetRequest(
                MainActivity.cashflow_domain+"api/budget/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            budget = new JSONObject(response);
                            System.out.println("BUDGET: " + budget);
                            TextView budgetTextView = (TextView)findViewById(R.id.budgetTextView);
                            budgetTextView.setText(budget.toString(2));
                        } catch (JSONException e) {
                            Log.e("MainActivity-budget","Error when parsing: " + response, e);
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    private LinearLayout expensePartList;

    private ArrayList<HashMap<String,Integer>> expenseParts = new ArrayList<>();


    public void newExpensePart(View view) {
        LinearLayout expensePart = new LinearLayout(this);
        expensePart.setOrientation(LinearLayout.VERTICAL);

        HashMap<String,Integer> expensePartIds = new HashMap<>();

        TextView amountDescription = new TextView(this);
        amountDescription.setText("Belopp");
        expensePart.addView(amountDescription);

        EditText amountInput = new EditText(this);
        amountInput.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amountInput.setPadding(0,0,0,40);
        amountInput.setId(View.generateViewId());
        expensePartIds.put("amount",amountInput.getId());
        expensePart.addView(amountInput);


        TextView committeeDescription = new TextView(this);
        committeeDescription.setText("Välj en budgetposts id från listan");
        expensePart.addView(committeeDescription);

        Spinner budgetPicker = new Spinner(this);
        budgetPicker.setId(View.generateViewId());
        expensePartIds.put("budget",budgetPicker.getId());

        try {
            JSONArray committees = budget.getJSONArray("committees");
            ArrayList<String> budgetIDs = new ArrayList<>();
            for (int i=0; i < committees.length(); i++) {
                JSONArray cost_centers = committees.getJSONObject(i).getJSONArray("cost_centres");
                for (int j=0; j < cost_centers.length(); j++) {
                    JSONArray budget_lines = cost_centers.getJSONObject(j).getJSONArray("budget_lines");
                    for (int k=0; k < budget_lines.length(); k++) {
                        budgetIDs.add(Integer.toString(budget_lines.getJSONObject(k).getInt("budget_line_id")));
                    }
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_spinner_item, budgetIDs);
            budgetPicker.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        budgetPicker.setBackgroundColor(Color.BLUE);

        expensePart.addView(budgetPicker);

        expenseParts.add(expensePartIds);

        expensePartList = (LinearLayout) findViewById(R.id.ExpensePartList);
        expensePartList.addView(expensePart, expensePartList.getChildCount()-1);
    }

    public void sendExpense(View view) {
        JSONObject createExpenseJSON = new JSONObject();
        try {
            createExpenseJSON.put(
                    "description",
                    ((EditText)findViewById(R.id.description)).getText().toString()
            );

            createExpenseJSON.put(
                    "expense_date",
                    ((EditText)findViewById(R.id.date)).getText().toString()
            );
            JSONArray expensePartsJSONArray = new JSONArray();
            for (int i=0; i < expenseParts.size(); i++) {
                JSONObject expensePart = new JSONObject();
                expensePart.put(
                        "budget_line_id",
                        Integer.parseInt(((Spinner)findViewById(
                                expenseParts.get(i).get("budget")
                        )).getSelectedItem().toString())
                );
                expensePart.put(
                        "amount",
                        Integer.parseInt(((EditText)findViewById(
                                expenseParts.get(i).get("amount")
                        )).getText().toString())
                );
                expensePartsJSONArray.put(expensePart);
            }
            createExpenseJSON.put("expense_parts",expensePartsJSONArray);
            APIConnection.makeJSONPostRequest(MainActivity.cashflow_domain + "api/expense/", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println(response);
                    if (imageBitmap != null) { // An image has been chosen
                        HashMap<String,String> params = new HashMap<>();
                        params.put("file",getStringImage(imageBitmap));
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            params.put("expense", Integer.toString(responseJSON.getJSONObject("expense").getInt("id")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        APIConnection.makePostRequest(MainActivity.cashflow_domain + "api/file/", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println(response);
                            }
                        }, params);

                    }
                }
            },createExpenseJSON);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int PICK_IMAGE_REQUEST = 1;
    Bitmap imageBitmap;

    public void chooseImage(View view) {
        //Use  MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        //This line totaly yanked off http://stackoverflow.com/questions/4922037/android-let-user-pick-image-or-video-from-gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent.createChooser(intent, "Select picture"),PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Method totaly yanked from https://www.simplifiedcoding.net/android-volley-tutorial-to-upload-image-to-server/
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
}
