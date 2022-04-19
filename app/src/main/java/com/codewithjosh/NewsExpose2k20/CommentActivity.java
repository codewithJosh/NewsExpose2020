package com.codewithjosh.NewsExpose2k20;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class CommentActivity extends AppCompatActivity {

    EditText add_comment;
    ImageView image_profile, send, back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        add_comment = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.image_profile);
        send = findViewById(R.id.send);
        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommentActivity.this, MainActivity.class));
            }
        });

    }

}