package com.codewithjosh.NewsExpose2k20;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    Button btn_register;
    CheckBox cb_recaptcha;
    CountryCodePicker ccp_country;
    EditText et_user_name, et_email, et_contact, et_password, et_re_password;
    LinearLayout nav_login, is_loading;

    final String s_tag = RegisterActivity.class.getSimpleName();
    final String s_site = "6LdmXHcfAAAAAAGqu4EGoI8Ihrk8IB78NdM2cKFJ";
    final String s_secret = "6LdmXHcfAAAAAHMLaAuerAsSOZDkNJYA-gJ8Fma3";

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore firebaseFirestore;

    DatabaseReference databaseRef;
    DocumentReference documentRef;

    RequestQueue req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btn_register = findViewById(R.id.btn_register);
        cb_recaptcha = findViewById(R.id.cb_recaptcha);
        ccp_country = findViewById(R.id.ccp_country);
        et_user_name = findViewById(R.id.et_user_name);
        et_email = findViewById(R.id.et_email);
        et_contact = findViewById(R.id.et_contact);
        et_password = findViewById(R.id.et_password);
        et_re_password = findViewById(R.id.et_re_password);
        nav_login = findViewById(R.id.nav_login);
        is_loading = findViewById(R.id.is_loading);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        nav_login.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btn_register.setOnClickListener(v -> {

            is_loading.setVisibility(View.VISIBLE);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CommentActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

            final String s_user_name = et_user_name.getText().toString().toLowerCase();
            final String s_email = et_email.getText().toString().toLowerCase();
            final String s_contact = et_contact.getText().toString();
            final String s_password = et_password.getText().toString();
            final String s_re_password = et_re_password.getText().toString();

            if (!isConnected()) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
            } else if (s_user_name.isEmpty() || s_email.isEmpty()
                    || s_password.isEmpty() || s_re_password.isEmpty()) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else if (!s_user_name.startsWith("@ne.")) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Username must starts with @ne.", Toast.LENGTH_SHORT).show();
            } else if (s_user_name.length() < 5) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Provide a valid Username", Toast.LENGTH_SHORT).show();
            } else if (!s_email.endsWith("@ne.xpose")) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Email must end with @ne.xpose", Toast.LENGTH_SHORT).show();
            } else if (s_email.length() < 10) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Provide a valid Email Address", Toast.LENGTH_SHORT).show();
            } else if (!s_contact.startsWith("09")) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Provide a valid Phone Number", Toast.LENGTH_SHORT).show();
            } else if (s_contact.length() < 11) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Phone Number must be at least 11 digits", Toast.LENGTH_SHORT).show();
            } else if (s_password.length() < 6) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else if (!s_password.equals(s_re_password)) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Password doesn't match", Toast.LENGTH_SHORT).show();
            }
            else if (!cb_recaptcha.isChecked()) {
                is_loading.setVisibility(View.GONE);
                onRecaptcha();
            }
            else onRegister(s_user_name, s_email, s_contact, s_password);

        });

        ccp_country.registerCarrierNumberEditText(et_contact);

        req = Volley.newRequestQueue(getApplicationContext());

        cb_recaptcha.setOnClickListener(v -> onRecaptcha());

    }

    private void onRegister(final String s_user_name, final String s_email, final String s_contact, final String s_password) {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_name", s_user_name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots.isEmpty()) {

                        firebaseAuth
                                .createUserWithEmailAndPassword(s_email, s_password)
                                .addOnSuccessListener(authResult -> {

                                    final String s_user_bio = "";
                                    final String s_user_id = authResult.getUser().getUid();
                                    final String s_user_image = "https://firebasestorage.googleapis.com/v0/b/news-expose-2k20.appspot.com/o/20220415_Res%2FDefaultUserImage.png?alt=media&token=4cdbad29-194b-410e-80fa-feb641a06998";
                                    final boolean user_is_admin = false;
                                    final boolean user_is_verified = false;
                                    final int i_version_code = BuildConfig.VERSION_CODE;

                                    final UserModel user = new UserModel(
                                            s_user_bio,
                                            s_contact,
                                            s_email,
                                            s_user_id,
                                            s_user_image,
                                            user_is_admin,
                                            user_is_verified,
                                            s_user_name,
                                            i_version_code
                                    );

                                    documentRef = firebaseFirestore
                                            .collection("Users")
                                            .document(s_user_id);

                                    documentRef
                                            .get()
                                            .addOnSuccessListener(documentSnapshot -> {

                                                if (documentSnapshot != null)

                                                    if (!documentSnapshot.exists()) {

                                                        documentRef
                                                                .set(user)
                                                                .addOnSuccessListener(unused -> {

                                                                    HashMap<String, Object> support = new HashMap<>();
                                                                    support.put("user_email", s_email);
                                                                    support.put("user_name", s_user_name);
                                                                    support.put("user_version_code", i_version_code);

                                                                    databaseRef = firebaseDatabase
                                                                            .getReference("Users")
                                                                            .child(s_user_id);

                                                                    databaseRef
                                                                            .get()
                                                                            .addOnSuccessListener(dataSnapshot -> {

                                                                                if (!dataSnapshot.exists()) {

                                                                                    databaseRef
                                                                                            .setValue(support)
                                                                                            .addOnSuccessListener(_unused -> {

                                                                                                is_loading.setVisibility(View.GONE);

                                                                                                Intent intent = new Intent(this, VerificationActivity.class);
                                                                                                intent.putExtra("s_user_contact", ccp_country.getFullNumberWithPlus());
                                                                                                startActivity(intent);
                                                                                            });
                                                                                }
                                                                            });


                                                                });
                                                    }
                                            });

                                }).addOnFailureListener(e -> {

                            if (e.toString().contains("The email address is already in use by another account")) {
                                is_loading.setVisibility(View.GONE);
                                Toast.makeText(this, "Email is Already Exist!", Toast.LENGTH_SHORT).show();
                            } else if (e.toString().contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred")) {
                                is_loading.setVisibility(View.GONE);
                                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                            } else {
                                is_loading.setVisibility(View.GONE);
                                Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        is_loading.setVisibility(View.GONE);
                        Toast.makeText(this, "Username is Already Taken!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void onRecaptcha() {

        cb_recaptcha.setChecked(false);

        SafetyNet
                .getClient(this)
                .verifyWithRecaptcha(s_site)
                .addOnSuccessListener(recaptchaTokenResponse -> {

                    if (recaptchaTokenResponse.getTokenResult() != null
                            && !recaptchaTokenResponse.getTokenResult().isEmpty()) {

                        handleSiteVerify(recaptchaTokenResponse.getTokenResult());
                    }

                }).addOnFailureListener(e -> {

            if (e instanceof ApiException) {

                ApiException apiException = (ApiException) e;
                Log.d(s_tag, "Error message: " + CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));
            }
            else Log.d(s_tag, "Unknown type of error: " + e.getMessage());

        });

    }

    protected void handleSiteVerify(final String s_tokenResult) {

        final String s_url = "https://www.google.com/recaptcha/api/siteverify";

        StringRequest sr_req = new StringRequest(Request.Method.POST, s_url,
                response -> {

                    try {

                        JSONObject jsonObject = new JSONObject(response);

                        if(jsonObject.getBoolean("success")){

                            cb_recaptcha.setTextColor(getResources().getColor(R.color.colorFulvous));
                            cb_recaptcha.setChecked(true);
                            cb_recaptcha.setClickable(false);
                        }
                        else Toast.makeText(getApplicationContext(), jsonObject.getString("error-codes"), Toast.LENGTH_LONG).show();

                    }
                    catch (Exception ex) {

                        Log.d(s_tag, "JSON exception: " + ex.getMessage());
                    }
                },
                error -> Log.d(s_tag, "Error message: " + error.getMessage())) {

            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                params.put("secret", s_secret);
                params.put("response", s_tokenResult);
                return params;

            }
        };

        sr_req.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        req.add(sr_req);

    }

}
