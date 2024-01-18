package com.example.photogallery;

import android.content.Context;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
        textViewLocation = findViewById(R.id.text_view_location);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String photoQuote = getIntent().getStringExtra("photo_quote");
        String photoLocation = getIntent().getStringExtra("photo_location");

        if (photoQuote != null && !photoQuote.isEmpty()) {
            textQuote.setText(photoQuote);
        } else {
            fetchRandomQuote();
        }



        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Handle location changes
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                if (photoLocation != null && !photoLocation.isEmpty()) {
                    textViewLocation.setText(photoLocation);
                } else {
                    fetchLocation(latitude, longitude);
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener);
        }

        imageViewDetail = findViewById(R.id.image_view_detail);
        fileName = findViewById(R.id.text_view_name);
        textQuote = findViewById(R.id.text_view_quote);
        fetchRandomQuote();

        String photoUrl = getIntent().getStringExtra("photo_url");
        String photoName = getIntent().getStringExtra("photo_name");

        fileName.setText(photoName);


        Picasso.with(this)
                .load(photoUrl)
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(imageViewDetail);

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
                    saveDetailsToFirebase(quote.getContent(), null);
                }
            }
        }.execute();
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

    private void saveDetailsToFirebase(String quote, String location) {
        String uploadId = getIntent().getStringExtra("upload_id");
        if (uploadId != null) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploads").child(uploadId);
            if (quote != null) {
                databaseRef.child("mQuote").setValue(quote);
            }
            if (location != null) {
                databaseRef.child("mLocation").setValue(location);
            }
        }
    }
    private void updateLocationInFirebase(String location) {
        String uploadId = getIntent().getStringExtra("upload_id");
        if (uploadId != null) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploads").child(uploadId);
            databaseRef.child("mLocation").setValue(location);
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

                // Update Firebase
                updateLocationInFirebase(locationText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}