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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        imageViewDetail = findViewById(R.id.image_view_detail);
        fileName = findViewById(R.id.text_view_name);
        textQuote = findViewById(R.id.text_view_quote);
        textViewLocation = findViewById(R.id.text_view_location);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        try {
            Intent intent = getIntent();
            if (intent.hasExtra("upload_id")) {
                int uploadId = intent.getIntExtra("upload_id", -1); // Default to -1 if not found
                String photoUrl = intent.getStringExtra("photo_url");
                String photoName = intent.getStringExtra("photo_name");
                String photoQuote = intent.getStringExtra("photo_quote");
                String photoLocation = intent.getStringExtra("photo_location");

                // Set the data to your views
                fileName.setText(photoName);
                textQuote.setText(photoQuote);
                textViewLocation.setText(photoLocation);
                Picasso.with(this).load(photoUrl).into(imageViewDetail);
            } else {
                // Handle the case where there's no data passed
                Toast.makeText(this, "No image data provided", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity as there's no data to display
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid image ID", Toast.LENGTH_SHORT).show();
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
                String locationText = "Uploaded from location: " + address.getLocality() + ", " + address.getCountryName();
                textViewLocation.setText(locationText);
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
                super.onPostExecute(quote);
                if (quote != null) {
                    textQuote.setText("\"" + quote.getContent() + "\"\n- " + quote.getAuthor());
                    saveQuoteToDatabase(quote.getContent());
                }
            }
        }.execute();
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
        locationManager.removeUpdates(locationListener);
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

}