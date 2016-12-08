package com.example.evangarcia.fridgerecipes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import static com.example.evangarcia.fridgerecipes.ViewFridgeActivity.EXTRA_INGREDIENT_DELETE_CLICKED;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SETTINGS = 0;
    //Tags for Intent Calls and API Call URL
    public static final String EXTRA_MAIN_CURRENT_FRIDGE = "com.example.garcia.FridgeRecipes.main_current_fridge";
    private static String API_SEARCH_URL_BASE = "http://food2fork.com/api/search?key=9bda1f02583ad22b3e1e8236f74285fd&q=";
    public static final String EXTRA_MAIN_RESULT_TEXT =
            "com.example.evangarcia.fridgerecipes.main_result_text";

    public static final String PREFS = "sharPrefs";
    private static final String KEY_INT_INDEX = "int_index";

    //Declare views for this activity
    private TextView AddIngredientTextView;
    private EditText AddIngredientEditText;
    private Button AddIngredientButton;
    private Button SearchAllButton;
    private TextView CustomSearchTextView;
    private EditText CustomSearchEditText;
    private Button CustomSearchButton;
    private TextView Test;

    //Stores what ingredients are in the fridge
    private ArrayList<String> FridgeIngredientsArrayList = new ArrayList<>(50);

    //HTTP Connection declaration and string declarations
    HttpURLConnection connection = null;
    BufferedReader reader = null;
    static String CustomIngredient = null;
    static String FridgeIngredient = null;
    static String CustomSearch = null;
    static String FridgeSearch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Calls retrieve function
        retrieve();


        //Declare and set custom font and set views to their xml counterparts
        Typeface Colaborate = Typeface.createFromAsset(getAssets(), "colab.ttf");
        AddIngredientTextView = (TextView) findViewById(R.id.AddTextView);
        AddIngredientEditText = (EditText) findViewById(R.id.AddEditText);
        AddIngredientButton = (Button) findViewById(R.id.AddButton);
        SearchAllButton = (Button) findViewById(R.id.SearchWithAllIngredientsButton);
        CustomSearchTextView = (TextView) findViewById(R.id.CustomSearchTextView);
        CustomSearchEditText = (EditText) findViewById(R.id.CustomSearchEditText);
        CustomSearchButton = (Button) findViewById(R.id.CustomSearchButton);
        AddIngredientTextView.setTypeface(Colaborate);
        AddIngredientEditText.setTypeface(Colaborate);
        AddIngredientButton.setTypeface(Colaborate);
        SearchAllButton.setTypeface(Colaborate);
        CustomSearchTextView.setTypeface(Colaborate);
        CustomSearchEditText.setTypeface(Colaborate);
        CustomSearchButton.setTypeface(Colaborate);
        Test = (TextView) findViewById(R.id.TestTextView);


        //Add listeners to the EditTexts
        AddIngredientEditText.addTextChangedListener(AddIngredientsToFridgeListener);
        CustomSearchEditText.addTextChangedListener(CustomSearchIngredientsListener);

        //Clear EditText when clicked
        AddIngredientEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddIngredientEditText.setText("");
            }
        });

        //Clear EditText when clicked
        CustomSearchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomSearchEditText.setText("");
            }
        });


        //Add Ingredient to ArrayList
        AddIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (FridgeIngredient == null || FridgeIngredient.isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.PlsEnterIngredientString, Toast.LENGTH_LONG).show();
                } else {
                    if (FridgeIngredient.contains(",")) {
                        Toast.makeText(MainActivity.this, R.string.EnterOneIngredientAtATimeString, Toast.LENGTH_LONG).show();
                    } else {
                        if (FridgeIngredientsArrayList.contains(FridgeIngredient.toLowerCase()))
                            Toast.makeText(MainActivity.this, R.string.AlreadyInFridgeString, Toast.LENGTH_LONG).show();
                        else if (!FridgeIngredientsArrayList.contains(FridgeIngredient)) {
                            Toast.makeText(MainActivity.this, R.string.IngredientAddedString, Toast.LENGTH_LONG).show();
                            FridgeIngredientsArrayList.add(FridgeIngredient.toLowerCase());
                            AddIngredientEditText.setText("");
                            store();
                        }
                    }
                }
            }
        });

        //Search using Fridge
        SearchAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (FridgeIngredientsArrayList.isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.AddIngredientInFridgeString, Toast.LENGTH_LONG).show();
                } else {

                    FridgeIngredient = "";
                    for (int i = 0; i < FridgeIngredientsArrayList.size(); i++) {
                        if (i < FridgeIngredientsArrayList.size() - 1)
                            FridgeIngredient = FridgeIngredient + FridgeIngredientsArrayList.get(i) + ',';
                        else
                            FridgeIngredient = FridgeIngredient + FridgeIngredientsArrayList.get(i);
                    }

                    FridgeIngredient = FridgeIngredient.replace(",", "%2C");
                    FridgeIngredient = FridgeIngredient.replace(" ", "+");
                    FridgeSearch = API_SEARCH_URL_BASE + FridgeIngredient;

                    Intent i = new Intent(MainActivity.this, ResultActivity.class);
                    i.putExtra(EXTRA_MAIN_RESULT_TEXT, FridgeSearch);
                    startActivity(i);
                }
            }
        });

        //Search using custom search
        CustomSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (CustomIngredient == null || CustomIngredient.isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.PlsEnterSearchValueString, Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(MainActivity.this, ResultActivity.class);
                    i.putExtra(EXTRA_MAIN_RESULT_TEXT, CustomSearch);
                    startActivity(i);
                }

            }
        });


    }

    //Sets whats in the EditText to FridgeIngredient
    private final TextWatcher AddIngredientsToFridgeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            FridgeIngredient = charSequence.toString();

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    //Sets whats in the EditText to CustomIngredient

    private final TextWatcher CustomSearchIngredientsListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            CustomIngredient = charSequence.toString();


            //Add this into Function
            CustomIngredient = CustomIngredient.replace(",", "%2C");
            CustomIngredient = CustomIngredient.replace(" ", "+");
            CustomSearch = API_SEARCH_URL_BASE + CustomIngredient;
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //If toolbar ViewFridge is clicked, start ViewFridgeActivity and pass in the FridgeIngredients array
        if (id == R.id.action_settings) {

            Intent i = new Intent(MainActivity.this, ViewFridgeActivity.class);

            i.putExtra(EXTRA_MAIN_CURRENT_FRIDGE, FridgeIngredientsArrayList);

            startActivityForResult(i, REQUEST_CODE_SETTINGS);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        //Call certain functions if their buttons were clicked in SettingsActivity
        if (requestCode == REQUEST_CODE_SETTINGS) {
            if (data == null)
                return;

            if (ViewFridgeActivity.wasDeleteIngredientsClicked(data) != null)
                DeleteIngredient(data.getStringExtra(EXTRA_INGREDIENT_DELETE_CLICKED));

            if (ViewFridgeActivity.wasClearFridgeClicked(data))
                ClearFridge();


        }
    }

    //Delete a specific ingredient from the fridge that was returned from the ViewFridgeActivity
    private void DeleteIngredient(String ItemToBeDeleted) {

        for (int i = 0; i < FridgeIngredientsArrayList.size(); i++) {
            String tempName = FridgeIngredientsArrayList.get(i);
            if (tempName.equals(ItemToBeDeleted)) {
                FridgeIngredientsArrayList.remove(i);
            }
        }
    }

    //Clear the entire fridge of ingredients
    private void ClearFridge() {

        FridgeIngredientsArrayList.clear();
    }

    //Stores data away into shared preference
    //Shared preference can't take arrays so the int and bool arrays are "serialized" to string
    //in order to push to shared pref

    public void store()
    {
        SharedPreferences sharPref = getSharedPreferences(PREFS,0);
        SharedPreferences.Editor editor = sharPref.edit();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < FridgeIngredientsArrayList.size(); i++) {
            sb.append(FridgeIngredientsArrayList.get(i)).append(",");
        }
        editor.putString(KEY_INT_INDEX,sb.toString());
        editor.commit();
    }

    //Parses the string and turns the data into an array
    //Repopulates all of the data
    //Only follows through if shared pref aleady exists
    public void retrieve()
    {
        SharedPreferences sharPref = getSharedPreferences(PREFS,0);
        String userString = sharPref.getString(KEY_INT_INDEX,"");
        if(!userString.equals("")) {
            String[] intArray = userString.split(",");
            for (int i = 0; i < intArray.length; i++) {
                FridgeIngredientsArrayList.add(i,intArray[i]);
            }
        }
    }
}