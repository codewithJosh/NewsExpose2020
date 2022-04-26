package com.codewithjosh.NewsExpose2k20;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {

    Button btn_submit;
    EditText et_otp;
    LinearLayout btn_resend, is_loading;
    TextView tv_resend;

    String s_user_contact, s_verification_id;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        btn_submit = findViewById(R.id.btn_submit);
        et_otp = findViewById(R.id.et_otp);
        btn_resend = findViewById(R.id.btn_resend);
        is_loading = findViewById(R.id.is_loading);
        tv_resend = findViewById(R.id.tv_resend);

        s_user_contact = getIntent().getStringExtra("s_user_contact");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        sendVerificationCode();

        btn_resend.setOnClickListener(v -> {

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CommentActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (getCurrentFocus() != null) getCurrentFocus().clearFocus();
            sendVerificationCode();
        });

        btn_submit.setOnClickListener(v -> {

            is_loading.setVisibility(View.VISIBLE);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CommentActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

            final String s_otp = et_otp.getText().toString();

            if (!isConnected()) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
            } else if (s_otp.isEmpty()) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(this, "OTP is required!", Toast.LENGTH_SHORT).show();
            } else if (s_otp.length() != 6) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(this, "OTP must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {

                final PhoneAuthCredential credential = PhoneAuthProvider.getCredential(s_verification_id, s_otp);
                firebaseUser = firebaseAuth.getCurrentUser();

                if (firebaseUser != null) {

                    firebaseUser
                            .linkWithCredential(credential)
                            .addOnSuccessListener(authResult -> {

                                final String s_user_id = firebaseUser.getUid();

                                firebaseFirestore
                                        .collection("Users")
                                        .document(s_user_id)
                                        .update("user_is_verified", true)
                                        .addOnSuccessListener(unused -> {

                                            is_loading.setVisibility(View.GONE);
                                            Toast.makeText(this, "You're Successfully Added!", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(this, LoginActivity.class));
                                            finish();
                                        });

                            }).addOnFailureListener(e -> {

                        final String _e = e.toString().toLowerCase();

                        if (_e.contains("expired")) {
                            is_loading.setVisibility(View.GONE);
                            Toast.makeText(VerificationActivity.this, "OTP has expired", Toast.LENGTH_SHORT).show();
                        } else if (_e.contains("invalid")) {
                            is_loading.setVisibility(View.GONE);
                            Toast.makeText(VerificationActivity.this, "OTP doesn't match", Toast.LENGTH_SHORT).show();
                        } else {
                            is_loading.setVisibility(View.GONE);
                            Toast.makeText(VerificationActivity.this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    // TODO: DEBUG
    private void sendVerificationCode() {

        is_loading.setVisibility(View.VISIBLE);

        if (!isConnected()) {

            is_loading.setVisibility(View.GONE);
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
        } else {

            new CountDownTimer(60000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                    tv_resend.setText(String.valueOf(millisUntilFinished / 1000));
                    btn_resend.setEnabled(false);

                }

                @Override
                public void onFinish() {

                    tv_resend.setText(R.string.text_resend);
                    btn_resend.setEnabled(true);

                }

            }.start();

            PhoneAuthOptions options = PhoneAuthOptions
                    .newBuilder(firebaseAuth)
                    .setPhoneNumber(s_user_contact)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {

                            is_loading.setVisibility(View.GONE);
                            System.out.println(e);
                            Toast.makeText(VerificationActivity.this, "Verification Failed!", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                            is_loading.setVisibility(View.GONE);
                            Toast.makeText(VerificationActivity.this, "You'll receive your OTP shortly", Toast.LENGTH_SHORT).show();
                            VerificationActivity.this.s_verification_id = s;

                        }
                    }).build();

            PhoneAuthProvider.verifyPhoneNumber(options);
        }

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

}