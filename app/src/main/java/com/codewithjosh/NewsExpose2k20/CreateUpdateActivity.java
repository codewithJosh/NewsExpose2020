package com.codewithjosh.NewsExpose2k20;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CreateUpdateActivity extends AppCompatActivity {

    ImageView close, image_added;
    TextView update;
    EditText subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_update);

        close = findViewById(R.id.back);
        image_added = findViewById(R.id.image_added);
        update = findViewById(R.id.update);
        subject = findViewById(R.id.subject);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreateUpdateActivity.this, HomeActivity.class));
                finish();
            }
        });

    }

}