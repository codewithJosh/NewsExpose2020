package com.codewithjosh.NewsExpose2k20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {

    Button btn_submit;
    EditText et_otp;
    LinearLayout btn_resend, is_loading;
    TextView tv_resend;

    String s_user_contact, s_verification_id;

    FirebaseAuth firebaseAuth;

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

        sendVerificationCode();

    }

    // TODO: DEBUG
    private void sendVerificationCode() {

        is_loading.setVisibility(View.VISIBLE);

        if (!isConnected()) {

            is_loading.setVisibility(View.GONE);
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
        else {

            new CountDownTimer(60000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                    tv_resend.setText(String.valueOf(millisUntilFinished/1000));
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