package se.datasektionen.cashflow;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        expensePartIds.put("amountInput",amountInput.getId());
        expensePart.addView(amountInput);


        TextView committeeDescription = new TextView(this);
        committeeDescription.setText("Nämnd");
        expensePart.addView(committeeDescription);

        Spinner commiteePicker = new Spinner(this);
        commiteePicker.setId(View.generateViewId());
        expensePartIds.put("committee",commiteePicker.getId());
        try {
            JSONArray committees = budget.getJSONArray("committees");
            String[] committeeNames = new String[committees.length()];
            for (int i=0; i < committees.length(); i++) {
                committeeNames[i] = committees.getJSONObject(i).getString("committee_name");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_spinner_item, committeeNames);
            commiteePicker.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        commiteePicker.setBackgroundColor(Color.BLUE);

        commiteePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(((TextView) view).getText());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        expensePart.addView(commiteePicker);

        expenseParts.add(expensePartIds);

        expensePartList = (LinearLayout) findViewById(R.id.ExpensePartList);
        expensePartList.addView(expensePart, expensePartList.getChildCount()-1);
    }

    public void sendExpense(View view) {
        Spinner committee = (Spinner)expensePartList.findViewById(expenseParts.get(0).get("committees"));
        System.out.println(committee.getSelectedItem().toString());
    }

}
