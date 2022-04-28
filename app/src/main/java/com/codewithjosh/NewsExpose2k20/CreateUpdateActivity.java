package com.codewithjosh.NewsExpose2k20;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.codewithjosh.NewsExpose2k20.models.UpdateModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

public class CreateUpdateActivity extends AppCompatActivity {

    EditText et_update_content;
    ImageButton btn_back, btn_create_update;
    ImageView iv_update_image;

    String s_update_image;
    UploadTask uTask;
    Uri uri;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;

    DatabaseReference databaseRef;
    StorageReference storageRef;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_update);

        et_update_content = findViewById(R.id.et_update_content);
        btn_back = findViewById(R.id.btn_back);
        btn_create_update = findViewById(R.id.btn_create_update);
        iv_update_image = findViewById(R.id.iv_update_image);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        databaseRef = firebaseDatabase.getReference("Updates");
        storageRef = firebaseStorage.getReference("Updates");

        btn_back.setOnClickListener(v -> onBackPressed());

        btn_create_update.setOnClickListener(v -> onUpdate());

        CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this);

    }

    private void onUpdate() {
        pd = new ProgressDialog(this);
        pd.setMessage("Updating");
        pd.show();

        if (uri != null) {

            final StorageReference _storageRef = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(uri));

            uTask = _storageRef.putFile(uri);
            uTask.continueWithTask(task -> {

                if (!task.isSuccessful()) throw task.getException();
                return _storageRef.getDownloadUrl();

            }).addOnSuccessListener(uri -> {

                if (uri != null) s_update_image = uri.toString();

                final String s_update_id = databaseRef.push().getKey();
                final String s_update_content = et_update_content.getText().toString();
                final String s_user_id = firebaseAuth.getCurrentUser().getUid();
                final int i_version_code = BuildConfig.VERSION_CODE;

                final UpdateModel update = new UpdateModel(
                        s_update_id,
                        s_update_image,
                        s_update_content,
                        s_user_id,
                        i_version_code
                );

                if (s_update_id != null)

                    databaseRef
                            .child(s_update_id)
                            .setValue(update)
                            .addOnSuccessListener(runnable -> {

                                pd.dismiss();
                                onBackPressed();
                            });

            });
        } else Toast.makeText(this, "No Image Selected!", Toast.LENGTH_SHORT).show();

    }

    private String getFileExtension(Uri uri) {

        final String result = uri.getPath();
        int cut = result.lastIndexOf('/');
        if (cut != -1) return result.substring(cut + 1);
        return null;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            uri = result.getUri();
            iv_update_image.setImageURI(uri);
        } else {

            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }

    }

}