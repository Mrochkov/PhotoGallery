package com.example.photogallery;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ImageDetailActivity extends AppCompatActivity {
    private ImageView imageViewDetail;
    TextView fileName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        imageViewDetail = findViewById(R.id.image_view_detail);
        fileName = findViewById(R.id.text_view_name);

        // Retrieve the photo URL from the Intent extras
        String photoUrl = getIntent().getStringExtra("photo_url");
        String photoName = getIntent().getStringExtra("photo_name");

        fileName.setText(photoName);

        // Load and display the selected photo using Picasso or any other image-loading library
        Picasso.with(this)
                .load(photoUrl)
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(imageViewDetail);

        // Add code to display other photo details as needed
    }
}