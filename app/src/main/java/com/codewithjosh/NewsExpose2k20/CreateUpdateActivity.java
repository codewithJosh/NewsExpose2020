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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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

    ConstraintLayout isLoading;
    EditText etUpdateContent;
    ImageButton btnBack;
    ImageButton btnCreateUpdate;
    ImageView ivUpdateImage;
    String userId;
    String updateContent;
    String updateImage;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;
    DocumentReference documentRef;
    SharedPreferences sharedPref;
    StorageReference storageRef;
    UploadTask uTask;
    Uri uri;

    @Override
    protected void onStart() {

        super.onStart();

        firebaseFirestore = FirebaseFirestore.getInstance();

        load();
        loadUserBio(userId);

    }

    private void load() {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);
        userId = sharedPref.getString("user_id", String.valueOf(MODE_PRIVATE));

    }

    private void loadUserBio(final String userId) {

        firebaseFirestore
                .collection("Users")
                .document(userId)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        final UserModel user = value.toObject(UserModel.class);

                        if (user != null) {

                            final String userBio = user.getUser_bio();

                            etUpdateContent.setHint(
                                    !userBio.isEmpty()
                                            ? "What's on your mind, %?".replace("%", userBio)
                                            : getString(R.string.hint_update_content)
                            );

                        }

                    }

                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_update);

        initViews();
        initInstances();
        load();
        build();

    }

    private void initViews() {

        etUpdateContent = findViewById(R.id.et_update_content);
        btnBack = findViewById(R.id.btn_back);
        btnCreateUpdate = findViewById(R.id.btn_create_update);
        ivUpdateImage = findViewById(R.id.iv_update_image);
        isLoading = findViewById(R.id.is_loading);

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

    }

    private void build() {

        btnBack.setOnClickListener(v -> onBackPressed());

        btnCreateUpdate.setOnClickListener(v ->
        {

            isLoading.setVisibility(View.VISIBLE);
            final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

            updateContent = etUpdateContent.getText().toString().trim();
            if (isConnected() && uri != null) onCreateUpdate();

            else {

                isLoading.setVisibility(View.GONE);
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

            }

        });

        getImage();

        ivUpdateImage.setOnClickListener(v -> getImage());

    }

    private boolean isConnected() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void onCreateUpdate() {

        final Random ran = new Random();
        final String fileExtension = getFileExtension(uri);

        if (fileExtension != null) {

            final int _fileExtension = fileExtension.lastIndexOf(".");

            storageRef = firebaseStorage
                    .getReference("Updates")
                    .child(ran.nextInt(999999999)
                            + fileExtension.substring(_fileExtension));

        }

        uTask = storageRef.putFile(uri);
        uTask.continueWithTask(task ->
        {

            if (!task.isSuccessful()) throw task.getException();

            return storageRef.getDownloadUrl();

        }).addOnSuccessListener(uri ->
        {

            final String updateId = firebaseFirestore
                    .collection("Updates")
                    .document()
                    .getId();

            if (uri != null) updateImage = String.valueOf(uri);
            final Calendar calendar = Calendar.getInstance();
            final Date updateTimestamp = calendar.getTime();

            final UpdateModel update = new UpdateModel(
                    updateId,
                    updateImage,
                    updateContent,
                    updateTimestamp,
                    userId
            );

            onUpdate(updateId, update);

        });

    }

    private String getFileExtension(final Uri uri) {

        final String result = uri.getPath();
        final int cut = result.lastIndexOf('/');
        if (cut != -1) return result.substring(cut + 1);
        return null;

    }

    private void onUpdate(final String updateId, final UpdateModel update) {

        documentRef = firebaseFirestore
                .collection("Updates")
                .document(updateId);

        documentRef.addSnapshotListener((value, error) ->
        {

            if (value != null && !value.exists())

                documentRef
                        .set(update)
                        .addOnSuccessListener(unused ->
                        {

                            isLoading.setVisibility(View.GONE);
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
            ivUpdateImage.setImageURI(uri);

        } else onBackPressed();

    }

}