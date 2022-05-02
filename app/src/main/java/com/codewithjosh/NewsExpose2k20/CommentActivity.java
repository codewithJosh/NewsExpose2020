package com.codewithjosh.NewsExpose2k20;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codewithjosh.NewsExpose2k20.adapters.CommentAdapter;
import com.codewithjosh.NewsExpose2k20.models.CommentModel;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    Button btn_comment;
    ConstraintLayout is_loading;
    EditText et_comment_content;
    ImageButton btn_back;
    ImageView iv_user_image;
    RecyclerView recycler_comments;
    TextView tv_status;

    String s_update_id, s_user_id, s_comment_content;

    FirebaseFirestore firebaseFirestore;

    CollectionReference collectionRef;
    DocumentReference documentRef;

    SharedPreferences sharedPref;

    private CommentAdapter commentAdapter;
    private List<CommentModel> commentList;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseFirestore = FirebaseFirestore.getInstance();

        load();
        loadUserImage();
    }

    private void loadUserImage() {

        firebaseFirestore
                .collection("Users")
                .document(s_user_id)
                .addSnapshotListener((value, error) -> {

                    if (value != null) {

                        final UserModel user = value.toObject(UserModel.class);

                        if (user != null) {

                            final String s_user_image = user.getUser_image();

                            Glide.with(this).load(s_user_image).into(iv_user_image);

                        }
                    }
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        firebaseFirestore = FirebaseFirestore.getInstance();

        initViews();
        load();

        collectionRef = firebaseFirestore
                .collection("Updates")
                .document(s_update_id)
                .collection("Comments");

        loadComments();
        build();

    }

    private void initViews() {

        et_comment_content = findViewById(R.id.et_comment_content);
        btn_back = findViewById(R.id.btn_back);
        btn_comment = findViewById(R.id.btn_comment);
        iv_user_image = findViewById(R.id.civ_user_image);
        is_loading = findViewById(R.id.is_loading);
        recycler_comments = findViewById(R.id.recycler_comments);
        tv_status = findViewById(R.id.tv_status);

        recycler_comments.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recycler_comments.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        recycler_comments.setAdapter(commentAdapter);

    }

    private void load() {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);
        s_update_id = sharedPref.getString("s_update_id", String.valueOf(MODE_PRIVATE));
        s_user_id = sharedPref.getString("s_user_id", String.valueOf(MODE_PRIVATE));

    }

    private void loadComments() {

        is_loading.setVisibility(View.VISIBLE);

        collectionRef
                .orderBy("comment_timestamp")
                .addSnapshotListener((value, error) -> {

                    if (value != null)

                        if (validate(value)) onLoadComments(value);

                        else is_loading.setVisibility(View.GONE);

                });

    }

    private boolean validate(final QuerySnapshot value) {

        if (!isConnected()) tv_status.setText(R.string.text_status_disconnected);

        else if (value.isEmpty()) tv_status.setText(R.string.text_status_empty_1);

        else return true;

        return false;

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void onLoadComments(final QuerySnapshot value) {

        is_loading.setVisibility(View.GONE);
        tv_status.setText("");

        commentList.clear();
        for (QueryDocumentSnapshot snapshot : value) {

            final CommentModel comment = snapshot.toObject(CommentModel.class);

            commentList.add(comment);
        }
        commentAdapter.notifyDataSetChanged();

    }

    private void build() {

        btn_back.setOnClickListener(v -> onBackPressed());

        btn_comment.setOnClickListener(v -> {

            s_comment_content = et_comment_content.getText().toString().trim();

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CommentActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

            if (!isConnected())
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

            else onComment();

        });

        et_comment_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                s_comment_content = et_comment_content.getText().toString().trim();
                btn_comment.setEnabled(!s_comment_content.isEmpty());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void onComment() {

        final String s_comment_id = collectionRef
                .document()
                .getId();

        final Calendar calendar = Calendar.getInstance();
        final Date date_comment_timestamp = calendar.getTime();

        final CommentModel comment = new CommentModel(
                s_comment_content,
                date_comment_timestamp,
                s_user_id
        );

        onSend(s_comment_id, comment);

    }

    private void onSend(final String s_comment_id, final CommentModel comment) {

        documentRef = collectionRef
                .document(s_comment_id);

        documentRef.addSnapshotListener((value, error) -> {

            if (value != null)

                if (!value.exists())

                    documentRef
                            .set(comment)
                            .addOnSuccessListener(unused -> et_comment_content.setText(""));
        });

    }

}