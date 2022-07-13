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

    public static final int cameraRequest = 99;
    public static final int storageRequest = 100;
    ImageButton navHome;
    ImageButton navProfile;
    ImageButton navCreateUpdate;
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

        final String userId = sharedPref.getString("user_id", String.valueOf(MODE_PRIVATE));

        checkUserAdmin(userId);

    }

    private void checkUserAdmin(final String userId) {

        firebaseFirestore
                .collection("Users")
                .document(userId)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null)
                    {

                        final UserModel user = value.toObject(UserModel.class);

                        if (user != null)
                        {

                            final boolean userIsAdmin = user.isUser_is_admin();

                            if (userIsAdmin) navCreateUpdate.setVisibility(View.VISIBLE);

                            else navCreateUpdate.setVisibility(View.GONE);

                        }
                        
                    }
                    
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        build();

    }

    private void initViews() {

        cameraPermission = new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };
        storagePermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };

        navHome = findViewById(R.id.nav_home);
        navProfile = findViewById(R.id.nav_profile);
        navCreateUpdate = findViewById(R.id.nav_create_update);

    }

    private void build() {

        navCreateUpdate.setOnClickListener(v -> onCreateUpdate());

        navHome.setOnClickListener(v -> 
        {
            
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame, new HomeFragment())
                    .commit();
            
            onStart();
            
        });

        navProfile.setOnClickListener(v -> 
        {
            
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame, new ProfileFragment())
                    .commit();
            
            navCreateUpdate.setVisibility(View.GONE);
            
        });

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, new HomeFragment())
                .commit();

    }

    private void onCreateUpdate() {

        if (!checkCameraPermission()) requestCameraPermission();

        else if (!checkStoragePermission()) requestStoragePermission();

        else startActivity(new Intent(this, CreateUpdateActivity.class));

    }

    private boolean checkCameraPermission() {

        final boolean isCameraGranted = checkSelfPermission(Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        final boolean isStorageGranted = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return isCameraGranted && isStorageGranted;

    }

    private boolean checkStoragePermission() {
        
        return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        
    }

    private void requestCameraPermission() {
        
        requestPermissions(cameraPermission, cameraRequest);
        
    }

    private void requestStoragePermission() {
        
        requestPermissions(storagePermission, storageRequest);
        
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) 
        {

            case cameraRequest:

                if (grantResults.length > 0) 
                {

                    final boolean cameraAccepted = grantResults[0] == (PackageManager.PERMISSION_GRANTED);
                    final boolean storageAccepted = grantResults[1] == (PackageManager.PERMISSION_GRANTED);

                    if (cameraAccepted && storageAccepted) onCreateUpdate();

                    else Toast.makeText(this, "Please enable camera and storage permission", Toast.LENGTH_SHORT).show();

                }
                break;

            case storageRequest:

                if (grantResults.length > 0) 
                {

                    final boolean storageAccepted = grantResults[0] == (PackageManager.PERMISSION_GRANTED);

                    if (storageAccepted) onCreateUpdate();

                    else Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_SHORT).show();

                }
                break;

        }

    }

}