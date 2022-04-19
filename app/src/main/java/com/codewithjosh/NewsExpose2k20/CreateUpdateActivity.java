package com.codewithjosh.NewsExpose2k20;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.theartofdev.edmodo.cropper.CropImage;

public class CreateUpdateActivity extends AppCompatActivity {

    ImageView close, image_added;
    TextView update;
    EditText subject;

    Uri imageUri;

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

        CropImage.activity()
                .setAspectRatio(1,1)
                .start(CreateUpdateActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            image_added.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(CreateUpdateActivity.this, HomeActivity.class));
            finish();
        }
    }

}