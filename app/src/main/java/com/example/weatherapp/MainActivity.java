package com.example.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText etCity;
    private TextView tvResult;
    private Button btnGetWeather;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCity = findViewById(R.id.etCity);
        tvResult = findViewById(R.id.tvResult);
        btnGetWeather = findViewById(R.id.btnGetWeather);
        
        executorService = Executors.newSingleThreadExecutor();

        btnGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = etCity.getText().toString().trim();
                if (city.isEmpty()) {
                    tvResult.setText("Error: Please enter a city name.");
                    return;
                }
                tvResult.setText("Fetching weather for " + city + "...");
                fetchWeather(city);
            }
        });
    }

    private void fetchWeather(String city) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
                    String urlString = "https://wttr.in/" + encodedCity + "?format=Location:+%l%nCondition:+%C%nTemperature:+%t%nHumidity:+%h%nWind:+%w";
                    
                    URL url = new URI(urlString).toURL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent", "Android Weather App");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line).append("\n");
                        }
                        reader.close();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvResult.setText(response.toString().trim());
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvResult.setText("Error: Could not retrieve data (HTTP " + responseCode + ")");
                            }
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvResult.setText("Error: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
