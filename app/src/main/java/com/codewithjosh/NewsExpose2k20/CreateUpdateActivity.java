package com.codewithjosh.NewsExpose2k20;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.codewithjosh.NewsExpose2k20.models.UpdateModel;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class CreateUpdateActivity extends AppCompatActivity {

    EditText et_update_content;
    ImageButton btn_back, btn_create_update;
    ImageView iv_update_image;
    LinearLayout is_loading;

    String s_user_id, s_update_content, s_update_image;

    UploadTask uTask;
    Uri uri;

    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;

    DocumentReference documentRef;
    StorageReference storageRef;

    SharedPreferences sharedPref;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseFirestore = FirebaseFirestore.getInstance();

        load();
        loadUserBio(s_user_id);

    }

    private void load() {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);

        s_user_id = sharedPref.getString("s_user_id", String.valueOf(MODE_PRIVATE));

    }

    private void loadUserBio(final String s_user_id) {

        firebaseFirestore
                .collection("Users")
                .document(s_user_id)
                .addSnapshotListener((value, error) -> {

                    if (value != null) {

                        final UserModel user = value.toObject(UserModel.class);

                        if (user != null) {

                            final String s_user_bio = user.getUser_bio();
                            final String s_hint = "What's on your mind, " + s_user_bio + "?";
                            final String s_hint_empty = "Add a caption…";

                            if (!s_user_bio.isEmpty()) et_update_content.setHint(s_hint);

                            else et_update_content.setHint(s_hint_empty);

                        }
                    }
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_update);

        initViews();
        initInstance();
        load();
        build();

    }

    private void initViews() {

        et_update_content = findViewById(R.id.et_update_content);
        btn_back = findViewById(R.id.btn_back);
        btn_create_update = findViewById(R.id.btn_create_update);
        iv_update_image = findViewById(R.id.iv_update_image);
        is_loading = findViewById(R.id.is_loading);

    }

    private void initInstance() {

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

    }

    private void build() {

        btn_back.setOnClickListener(v -> onBackPressed());

        btn_create_update.setOnClickListener(v -> {

            is_loading.setVisibility(View.VISIBLE);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CommentActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

            s_update_content = et_update_content.getText().toString().trim();
            if (isConnected() && uri != null) onCreateUpdate();
            else {

                is_loading.setVisibility(View.GONE);
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
            }
        });

        getImage();

        iv_update_image.setOnClickListener(v -> getImage());

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void onCreateUpdate() {

        final Random ran = new Random();
        final String s_file_extension = getFileExtension(uri);

        if (s_file_extension != null) {

            final int i_file_extension = s_file_extension.lastIndexOf(".");

            storageRef = firebaseStorage
                    .getReference("Updates")
                    .child(ran.nextInt(999999999)
                            + s_file_extension.substring(i_file_extension));
        }

        uTask = storageRef.putFile(uri);

        uTask.continueWithTask(task -> {

            if (!task.isSuccessful()) throw task.getException();

            return storageRef.getDownloadUrl();

        }).addOnSuccessListener(uri -> {

            final String s_update_id = firebaseFirestore
                    .collection("Updates")
                    .document()
                    .getId();

            if (uri != null) s_update_image = uri.toString();
            final Calendar calendar = Calendar.getInstance();
            final Date date_update_timestamp = calendar.getTime();

            final UpdateModel update = new UpdateModel(
                    s_update_id,
                    s_update_image,
                    s_update_content,
                    date_update_timestamp,
                    s_user_id
            );

            onUpdate(s_update_id, update);

        });

    }

    private String getFileExtension(final Uri uri) {

        final String result = uri.getPath();
        int cut = result.lastIndexOf('/');
        if (cut != -1) return result.substring(cut + 1);
        return null;

    }

    private void onUpdate(final String s_update_id, final UpdateModel update) {

        documentRef = firebaseFirestore
                .collection("Updates")
                .document(s_update_id);

        documentRef.addSnapshotListener((value, error) -> {

            if (value != null)

                if (!value.exists())

                    documentRef
                            .set(update)
                            .addOnSuccessListener(unused -> {

                                is_loading.setVisibility(View.GONE);
                                onBackPressed();
                            });
        });

    }

    private void getImage() {

        CropImage
                .activity()
                .start(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            final CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (result != null) uri = result.getUri();
            iv_update_image.setImageURI(uri);
        } else onBackPressed();

    }

}