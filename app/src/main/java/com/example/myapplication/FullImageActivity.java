package com.example.myapplication;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class FullImageActivity extends AppCompatActivity {

    ImageView myImage, back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        myImage = findViewById(R.id.image);
        back = findViewById(R.id.back);

        Glide.with(this)
                .load(getIntent().getStringExtra("image"))
                .placeholder(R.color.codeGray)
                .into(myImage);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0,0);
            }
        });
    }
}