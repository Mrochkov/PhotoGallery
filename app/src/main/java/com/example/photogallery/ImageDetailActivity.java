package com.example.photogallery;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.widget.Toast;


public class ImageDetailActivity extends AppCompatActivity {
    private ImageView imageViewDetail;
    private TextView fileName;
    private TextView textViewLocation;
    private TextView textQuote;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Upload upload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        imageViewDetail = findViewById(R.id.image_view_detail);
        fileName = findViewById(R.id.text_view_name);
        textQuote = findViewById(R.id.text_view_quote);
        textViewLocation = findViewById(R.id.text_view_location);
        TextView textViewCountry = findViewById(R.id.text_view_country);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        Intent intent = getIntent();
        if (intent.hasExtra("upload_id")) {
            int uploadId = intent.getIntExtra("upload_id", -1);
            DatabaseHelper db = new DatabaseHelper(this);
            upload = db.getUpload(uploadId);
            if (upload != null) {
                Picasso.with(this).load(new File(upload.getImageUrl())).into(imageViewDetail);
                fileName.setText(upload.getName());
                textViewLocation.setText(upload.getLocation());
                if (upload.getQuote().equals("Fetching quote...")) {
                    fetchRandomQuote();
                } else {
                    textQuote.setText(upload.getQuote());
                }
                if (upload.getLocation().equals("Fetching location...")) {
                    setupLocationListener();
                }
            }
        } else {
            Toast.makeText(this, "No image data provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupLocationListener();
        fetchRandomQuote();


    }


        private void setupLocationListener() {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    updateLocationText(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    private void updateLocationText(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String cityName = address.getLocality();
                if (upload != null) {
                    fetchCityData(cityName, upload);
                    String locationText = "Uploaded from location: " + cityName;
                    textViewLocation.setText(locationText);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            textViewLocation.setText("Unable to get location");
        }
    }




    private void fetchRandomQuote() {
        new AsyncTask<Void, Void, Quote>() {
            @Override
            protected Quote doInBackground(Void... voids) {
                try {
                    URL url = new URL("https://api.quotable.io/random");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return parseQuote(response.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Quote quote) {
                if (quote != null) {
                    String quoteText = "\"" + quote.getContent() + "\"\n- " + quote.getAuthor();
                    textQuote.setText(quoteText);
                    saveDetailsToDatabase(quoteText, textViewLocation.getText().toString());
                }
            }
        }.execute();
    }

    private void fetchCityData(String cityName, final Upload upload) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    String urlString = "https://api.api-ninjas.com/v1/city?name=" + URLEncoder.encode(cityName, "UTF-8");
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("X-Api-Key", "jA7r8AoQbSfDfPerPk+qPw==OlF3XTRWVGgDSAwW");
                    connection.setRequestMethod("GET");
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String response) {
                if (response != null && !response.isEmpty()) {
                    parseAndDisplayCityInfo(response, upload);
                }
            }
        }.execute();
    }
    private void parseAndDisplayCityInfo(String response, Upload upload) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            if (jsonArray.length() > 0) {
                JSONObject cityInfo = jsonArray.getJSONObject(0);
                String country = cityInfo.optString("country", "Not available");
                int population = cityInfo.optInt("population", -1);
                boolean isCapital = cityInfo.optBoolean("is_capital", false);

                upload.setCountry(country);
                upload.setPopulation(population);
                upload.setCapital(isCapital);

                DatabaseHelper db = new DatabaseHelper(this);
                db.updateUpload(upload);

                displayUploadDetails(upload);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void displayUploadDetails(Upload upload) {
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("Country: ").append(upload.getCountry()).append("\n");
        infoBuilder.append("Population: ").append(upload.getPopulation()).append("\n");
        infoBuilder.append("Is Capital: ").append(upload.isCapital() ? "Yes" : "No").append("\n");

        TextView textViewDetails = findViewById(R.id.text_view_country);
        textViewDetails.setText(infoBuilder.toString());
    }

    private void setupView(int uploadId) {
        DatabaseHelper db = new DatabaseHelper(this);
        Upload upload = db.getUpload(uploadId);

        if (upload != null) {
            if (upload.getCountry() == null || upload.getPopulation() == -1) {
                fetchCityData(upload.getLocation(), upload);
            } else {
                displayUploadDetails(upload);
            }
        }
    }
    private void saveQuoteToDatabase(String quote) {
        try {
            int uploadId = getIntent().getIntExtra("upload_id", -1);
            if (uploadId != -1) {
                DatabaseHelper db = new DatabaseHelper(this);
                Upload upload = db.getUpload(uploadId);
                if (upload != null) {
                    upload.setQuote(quote);
                    db.updateUpload(upload);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Quote parseQuote(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String content = jsonObject.getString("content");
            String author = jsonObject.getString("author");
            return new Quote(content, author);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    private void fetchLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String locationText = "Uploaded from location: " + address.getLocality() + ", " + address.getCountryName();
                textViewLocation.setText(locationText);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void saveDetailsToDatabase(String quote, String location) {
        try {
            int uploadId = getIntent().getIntExtra("upload_id", -1);
            if (uploadId != -1) {
                DatabaseHelper db = new DatabaseHelper(this);
                Upload upload = db.getUpload(uploadId);
                if (upload != null) {
                    upload.setQuote(quote);
                    upload.setLocation(location);
                    db.updateUpload(upload);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}