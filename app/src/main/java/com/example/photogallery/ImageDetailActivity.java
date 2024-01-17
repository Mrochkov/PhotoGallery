package com.example.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageDetailActivity extends AppCompatActivity {
    private ImageView imageViewDetail;
    private TextView fileName;
    private TextView textQuote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

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
}