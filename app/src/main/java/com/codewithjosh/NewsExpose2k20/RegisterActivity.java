package com.codewithjosh.NewsExpose2k20;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    final String s_tag = RegisterActivity.class.getSimpleName();
    final String s_site = "6LdmXHcfAAAAAAGqu4EGoI8Ihrk8IB78NdM2cKFJ";
    final String s_secret = "6LdmXHcfAAAAAHMLaAuerAsSOZDkNJYA-gJ8Fma3";
    Button btn_register;
    CheckBox cb_recaptcha;
    ConstraintLayout nav_login, is_loading;
    CountryCodePicker ccp_country;
    EditText et_user_name, et_email, et_contact, et_password, et_re_password;
    int i_version_code;
    String s_user_name, s_email, s_contact, s_password, s_re_password;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

    DatabaseReference databaseRef;
    DocumentReference documentRef;

    RequestQueue req;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        i_version_code = BuildConfig.VERSION_CODE;

        initView();
        initInstance();
        initSharedPref();
        build();

    }

    private void initView() {

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

    }

    private void initInstance() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        req = Volley.newRequestQueue(getApplicationContext());

    }

    private void initSharedPref() {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private void build() {

        nav_login.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btn_register.setOnClickListener(v -> {

            getString();

            if (validate(v)) checkUserName();

            else is_loading.setVisibility(View.GONE);

        });

        ccp_country.registerCarrierNumberEditText(et_contact);

        cb_recaptcha.setOnClickListener(v -> onRecaptcha());

    }

    private void getString() {

        s_user_name = et_user_name.getText().toString().toLowerCase();
        s_email = et_email.getText().toString().toLowerCase();
        s_contact = et_contact.getText().toString();
        s_password = et_password.getText().toString();
        s_re_password = et_re_password.getText().toString();

    }

    private boolean validate(final View v) {

        is_loading.setVisibility(View.VISIBLE);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CommentActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        if (!isConnected())
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        else if (s_user_name.isEmpty() || s_email.isEmpty()
                || s_password.isEmpty() || s_re_password.isEmpty())
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();

        else if (!s_user_name.startsWith("@ne."))
            Toast.makeText(this, "Username must starts with @ne.", Toast.LENGTH_SHORT).show();

        else if (s_user_name.length() < 5)
            Toast.makeText(this, "Provide a valid Username", Toast.LENGTH_SHORT).show();

        else if (!s_email.endsWith("@ne.xpose"))
            Toast.makeText(this, "Email must end with @ne.xpose", Toast.LENGTH_SHORT).show();

        else if (s_email.length() < 10)
            Toast.makeText(this, "Provide a valid Email Address", Toast.LENGTH_SHORT).show();

        else if (!s_contact.startsWith("09"))
            Toast.makeText(this, "Provide a valid Phone Number", Toast.LENGTH_SHORT).show();

        else if (s_contact.length() < 11)
            Toast.makeText(this, "Phone Number must be at least 11 digits", Toast.LENGTH_SHORT).show();

        else if (s_password.length() < 6)
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();

        else if (!s_password.equals(s_re_password))
            Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();

        else if (!cb_recaptcha.isChecked()) onRecaptcha();

        else return true;

        return false;

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
                            && !recaptchaTokenResponse.getTokenResult().isEmpty())

                        handleSiteVerify(recaptchaTokenResponse.getTokenResult());

                }).addOnFailureListener(e -> {

            if (e instanceof ApiException) {

                ApiException apiException = (ApiException) e;
                Log.d(s_tag, "Error message: " + CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));
            } else Log.d(s_tag, "Unknown type of error: " + e.getMessage());

        });

    }

    protected void handleSiteVerify(final String s_tokenResult) {

        final String s_url = "https://www.google.com/recaptcha/api/siteverify";

        StringRequest sr_req = new StringRequest(Request.Method.POST, s_url,
                response -> {

                    try {

                        JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.getBoolean("success")) {

                            cb_recaptcha.setTextColor(getResources().getColor(R.color.colorFulvous));
                            cb_recaptcha.setChecked(true);
                            cb_recaptcha.setClickable(false);
                        } else
                            Toast.makeText(getApplicationContext(), jsonObject.getString("error-codes"), Toast.LENGTH_LONG).show();

                    } catch (Exception ex) {

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

    private void checkUserName() {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_name", s_user_name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots != null)

                        if (!queryDocumentSnapshots.isEmpty()) {

                            is_loading.setVisibility(View.GONE);
                            Toast.makeText(this, "Username is Already Taken!", Toast.LENGTH_SHORT).show();
                        } else checkContact();

                });

    }

    private void checkContact() {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_contact", s_contact)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots != null)

                        if (!queryDocumentSnapshots.isEmpty()) {

                            is_loading.setVisibility(View.GONE);
                            Toast.makeText(this, "Phone Number is Unavailable!", Toast.LENGTH_SHORT).show();
                        } else onRegister();

                });

    }

    private void onRegister() {

        firebaseAuth
                .createUserWithEmailAndPassword(s_email, s_password)
                .addOnSuccessListener(authResult -> {

                    firebaseUser = firebaseAuth.getCurrentUser();

                    if (firebaseUser != null) {

                        final String s_user_bio = "";
                        final String s_user_id = firebaseUser.getUid();
                        final String s_user_image = "https://firebasestorage.googleapis.com/v0/b/news-expose-2k20.appspot.com/o/20220415_Res%2FDefaultUserImage.png?alt=media&token=4cdbad29-194b-410e-80fa-feb641a06998";
                        final boolean user_is_admin = false;
                        final boolean user_is_verified = false;

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

                        setUserFirestore(s_user_id, user);

                    }

                }).addOnFailureListener(e -> {

            is_loading.setVisibility(View.GONE);

            final String s_e = e.toString().toLowerCase();

            if (s_e.contains("the email address is already in use by another account"))
                Toast.makeText(this, "Email is Already Exist!", Toast.LENGTH_SHORT).show();

            else if (s_e.contains("a network error (such as timeout, interrupted connection or unreachable host) has occurred"))
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

            else
                Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();

        });

    }

    private void setUserFirestore(final String s_user_id, final UserModel user) {

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

                                        final HashMap<String, Object> _user = new HashMap<>();
                                        _user.put("user_email", s_email);
                                        _user.put("user_name", s_user_name);
                                        _user.put("user_version_code", i_version_code);

                                        setUserRealtime(s_user_id, _user);
                                    })
                                    .addOnFailureListener(e -> {

                                        is_loading.setVisibility(View.GONE);
                                        Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();
                                    });
                        }
                });

    }

    private void setUserRealtime(final String s_user_id, final HashMap<String, Object> user) {

        databaseRef = firebaseDatabase
                .getReference("Users")
                .child(s_user_id);

        databaseRef
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot != null)

                        if (!documentSnapshot.exists()) {

                            databaseRef
                                    .setValue(user)
                                    .addOnSuccessListener(unused -> {

                                        editor.putString("s_user_contact", ccp_country.getFullNumberWithPlus());
                                        editor.apply();
                                        startActivity(new Intent(this, VerificationActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {

                                        is_loading.setVisibility(View.GONE);
                                        Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();
                                    });
                        }
                });

    }

}
