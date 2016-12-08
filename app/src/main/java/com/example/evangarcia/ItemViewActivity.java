package com.example.evangarcia.fridgerecipes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static android.icu.lang.UCharacter.toUpperCase;

public class ItemViewActivity extends AppCompatActivity {
        private static String API_SEARCH_URL_BASE = "http://food2fork.com/api/get?key=9bda1f02583ad22b3e1e8236f74285fd&rId=";

        public static final String EXTRA_RESULT_ITEM_TEXT =
                "com.example.evangarcia.fridgerecipes.item_view_text";
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        TextView mTitleView;
        ImageView mImageView;
        TextView mIngredientsLabelTextView;
        List<String> ingList = new ArrayList<String>();
        String recURL;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.item_view_activity);

            //Set all Views to their xml counterparts
            mIngredientsLabelTextView = (TextView) findViewById(R.id.textView4);
            mTitleView = (TextView) findViewById(R.id.titleView);
            mImageView = (ImageView) findViewById(R.id.imageViewSolo);

            //Allows the ImageView to be clickable
            mImageView.setClickable(true);

            //Declare Custom Font and Set Custom Fonts with Underlining
            Typeface Colaborate = Typeface.createFromAsset(getAssets(), "colab.ttf");
            mTitleView.setTypeface(Colaborate);
            mTitleView.setPaintFlags(mTitleView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            mIngredientsLabelTextView.setTypeface(Colaborate);
            mIngredientsLabelTextView.setPaintFlags(mIngredientsLabelTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            //Get the selected result's data from the results activity
            Intent data = getIntent();
            String rResults = data.getStringExtra("com.example.evangarcia.fridgerecipes.item_view_text");
            rResults = API_SEARCH_URL_BASE+rResults;


            //Calls JSONTask
            new JSONTask().execute(rResults);

            //When ImageView is Clicked, the app opens a browser and takes you to the recipe's website
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( recURL ) );
                    startActivity( browse );
                }
            });

        }


        public class JSONTask extends AsyncTask<String, String, String> {

            @Override
            protected String doInBackground(String... params) {

                //Makes an http request to the Food2Fork API, and returns a JSON file
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

                 //Catch if API call fails
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
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

            //Parses JSON FIle and stores the ingredients into a JSON Array
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                try {
                    JSONObject jsnobject = new JSONObject(result);
                        JSONObject explrObject = jsnobject.getJSONObject("recipe");
                        mTitleView.setText(explrObject.getString("title"));
                    JSONArray jsonArray = explrObject.getJSONArray("ingredients");
                    recURL = explrObject.getString("source_url");
                    GetXMLTask task = new GetXMLTask();
                    task.execute(new String[] { explrObject.getString("image_url") });
                    String listNum;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        listNum = Integer.toString(i+1) + ". ";
                        listNum+=jsonArray.getString(i);
                        ingList.add(toUpperCase(listNum));
                    }

                    //Puts the Ingredients into a listview to be displayed to the user
                    ArrayAdapter <String> adapter = new ArrayAdapter<String>(ItemViewActivity.this,R.layout.item,ingList);
                    ListView list = (ListView) findViewById(R.id.listViewItem);
                    list.setAdapter(adapter);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

        }

    }
        //The below following code is based off a lot of answers on stack overflow. Built from mashing some answers together
        //Uses the image url that is found in the JSON file to yank the image
        //Turns image into a bitmap to be displayed
        private class GetXMLTask extends AsyncTask<String, Void, Bitmap> {
            @Override
            protected Bitmap doInBackground(String... urls) {
                Bitmap map = null;
                for (String url : urls) {
                    map = downloadImage(url);
                }
                return map;
            }

            // Sets the Bitmap returned by doInBackground
            @Override
            protected void onPostExecute(Bitmap result) {
                mImageView.setImageBitmap(result);
            }
            // Creates Bitmap from InputStream and returns it
            private Bitmap downloadImage(String url) {
                Bitmap bitmap = null;
                InputStream stream = null;
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = 1;
                bmOptions.outHeight = 3000;
                bmOptions.outWidth = 3000;


                try {
                    stream = getHttpConnection(url);
                    bitmap = BitmapFactory.
                            decodeStream(stream, null, bmOptions);
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return bitmap;
            }

            // Makes HttpURLConnection and returns InputStream
            private InputStream getHttpConnection(String urlString)
                    throws IOException {
                InputStream stream = null;
                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();

                try {
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    httpConnection.setRequestMethod("GET");
                    httpConnection.connect();

                    if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        stream = httpConnection.getInputStream();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return stream;
            }
        }

}
