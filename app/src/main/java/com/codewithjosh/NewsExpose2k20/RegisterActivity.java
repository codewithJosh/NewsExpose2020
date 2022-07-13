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

    final String tag = RegisterActivity.class.getSimpleName();
    final String site = "6LdmXHcfAAAAAAGqu4EGoI8Ihrk8IB78NdM2cKFJ";
    final String secret = "6LdmXHcfAAAAAHMLaAuerAsSOZDkNJYA-gJ8Fma3";
    Button btnRegister;
    CheckBox cbRecaptcha;
    ConstraintLayout navLogin;
    ConstraintLayout isLoading;
    CountryCodePicker ccpCountry;
    EditText etUserName;
    EditText etEmail;
    EditText etContact;
    EditText etPassword;
    EditText etRePassword;
    int versionCode;
    String userName;
    String email;
    String contact;
    String password;
    String rePassword;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;
    DatabaseReference databaseRef;
    DocumentReference documentRef;
    RequestQueue requestQueue;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        versionCode = BuildConfig.VERSION_CODE;

        initViews();
        initInstances();
        initSharedPref();
        build();

    }

    private void initViews() {

        btnRegister = findViewById(R.id.btn_register);
        cbRecaptcha = findViewById(R.id.cb_recaptcha);
        ccpCountry = findViewById(R.id.ccp_country);
        etUserName = findViewById(R.id.et_user_name);
        etEmail = findViewById(R.id.et_email);
        etContact = findViewById(R.id.et_contact);
        etPassword = findViewById(R.id.et_password);
        etRePassword = findViewById(R.id.et_re_password);
        navLogin = findViewById(R.id.nav_login);
        isLoading = findViewById(R.id.is_loading);

    }

    private void initInstances() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        requestQueue = Volley.newRequestQueue(this);

    }

    private void initSharedPref() {

        editor = getSharedPreferences("user", MODE_PRIVATE).edit();

    }

    private void build() {

        navLogin.setOnClickListener(v ->
        {

            startActivity(new Intent(this, LoginActivity.class));
            finish();

        });

        btnRegister.setOnClickListener(v ->
        {

            getString();

            if (validate(v)) checkUserName();

            else isLoading.setVisibility(View.GONE);

        });

        ccpCountry.registerCarrierNumberEditText(etContact);

        cbRecaptcha.setOnClickListener(v -> onRecaptcha());

    }

    private void getString() {

        userName = etUserName.getText().toString().toLowerCase();
        email = etEmail.getText().toString().toLowerCase();
        contact = etContact.getText().toString();
        password = etPassword.getText().toString();
        rePassword = etRePassword.getText().toString();

    }

    private boolean validate(final View v) {

        isLoading.setVisibility(View.VISIBLE);
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        if (!isConnected()) Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        else if (userName.isEmpty()
                || email.isEmpty()
                || password.isEmpty()
                || rePassword.isEmpty())
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();

        else if (!userName.startsWith("@ne.")) Toast.makeText(this, "Username must starts with @ne.", Toast.LENGTH_SHORT).show();

        else if (userName.length() < 5) Toast.makeText(this, "Provide a valid Username", Toast.LENGTH_SHORT).show();

        else if (!email.endsWith("@ne.xpose")) Toast.makeText(this, "Email must end with @ne.xpose", Toast.LENGTH_SHORT).show();

        else if (email.length() < 10) Toast.makeText(this, "Provide a valid Email Address", Toast.LENGTH_SHORT).show();

        else if (!contact.startsWith("09")) Toast.makeText(this, "Provide a valid Phone Number", Toast.LENGTH_SHORT).show();

        else if (contact.length() < 11) Toast.makeText(this, "Phone Number must be at least 11 digits", Toast.LENGTH_SHORT).show();

        else if (password.length() < 6) Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();

        else if (!password.equals(rePassword)) Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();

        else if (!cbRecaptcha.isChecked()) onRecaptcha();

        else return true;

        return false;

    }

    private boolean isConnected() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void onRecaptcha() {

        cbRecaptcha.setChecked(false);

        SafetyNet
                .getClient(this)
                .verifyWithRecaptcha(site)
                .addOnSuccessListener(recaptchaTokenResponse -> {

                    if (recaptchaTokenResponse.getTokenResult() != null
                            && !recaptchaTokenResponse.getTokenResult().isEmpty())

                        handleSiteVerify(recaptchaTokenResponse.getTokenResult());

                }).addOnFailureListener(e ->
                {

                    if (e instanceof ApiException)
                    {

                        final ApiException apiException = (ApiException) e;
                        Log.d(tag, "Error message: " + CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));

                    }
                    else Log.d(tag, "Unknown type of error: " + e.getMessage());

                });

    }

    protected void handleSiteVerify(final String tokenResult) {

        final String url = "https://www.google.com/recaptcha/api/siteverify";

        final StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response ->
                {

                    try
                    {

                        final JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.getBoolean("success"))
                        {

                            cbRecaptcha.setTextColor(getColor(R.color.color_fulvous));
                            cbRecaptcha.setChecked(true);
                            cbRecaptcha.setClickable(false);

                        }
                        else Toast.makeText(this, jsonObject.getString("error-codes"), Toast.LENGTH_LONG).show();

                    }
                    catch (Exception ex)
                    {

                        Log.d(tag, "JSON exception: " + ex.getMessage());

                    }

                },
                error -> Log.d(tag, "Error message: " + error.getMessage())) {

            @Override
            protected Map<String, String> getParams()
            {

                final Map<String, String> params = new HashMap<>();
                params.put("secret", secret);
                params.put("response", tokenResult);
                return params;

            }

        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(stringRequest);

    }

    private void checkUserName() {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_name", userName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                {

                    if (queryDocumentSnapshots != null)

                        if (queryDocumentSnapshots.isEmpty()) checkContact();

                        else {

                            isLoading.setVisibility(View.GONE);
                            Toast.makeText(this, "Username is Already Taken!", Toast.LENGTH_SHORT).show();

                        }

                });

    }

    private void checkContact() {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_contact", contact)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                {

                    if (queryDocumentSnapshots != null)

                        if (queryDocumentSnapshots.isEmpty()) onRegister();

                        else
                        {

                            isLoading.setVisibility(View.GONE);
                            Toast.makeText(this, "Phone Number is Unavailable!", Toast.LENGTH_SHORT).show();

                        }

                });

    }

    private void onRegister() {

        firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult ->
                {

                    firebaseUser = firebaseAuth.getCurrentUser();

                    if (firebaseUser != null)
                    {

                        final String userBio = "";
                        final String userId = firebaseUser.getUid();
                        final String userImage = "https://firebasestorage.googleapis.com/v0/b/news-expose-2k20.appspot.com/o/Res_20220713%2Fdefault_user_image.png?alt=media&token=73111bc4-aa84-41f3-b4fe-8f5ef206dd2a";
                        final boolean userIsAdmin = false;
                        final boolean userIsVerified = false;

                        final UserModel user = new UserModel(
                                userBio,
                                contact,
                                email,
                                userId,
                                userImage,
                                userIsAdmin,
                                userIsVerified,
                                userName,
                                versionCode
                        );

                        setUserFirestore(userId, user);

                    }

                }).addOnFailureListener(e ->
                {

                    isLoading.setVisibility(View.GONE);

                    final String _e = e.toString().toLowerCase();

                    if (_e.contains("the email address is already in use by another account")) Toast.makeText(this, "Email is Already Exist!", Toast.LENGTH_SHORT).show();

                    else if (_e.contains("a network error (such as timeout, interrupted connection or unreachable host) has occurred")) Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                    else Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();

                });

    }

    private void setUserFirestore(final String userId, final UserModel user) {

        documentRef = firebaseFirestore
                .collection("Users")
                .document(userId);

        documentRef
                .get()
                .addOnSuccessListener(documentSnapshot ->
                {

                    if (documentSnapshot != null && !documentSnapshot.exists())

                        documentRef
                                .set(user)
                                .addOnSuccessListener(unused ->
                                {

                                    final HashMap<String, Object> _user = new HashMap<>();
                                    _user.put("user_email", email);
                                    _user.put("user_name", userName);
                                    _user.put("user_version_code", versionCode);

                                    setUserRealtime(userId, _user);

                                })
                                .addOnFailureListener(e ->
                                {

                                    isLoading.setVisibility(View.GONE);
                                    Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();

                                });

                });

    }

    private void setUserRealtime(final String userId, final HashMap<String, Object> user) {

        databaseRef = firebaseDatabase
                .getReference("Users")
                .child(userId);

        databaseRef
                .get()
                .addOnSuccessListener(documentSnapshot ->
                {

                    if (documentSnapshot != null && !documentSnapshot.exists())

                        databaseRef
                                .setValue(user)
                                .addOnSuccessListener(unused ->
                                {

                                    editor.putString("user_contact", ccpCountry.getFullNumberWithPlus());
                                    editor.apply();
                                    startActivity(new Intent(this, VerificationActivity.class));
                                    finish();

                                })
                                .addOnFailureListener(e ->
                                {

                                    isLoading.setVisibility(View.GONE);
                                    Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();

                                });

                });

    }

}
