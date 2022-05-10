package com.project1.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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

public class MainActivity extends AppCompatActivity {

    private EditText countryCode, etCityName;
    private TextView tv_result;
    private String mCity;
    private RequestQueue mRequestQueue;

    private Button get;
    final static String ApiKey = "16331473ce854d12da03edbc0e46807e";
    final String url = "https://api.openweathermap.org/data/2.5/weather";

    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCityName = findViewById(R.id.et_cityName);
        countryCode = findViewById(R.id.et_countryCode);
        tv_result = findViewById(R.id.tv_result);
        get = findViewById(R.id.bt_get);


        mRequestQueue = Volley.newRequestQueue(getApplicationContext());

        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isNetworkAvailable()) {
                    Log.e("MainAct", "Diaglog banega");
                    MakeDialog();
                }
                else {
                    mCity = etCityName.getText().toString();
                    jsonParse();
                    Log.e("CityName", mCity);

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
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
                    mCity = etCityName.getText().toString();
                    jsonParse();
                    Log.e("CityName", mCity);

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etCityName.getWindowToken(), 0);
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

    private void jsonParse() {
        String country_Code = countryCode.getText().toString();
        String tempURL = "";

        if (mCity.isEmpty()) {
            tv_result.setText("Please ! Enter city name....!");
        } else {
            if (country_Code.equals("")) {
                tempURL = url + "?q=" + mCity + "&appid=" + ApiKey;
            }
            else {
                tempURL = url + "?q=" + mCity + "," + country_Code + "&appid=" + ApiKey;
            }
        }

        Log.e("MainAct", tempURL);

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

                    tv_result.setText(result);


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();
                Toast.makeText(MainActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();

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
}