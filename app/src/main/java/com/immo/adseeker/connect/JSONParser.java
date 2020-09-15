package com.immo.adseeker.connect;
import android.os.AsyncTask;
import android.util.Log;

import com.immo.adseeker.SearchFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class JSONParser extends AsyncTask<String,Void,Void> {
    private String data;
    SearchFragment searchFragment;
    public static ArrayList<String> searchTasks = new ArrayList<>();
    public static boolean googleSearch;
    public static boolean yandexSearch;
    public static int numberOfSites;
    public static int deepParse;
    public static boolean checkHTML;
    public static boolean checkBanners;
    public static boolean checkRedirect;
    public static boolean checkWords;
    public static ArrayList<String> keyWords  = new ArrayList<>();

    public JSONParser(){}

    public JSONParser(SearchFragment sFrag){
        searchFragment = sFrag;
    }


    @Override
    protected Void doInBackground(String... link) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(link[0]).openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            data = "";
            while (line != null){
                line = bufferedReader.readLine();
                if(line != null)
                    data += line;
            }
        } catch (IOException e) {
            Log.e("JSONparser", String.valueOf(e));

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(data != null){
            try {
                JSONObject objJSON = new JSONObject(data);
                JSONArray searchTasksJSON = objJSON.getJSONArray("searchTasks");
                for(int i = 0; i < searchTasksJSON.length();i++){
                    JSONObject arrObj = searchTasksJSON.getJSONObject(i);
                    searchTasks.add(arrObj.getString("task"));

                }
                googleSearch = objJSON.getBoolean("googleSearch");
                yandexSearch = objJSON.getBoolean("yandexSearch");
                numberOfSites = objJSON.getInt("numberOfSites");
                deepParse = objJSON.getInt("deepParse");
                checkHTML = objJSON.getBoolean("checkHtml");
                checkBanners = objJSON.getBoolean("checkBanner");
                checkRedirect = objJSON.getBoolean("checkRedirect");
                checkWords = objJSON.getBoolean("checkKeyWords");

                JSONArray keyWordsJSON = objJSON.getJSONArray("keyWords");
                for(int i = 0; i < keyWordsJSON.length();i++){

                    JSONObject arrObjJSON = keyWordsJSON.getJSONObject(i);
                    keyWords.add(arrObjJSON.getString("word"));
                }
                if(keyWords.size() == 0){
                    Log.e("resultJSON",searchTasks.get(0) + " " + searchTasks.size() + " " + googleSearch + " " + yandexSearch + " " + numberOfSites + " " + deepParse + " " +
                            checkHTML + " " + checkBanners + " " + checkRedirect + " " + checkWords);
                }
                else{
                    Log.e("resultJSON",searchTasks.get(0) + " " + searchTasks.size() + " " + googleSearch + " " + yandexSearch + " " + numberOfSites + " " + deepParse + " " +
                            checkHTML + " " + checkBanners + " " + checkRedirect + " " + checkWords + " " +
                            keyWords.get(0));
                }

            } catch (JSONException e) {
                Log.e("JSONparser", String.valueOf(e));
            }
        }
        if(searchFragment != null){
            searchFragment.setOptions(true);
            searchFragment = null;
        }
    }


    public void clear(){
        searchTasks.clear();
        keyWords.clear();
    }
}
