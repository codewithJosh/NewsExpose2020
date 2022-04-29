package com.codewithjosh.NewsExpose2k20;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.codewithjosh.NewsExpose2k20.fragments.HomeFragment;
import com.codewithjosh.NewsExpose2k20.fragments.ProfileFragment;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    public static final int CAMERA_REQUEST = 99;
    public static final int STORAGE_REQUEST = 100;

    ImageButton nav_home, nav_profile, nav_create_update;

    String[] cameraPermission;
    String[] storagePermission;

    FirebaseFirestore firebaseFirestore;

    SharedPreferences sharedPref;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseFirestore = FirebaseFirestore.getInstance();

        load();

    }

    private void load() {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);

        final String s_user_id = sharedPref.getString("s_user_id", String.valueOf(MODE_PRIVATE));

        checkUserAdmin(s_user_id);

    }

    private void checkUserAdmin(String s_user_id) {

        firebaseFirestore
                .collection("Users")
                .document(s_user_id)
                .addSnapshotListener((value, error) -> {

                    if (value != null) {

                        final UserModel user = value.toObject(UserModel.class);

                        if (user != null) {

                            final boolean user_is_admin = user.isUser_is_admin();

                            if (user_is_admin) nav_create_update.setVisibility(View.VISIBLE);

                            else nav_create_update.setVisibility(View.GONE);

                        }
                    }
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        initViews();
        build();

    }

    private void initViews() {

        nav_home = findViewById(R.id.nav_home);
        nav_profile = findViewById(R.id.nav_profile);
        nav_create_update = findViewById(R.id.nav_create_update);

    }

    private void build() {

        nav_create_update.setOnClickListener(v -> onCreateUpdate());

        nav_home.setOnClickListener(v -> getSupportFragmentManager().beginTransaction().replace(R.id.frame,
                new HomeFragment()).commit());

        nav_profile.setOnClickListener(v -> getSupportFragmentManager().beginTransaction().replace(R.id.frame,
                new ProfileFragment()).commit());

        getSupportFragmentManager().beginTransaction().replace(R.id.frame,
                new HomeFragment()).commit();

    }

    private void onCreateUpdate() {

        if (!checkCameraPermission()) requestCameraPermission();

        else if (!checkStoragePermission()) requestStoragePermission();

        else startActivity(new Intent(this, CreateUpdateActivity.class));

    }

    private boolean checkCameraPermission() {

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean _result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && _result;

    }

    private boolean checkStoragePermission() { return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED); }

    private void requestCameraPermission() { requestPermissions(cameraPermission, CAMERA_REQUEST); }

    private void requestStoragePermission() { requestPermissions(storagePermission, STORAGE_REQUEST); }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){

            case CAMERA_REQUEST:

                if (grantResults.length > 0) {

                    boolean camera_accepted = grantResults[0] == (PackageManager.PERMISSION_GRANTED);
                    boolean storage_accepted = grantResults[1] == (PackageManager.PERMISSION_GRANTED);

                    if (camera_accepted && storage_accepted) onCreateUpdate();

                    else Toast.makeText(this, "Please enable camera and storage permission", Toast.LENGTH_SHORT).show();

                }
                break;

            case STORAGE_REQUEST:

                if (grantResults.length > 0) {

                    boolean storage_accepted = grantResults[0] == (PackageManager.PERMISSION_GRANTED);

                    if (storage_accepted) onCreateUpdate();

                    else Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_SHORT).show();

                }
                break;

        }

    }

}