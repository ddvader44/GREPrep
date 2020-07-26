package com.ddvader44.greprep;
//application id 	b5d9ae95
//api key  a0043cbf63511619477729e64398edc1
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

public class Words extends AppCompatActivity {
    int wordId;
    EditText wordLife;
    TextView merDef,def;
    Button find;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);
        wordLife = findViewById(R.id.wordLife);
        merDef = findViewById(R.id.merriamDef);
        def = findViewById(R.id.def);
        find = findViewById(R.id.findMeaning);
        Intent intent=getIntent();
        wordId = intent.getIntExtra("wordId",-1);
        if(wordId!= -1)
        {
            wordLife.setText(MainActivity.words.get(wordId));
        }
        else
        {
            MainActivity.words.add("");
            wordId = MainActivity.words.size()-1;
        }

        wordLife.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MainActivity.words.set(wordId,String.valueOf(s));
                MainActivity.arrayAdapter.notifyDataSetChanged();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.ddvader44.greprep", Context.MODE_PRIVATE);
                HashSet<String> set = new HashSet<>(MainActivity.words);
                sharedPreferences.edit().putStringSet("words",set).apply();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                def.setVisibility(View.VISIBLE);
                merDef.setVisibility(View.VISIBLE);
                new CallbackTask().execute(dictionaryEntries());
            }
        });

    }
    private String dictionaryEntries() {
        final String language = "en-gb";
        final String word = wordLife.getText().toString();
        final String fields = "definitions";
        final String strictMatch = "false";
        final String word_id = word.toLowerCase();
        return "https://od-api.oxforddictionaries.com:443/api/v2/entries/" + language + "/" + word_id + "?" + "fields=" + fields + "&strictMatch=" + strictMatch;
    }
    private class CallbackTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {


            final String app_id = "b5d9ae95";
            final String app_key = "a0043cbf63511619477729e64398edc1";
            try {
                URL url = new URL(params[0]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept","application/json");
                urlConnection.setRequestProperty("app_id",app_id);
                urlConnection.setRequestProperty("app_key",app_key);

                // read the output from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                return stringBuilder.toString();

            }
            catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String def;
            try {
                JSONObject js = new JSONObject(result);
                JSONArray results = js.getJSONArray("results");

                JSONObject lEntries = results.getJSONObject(0);
                JSONArray laArray = lEntries.getJSONArray("lexicalEntries");

                JSONObject entries = laArray.getJSONObject(0);
                JSONArray e = entries.getJSONArray("entries");

                JSONObject senses = e.getJSONObject(0);
                JSONArray sensesArray = senses.getJSONArray("senses");

                JSONObject d = sensesArray.getJSONObject(0);
                JSONArray de = d.getJSONArray("definitions");

                def = de.getString(0);

                merDef.setText(def);



            }catch (JSONException e)
            {
                e.printStackTrace();
                merDef.setText("No such word found!");
            }

        }
    }
}


