package com.codewithjosh.NewsExpose2k20;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.codewithjosh.NewsExpose2k20.fragments.HomeFragment;
import com.codewithjosh.NewsExpose2k20.fragments.ProfileFragment;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    ImageButton nav_home, nav_profile, nav_create_update;

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

        initViews();
        build();

    }

    private void initViews() {

        nav_home = findViewById(R.id.nav_home);
        nav_profile = findViewById(R.id.nav_profile);
        nav_create_update = findViewById(R.id.nav_create_update);

    }

    private void build() {

        nav_create_update.setOnClickListener(v -> startActivity(new Intent(this, CreateUpdateActivity.class)));

        nav_home.setOnClickListener(v -> getSupportFragmentManager().beginTransaction().replace(R.id.frame,
                new HomeFragment()).commit());

        nav_profile.setOnClickListener(v -> getSupportFragmentManager().beginTransaction().replace(R.id.frame,
                new ProfileFragment()).commit());

        getSupportFragmentManager().beginTransaction().replace(R.id.frame,
                new HomeFragment()).commit();

    }

}