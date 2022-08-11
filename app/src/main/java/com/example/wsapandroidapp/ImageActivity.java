package com.example.wsapandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ImageView imageView = findViewById(R.id.imageView);

        Context context = ImageActivity.this;

        String image = getIntent().getStringExtra("image");

        Glide.with(context).load(image).placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imageView);
    }
}