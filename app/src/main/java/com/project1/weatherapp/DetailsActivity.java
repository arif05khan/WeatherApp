package com.project1.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class DetailsActivity extends AppCompatActivity {

    private RequestQueue mRequestQueue;
    private String mCity;
    private TextView mTvResult, mTvCity;

    private static final String ApiKey = "16331473ce854d12da03edbc0e46807e";
    private static final String url = "https://api.openweathermap.org/data/2.5/weather";

    private DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mRequestQueue = Volley.newRequestQueue(this);
        mTvCity = findViewById(R.id.details_tv_cityName);
        mTvResult = findViewById(R.id.details_tv_result);

        Bundle bundle = getIntent().getExtras();
         mCity = bundle.getString("cityName");

        if(!mCity.equals("")) {
            mTvCity.setText(mCity);
            jsonParse();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Details");
    }

    private void jsonParse() {
        String tempURL = "";
        tempURL = url + "?q=" + mCity + "&appid=" + ApiKey;

        Log.e("DetailAct", tempURL);

        StringRequest request = new StringRequest(Request.Method.GET, tempURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    String result = "";
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                    JSONObject JsonWeather = jsonArray.getJSONObject(0);

                    String description = JsonWeather.getString("description");

                    JSONObject JsonMain = jsonResponse.getJSONObject("main");
                    double temp = JsonMain.getDouble("temp") - 273.15;
                    double temp_min = JsonMain.getDouble("temp_min") - 273.15;
                    double temp_max = JsonMain.getDouble("temp_max") - 273.15;
                    int humidity = JsonMain.getInt("humidity");

                    JSONObject jsonWind = jsonResponse.getJSONObject("wind");
                    String wind = jsonWind.getString("speed");

                    JSONObject jsonSys = jsonResponse.getJSONObject("sys");
                    String country = jsonSys.getString("country");
                    String city = jsonResponse.getString("name");

                    result += "Current weather of " + city + " (" + country + ") " + "\n" +
                            "Temperature: " + df.format(temp) + " °C\n" + "Minimum Temperature: " + df.format(temp_min) + " °C\n" +
                            "Maximum Temperature: " + df.format(temp_max) + " °C\n" + "Humidity: " + humidity + " %\n" +
                            "Wind Speed: " + wind + " m/s\n" + "\n Hope you found this informative !";

                    mTvResult.setText(result);


                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(DetailsActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();
            }
        });

        mRequestQueue.add(request);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("cityName", mCity);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(!isNetworkAvailable()) {
            MakeDialog();
        }
        else {
            mCity = savedInstanceState.getString("cityName");
            jsonParse();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void MakeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connection");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!isNetworkAvailable()) {
                    Log.e("MainAct", "Diaglog banega");
                    MakeDialog();
                }
                else {
                    dialog.dismiss();
                    mCity = mTvCity.getText().toString();
                    jsonParse();
                    Log.e("CityName", mCity);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }
}