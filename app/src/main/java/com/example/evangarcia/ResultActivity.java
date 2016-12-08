package com.example.evangarcia.fridgerecipes;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {
    public static final String EXTRA_RESULT_ITEM_TEXT =
            "com.example.evangarcia.fridgerecipes.item_view_text";


    //Initializing holders
    HttpURLConnection connection = null;
    BufferedReader reader = null;
    private TextView ResultsTextView;
    private List<String> titleList = new ArrayList<String>();
    private List<String> idList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_activity);

        Typeface Colaborate = Typeface.createFromAsset(getAssets(), "colab.ttf");
        ResultsTextView = (TextView) findViewById(R.id.ResultstextView);
        ResultsTextView.setTypeface(Colaborate);

        //Get intent from main activity that contains the url for the API call
        Intent data = getIntent();

        String rResults = data.getStringExtra("com.example.evangarcia.fridgerecipes.main_result_text");

        //Execute the Search API call
        new JSONTask().execute(rResults);
        ListView list = (ListView) findViewById(R.id.listViewMain);

        //When item is clicked, pass the ID of the selected recipe to the next activity
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                Intent i = new Intent(ResultActivity.this, ItemViewActivity.class);
                i.putExtra(EXTRA_RESULT_ITEM_TEXT, idList.get(position));
                startActivity(i);
            }
        });
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            //Calling the Search request based on ingredients entered
            try {
                URL FoodToFork = new URL(params[0]);

                connection = (HttpURLConnection) FoodToFork.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();

                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                return buffer.toString();

            //Catch if unsuccessful
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //if successful
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //JSON parsing, stores titles in an JSONarray
            try {
                JSONObject jsnobject = new JSONObject(result);
                JSONArray jsonArray = jsnobject.getJSONArray("recipes");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject explrObject = jsonArray.getJSONObject(i);
                    titleList.add(explrObject.getString("title"));
                    idList.add(explrObject.getString("recipe_id"));
                }
                //Stores titles in an array to be clicked
                ArrayAdapter <String> adapter = new ArrayAdapter<String>(ResultActivity.this,R.layout.item,titleList);
                ListView list = (ListView) findViewById(R.id.listViewMain);
                list.setAdapter(adapter);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        }

    }
