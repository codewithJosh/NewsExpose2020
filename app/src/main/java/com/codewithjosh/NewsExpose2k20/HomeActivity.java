package com.codewithjosh.NewsExpose2k20;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.codewithjosh.NewsExpose2k20.fragments.HomeFragment;
import com.codewithjosh.NewsExpose2k20.fragments.ProfileFragment;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    CircleImageView nav_profile;
    ImageButton nav_home, nav_create_update;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    DatabaseReference databaseRef;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        final String s_user_id = firebaseAuth.getCurrentUser().getUid();

        databaseRef = firebaseDatabase
                .getReference()
                .child("Users")
                .child(s_user_id);

//        TODO: USE GET METHOD ONCE IT IS AVAILABLE
        databaseRef
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child("admin").getValue().equals(true))
                            nav_create_update.setVisibility(View.VISIBLE);
                        else nav_create_update.setVisibility(View.GONE);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        nav_profile = findViewById(R.id.nav_profile);
        nav_home = findViewById(R.id.nav_home);
        nav_create_update = findViewById(R.id.nav_create_update);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        final String s_user_id = firebaseAuth.getCurrentUser().getUid();

        databaseRef = firebaseDatabase
                .getReference()
                .child("Users")
                .child(s_user_id);

//        TODO: USE GET METHOD ONCE IT IS AVAILABLE
        databaseRef
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
//                        TODO: FOUND ISSUE: UPDATE THE CREDENTIALS
                        Glide.with(getApplicationContext()).load(userModel.getImageurl()).into(nav_profile);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        nav_create_update.setOnClickListener(v -> startActivity(new Intent(this, CreateUpdateActivity.class)));

        nav_home.setOnClickListener(v -> getSupportFragmentManager().beginTransaction().replace(R.id.frame,
                new HomeFragment()).commit());

        nav_profile.setOnClickListener(v -> getSupportFragmentManager().beginTransaction().replace(R.id.frame,
                new ProfileFragment()).commit());

        getSupportFragmentManager().beginTransaction().replace(R.id.frame,
                new HomeFragment()).commit();

    }

}