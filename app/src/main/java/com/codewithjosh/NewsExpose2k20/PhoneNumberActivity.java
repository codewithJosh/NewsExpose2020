package com.codewithjosh.NewsExpose2k20;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

public class PhoneNumberActivity extends AppCompatActivity {

    Button btn_next;
    CountryCodePicker ccp_country;
    EditText et_contact;
    LinearLayout is_loading;
    TextView btn_skip;

    String s_user_id, s_user_contact;

    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        btn_next = findViewById(R.id.btn_next);
        ccp_country = findViewById(R.id.ccp_country);
        et_contact = findViewById(R.id.et_contact);
        is_loading = findViewById(R.id.is_loading);
        btn_skip = findViewById(R.id.btn_skip);

        s_user_id = getIntent().getStringExtra("s_user_id");
        s_user_contact = getIntent().getStringExtra("s_user_contact");

        firebaseFirestore = FirebaseFirestore.getInstance();

        et_contact.setText(s_user_contact);

        btn_skip.setOnClickListener(v -> {

            is_loading.setVisibility(View.VISIBLE);

            if (isConnected())

                firebaseFirestore
                    .collection("Users")
                    .document(s_user_id)
                    .update("user_contact", "", "user_is_verified", true)
                    .addOnSuccessListener(unused -> {

                        is_loading.setVisibility(View.GONE);
                        Toast.makeText(this, "Welcome, You've Successfully Login!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    });
            else {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
            }

        });

        ccp_country.registerCarrierNumberEditText(et_contact);

        btn_next.setOnClickListener(v -> {

            is_loading.setVisibility(View.VISIBLE);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CommentActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

            String s_contact = et_contact.getText().toString();

            if (!isConnected()) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
            }
            else if (s_contact.isEmpty()) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(this, "Phone Number is required!", Toast.LENGTH_SHORT).show();
            }
            else if (!s_contact.startsWith("09")) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(this, "Provide a valid Phone Number", Toast.LENGTH_SHORT).show();
            }
            else if (s_contact.length() < 11) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(this, "Phone Number must be at least 11 digits", Toast.LENGTH_SHORT).show();
            }
            else {

                firebaseFirestore
                        .collection("Users")
                        .document(s_user_id)
                        .update("user_contact", s_contact)
                        .addOnSuccessListener(runnable -> {

                            is_loading.setVisibility(View.GONE);

                            Intent intent = new Intent(this, VerificationActivity.class);
                            intent.putExtra("s_user_contact", ccp_country.getFullNumberWithPlus());
                            startActivity(intent);
                            finish();
                        });
            }
        });

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

}