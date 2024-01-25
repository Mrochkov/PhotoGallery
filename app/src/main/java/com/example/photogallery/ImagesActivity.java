package com.example.photogallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;



import java.util.ArrayList;
import java.util.List;

public class ImagesActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private List<Upload> mUpload;
    private ProgressBar mProgressCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mProgressCircle = findViewById(R.id.progress_circle);


        DatabaseHelper db = new DatabaseHelper(this);
        mUpload = db.getAllUploads();
        mAdapter = new ImageAdapter(ImagesActivity.this, db);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(ImagesActivity.this);



    }

    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(this, ImageDetailActivity.class);
        Upload clickedItem = mUpload.get(position);

        detailIntent.putExtra("upload_id", clickedItem.getId());
        detailIntent.putExtra("photo_url", clickedItem.getImageUrl());
        detailIntent.putExtra("photo_name", clickedItem.getName());
        detailIntent.putExtra("photo_quote", clickedItem.getQuote());
        detailIntent.putExtra("photo_location", clickedItem.getLocation());

        startActivity(detailIntent);
    }


    @Override
    public void onWhateverClick(int position) {
        Toast.makeText(this, "Whatever click at position: " + position, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDeleteClick(int position) {
        DatabaseHelper db = new DatabaseHelper(this);
        Upload clickedItem = mUpload.get(position);
        db.deleteUpload(clickedItem.getId());
        mUpload.remove(position);
        refreshRecyclerView();

        Toast.makeText(this, "Deleted item at position: " + position, Toast.LENGTH_SHORT).show();
    }

    private void refreshRecyclerView() {
        mAdapter = new ImageAdapter(ImagesActivity.this, new DatabaseHelper(this));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}