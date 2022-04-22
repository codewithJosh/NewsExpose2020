package com.codewithjosh.NewsExpose2k20.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codewithjosh.NewsExpose2k20.R;
import com.codewithjosh.NewsExpose2k20.models.CommentModel;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    public Context mContext;
    public List<CommentModel> mComment;
    FirebaseDatabase firebaseDatabase;

    public CommentAdapter(Context mContext, List<CommentModel> mComment) {
        this.mContext = mContext;
        this.mComment = mComment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_comment, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final CommentModel commentModel = mComment.get(position);

        firebaseDatabase = FirebaseDatabase.getInstance();

//        TODO: FOUND ISSUE: UPDATE THE MODELS
        holder.tv_comment_content.setText(commentModel.getComment_content());

        getUser(holder.civ_user_image, holder.tv_user_name, commentModel.getUser_id());

    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    private void getUser(final CircleImageView civ_user_image, final TextView tv_user_name, final String s_user_id) {

        final DatabaseReference userRef = firebaseDatabase
                .getReference()
                .child("Users")
                .child(s_user_id);

//        TODO: USE GET METHOD ONCE IT IS AVAILABLE
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                Glide.with(mContext).load(userModel.getUser_image()).into(civ_user_image);
                tv_user_name.setText(userModel.getUser_name());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civ_user_image;
        TextView tv_user_name, tv_comment_content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            civ_user_image = itemView.findViewById(R.id.civ_user_image);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            tv_comment_content = itemView.findViewById(R.id.tv_comment_content);
        }
    }

}
