package com.example.evangarcia.fridgerecipes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.evangarcia.fridgerecipes.MainActivity.EXTRA_MAIN_CURRENT_FRIDGE;
import static com.example.evangarcia.fridgerecipes.MainActivity.PREFS;
import static com.example.evangarcia.fridgerecipes.R.id.toolbar;

public class ViewFridgeActivity extends AppCompatActivity {

    public static final String EXTRA_INGREDIENT_DELETE_CLICKED = "com.example.garcia.FridgeRecipes.delete_ingredients_clicked";
    private static final String EXTRA_FRIDGE_CLEAR_CLICKED = "com.example.garcia.FridgeRecipes.clear_fridge_clicked";

    //Delcare all Views used in this activity
    private TextView CurrentIngredientsTextView;
    private ScrollView CurrentIngredientsScrollView;
    private TextView ListCurrentIngredientsTextView;
    private TextView DeleteIngredientTextView;
    private EditText DeleteIngredientEditText;
    private Button DeleteIngredientButton;
    private Button ClearFridgeButton;

    //String for what ingredient to delete
    static String IngredientToDelete;
    private static final String KEY_INT_INDEX = "int_index";

    //ArrayList that stores all current ingredients in fridge
    ArrayList<String> CurrentIngredients;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_fridge);

        //Declare Custom Font
        Typeface Colaborate = Typeface.createFromAsset(getAssets(), "colab.ttf");

        //Set Views to their XML counterparts and set their font
        CurrentIngredientsTextView = (TextView) findViewById(R.id.CurrentIngredientsTextView);
        CurrentIngredientsScrollView = (ScrollView) findViewById(R.id.ListCurrentIngredientsScrollView);
        ListCurrentIngredientsTextView = (TextView) findViewById(R.id.ListCurrentIngredientsTextView);
        DeleteIngredientTextView = (TextView) findViewById(R.id.DeleteTextView);
        DeleteIngredientEditText = (EditText) findViewById(R.id.DeleteEditText);
        DeleteIngredientButton = (Button) findViewById(R.id.DeleteButton);
        ClearFridgeButton = (Button) findViewById(R.id.ClearFridgeButton);
        CurrentIngredientsTextView.setTypeface(Colaborate);
        ListCurrentIngredientsTextView.setTypeface(Colaborate);
        DeleteIngredientTextView.setTypeface(Colaborate);
        DeleteIngredientEditText.setTypeface(Colaborate);
        DeleteIngredientButton.setTypeface(Colaborate);
        ClearFridgeButton.setTypeface(Colaborate);
        CurrentIngredientsTextView.setPaintFlags(CurrentIngredientsTextView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        DeleteIngredientEditText.addTextChangedListener(DeleteIngredientListener);


        //Clear the edit text when it is clicked
        DeleteIngredientEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteIngredientEditText.setText("");
            }
        });

        //Get the current ingredients in the fridge
        Intent data = getIntent();

        CurrentIngredients = data.getStringArrayListExtra(EXTRA_MAIN_CURRENT_FRIDGE);

        //If the fridge is empty, checks if their are shared preferences to pull frum
        if(CurrentIngredients.isEmpty())
        {
            retrieve();
        }


        //Put newline for outputting ingredients to screen
        StringBuilder builder = new StringBuilder();
        for (String details : CurrentIngredients) {
            builder.append(details + "\n");
        }

        //Outputting ingredients to screen
        ListCurrentIngredientsTextView.setText(builder.toString());



        //Delete entered ingredient if clicked
        DeleteIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(CurrentIngredients.isEmpty() || IngredientToDelete == null || IngredientToDelete.isEmpty())
                {
                    Toast.makeText(ViewFridgeActivity.this, R.string.NothingToDelete, Toast.LENGTH_LONG).show();
                }
                else {

                    if (!CurrentIngredients.contains(IngredientToDelete.toLowerCase())) {
                        Toast.makeText(ViewFridgeActivity.this, R.string.NotinFridgeString, Toast.LENGTH_LONG).show();
                    } else {
                        for (int i = 0; i < CurrentIngredients.size(); i++) {
                            String tempName = CurrentIngredients.get(i);
                            if (tempName.equals(IngredientToDelete.toLowerCase())) {
                                CurrentIngredients.remove(i);
                            }
                        }

                        StringBuilder builder = new StringBuilder();
                        for (String details : CurrentIngredients) {
                            builder.append(details + "\n");
                        }
                        ListCurrentIngredientsTextView.setText(builder.toString());
                        store();
                        Toast.makeText(ViewFridgeActivity.this, R.string.IngredientDeletedString, Toast.LENGTH_LONG).show();

                        Intent data = new Intent();
                        data.putExtra(EXTRA_INGREDIENT_DELETE_CLICKED, IngredientToDelete);
                        setResult(RESULT_OK, data);
                    }

                }
            }
        });


        //Clear the fridge of all ingredients if clicked
        ClearFridgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(CurrentIngredients.isEmpty())
                {
                    Toast.makeText(ViewFridgeActivity.this, R.string.AlreadyClearedString, Toast.LENGTH_LONG).show();
                }

                else {
                    CurrentIngredients.clear();
                    clear();
                    StringBuilder builder = new StringBuilder();
                    for (String details : CurrentIngredients) {
                        builder.append(details + "\n");
                    }
                    ListCurrentIngredientsTextView.setText(builder.toString());
                    Toast.makeText(ViewFridgeActivity.this, R.string.FridgeClearedString, Toast.LENGTH_LONG).show();

                    Intent data = new Intent();
                    data.putExtra(EXTRA_FRIDGE_CLEAR_CLICKED, true);
                    setResult(RESULT_OK, data);
                }
            }
        });

    }

    //Stores whats in the edittext to IngredientToDelete
    private final TextWatcher DeleteIngredientListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            IngredientToDelete = charSequence.toString();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    //Returns what ingredient to delete to MainActivity if delete ingredient was clicked
    public static String wasDeleteIngredientsClicked(Intent result)
    {
        return result.getStringExtra(EXTRA_INGREDIENT_DELETE_CLICKED);
    }

    //Returns if clear ingredient was clicked to MainActivity
    public static boolean wasClearFridgeClicked(Intent result)
    {
        return result.getBooleanExtra(EXTRA_FRIDGE_CLEAR_CLICKED, false);
    }

    //Store CurrentIngredients into sharedpreferences
    public void store()
    {
        SharedPreferences sharPref = getSharedPreferences(PREFS,0);
        SharedPreferences.Editor editor = sharPref.edit();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CurrentIngredients.size(); i++) {
            sb.append(CurrentIngredients.get(i)).append(",");
        }
        editor.putString(KEY_INT_INDEX,sb.toString());
        editor.commit();
    }

    //Parses the string and turns the data into an array
    //Repopulates all of the data
    //Only follows through if shared pref already exists
    public void retrieve()
    {
        SharedPreferences sharPref = getSharedPreferences(PREFS,0);
        String userString = sharPref.getString(KEY_INT_INDEX,"");
        if(!userString.equals("")) {
            String[] intArray = userString.split(",");
            for (int i = 0; i < intArray.length; i++) {
                CurrentIngredients.add(i,intArray[i]);
            }
        }
    }

    //Clears shared prefs
    public void clear()
    {
        SharedPreferences sharPrefs = getSharedPreferences(PREFS,0);
        SharedPreferences.Editor editor = sharPrefs.edit();
        editor.clear();
        editor.commit();
    }

}
