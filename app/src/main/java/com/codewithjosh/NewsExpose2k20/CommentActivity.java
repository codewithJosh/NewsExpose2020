package com.codewithjosh.NewsExpose2k20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codewithjosh.NewsExpose2k20.adapters.CommentAdapter;
import com.codewithjosh.NewsExpose2k20.models.CommentModel;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    private CommentAdapter commentAdapter;
    private List<CommentModel> commentList;

    EditText et_comment_content;
    ImageButton btn_back, btn_comment;
    ImageView iv_user_image;
    RecyclerView recycler_comments;

    int i_version_code;
    String s_update_id, s_user_id;

    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        et_comment_content = findViewById(R.id.et_comment_content);
        btn_back = findViewById(R.id.btn_back);
        btn_comment = findViewById(R.id.btn_comment);
        iv_user_image = findViewById(R.id.civ_user_image);
        recycler_comments = findViewById(R.id.recycler_comments);

        i_version_code = BuildConfig.VERSION_CODE;
        s_update_id = getIntent().getStringExtra("s_update_id");
        s_user_id = getIntent().getStringExtra("s_user_id");

        firebaseDatabase = FirebaseDatabase.getInstance();

        btn_back.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));

        getUserImage();

        btn_comment.setOnClickListener(v -> {

            final String s_comment_content = et_comment_content.getText().toString().trim();

            if (s_comment_content.isEmpty()) Toast.makeText(this, "You can't send empty comment", Toast.LENGTH_SHORT).show();
            else onSend(s_comment_content);

        });

        recycler_comments.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recycler_comments.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        recycler_comments.setAdapter(commentAdapter);

        getComments();
    }

    private void onSend(final String s_comment_content) {

        final CommentModel comment = new CommentModel(
                s_comment_content,
                s_user_id,
                i_version_code
        );

        firebaseDatabase
                .getReference("Comments")
                .child(s_update_id)
                .push()
                .setValue(comment)
                .addOnSuccessListener(runnable -> et_comment_content.setText(""));

    }

    private void getUserImage() {

        firebaseDatabase
                .getReference("Users")
                .child(s_user_id)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final UserModel userModel = dataSnapshot.getValue(UserModel.class);

                        Glide.with(getApplicationContext()).load(userModel.getUser_image()).into(iv_user_image);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void getComments() {

        firebaseDatabase
                .getReference("Comments")
                .child(s_update_id)
                .orderByChild("user_version_code")
                .equalTo(i_version_code)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        commentList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            CommentModel commentModel = snapshot.getValue(CommentModel.class);
                            commentList.add(commentModel);
                        } commentAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

}