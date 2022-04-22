package com.codewithjosh.NewsExpose2k20.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codewithjosh.NewsExpose2k20.CommentActivity;
import com.codewithjosh.NewsExpose2k20.R;
import com.codewithjosh.NewsExpose2k20.models.UpdateModel;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateAdapter extends RecyclerView.Adapter<UpdateAdapter.ViewHolder> {

    public Context mContext;
    public List<UpdateModel> mUpdate;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    public UpdateAdapter(Context mContext, List<UpdateModel> mUpdate) {
        this.mContext = mContext;
        this.mUpdate = mUpdate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_update, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final UpdateModel updateModel = mUpdate.get(position);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        final String s_user_id = firebaseAuth.getCurrentUser().getUid();

//        TODO: FOUND ISSUE: UPDATE MODELS
        Glide.with(mContext).load(updateModel.getUpdate_image()).into(holder.iv_update_image);

        if (updateModel.getUpdate_content().isEmpty()) holder.tv_update_content.setVisibility(View.GONE);
        else {
            holder.tv_update_content.setVisibility(View.VISIBLE);
            holder.tv_update_content.setText(updateModel.getUpdate_content());
        }

        if (updateModel.getUser_id().isEmpty()) {
            holder.tv_user_name.setVisibility(View.VISIBLE);
            holder.tv_user_name.setText(mContext.getResources().getString(R.string.def_user_name));
        } else {
            holder.tv_user_name.setVisibility(View.VISIBLE);
            holder.tv_user_name.setText(updateModel.getUser_id());
        }

        isSeen(updateModel.getUpdate_id(), s_user_id, holder.btn_seen);
        seenCount(holder.tv_seen_count, s_user_id, updateModel.getUpdate_id());
        commentCount(updateModel.getUpdate_id(), holder.tv_comment_count);

        holder.btn_seen.setOnClickListener(v -> {

            final DatabaseReference seenRef = firebaseDatabase
                    .getReference()
                    .child("Seen")
                    .child(updateModel.getUpdate_id())
                    .child(s_user_id);

            if (holder.btn_seen.getTag().equals("seen")) seenRef.setValue(true);
            else seenRef.removeValue();

        });

        holder.nav_comment.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, CommentActivity.class);
            intent.putExtra("updateid", updateModel.getUpdate_id());
            intent.putExtra("userid", updateModel.getUser_id());
            mContext.startActivity(intent);
        });

        getSource(holder.civ_user_image, holder.tv_user_name, updateModel.getUser_id());
    }

    @Override
    public int getItemCount() {
        return mUpdate.size();
    }

    private void getSource(final CircleImageView civ_user_image, final TextView tv_user_name, final String s_user_id) {

        final DatabaseReference userRef = firebaseDatabase.getReference("Users").child(s_user_id);

//        TODO: USE GET METHOD ONCE IT IS AVAILABLE
        userRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel usermodel = dataSnapshot.getValue(UserModel.class);
                Glide.with(mContext).load(usermodel.getUser_image()).into(civ_user_image);
                tv_user_name.setText(usermodel.getUser_name());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isSeen(final String s_update_id, final String s_user_id, final ImageButton btn_seen) {

        final DatabaseReference updateRef = firebaseDatabase
                .getReference()
                .child("Seen")
                .child(s_update_id);

//        TODO: USE GET METHOD ONCE IT IS AVAILABLE
        updateRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(s_user_id).exists()) {
                    btn_seen.setImageResource(R.drawable.ic_seened);
                    btn_seen.setTag("seened");
                } else {
                    btn_seen.setImageResource(R.drawable.ic_seen);
                    btn_seen.setTag("seen");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void seenCount(final TextView tv_seen_count, final String s_user_id, final String s_update_id) {

        final DatabaseReference updateRef = firebaseDatabase
                .getReference()
                .child("Seen")
                .child(s_update_id);

//        TODO: USE GET METHOD ONCE IT IS AVAILABLE
        updateRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                tv_seen_count.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                if (dataSnapshot.child(s_user_id).exists())
                    tv_seen_count.setTextColor(mContext.getResources().getColor(R.color.colorKUCrimson));
                else
                    tv_seen_count.setTextColor(mContext.getResources().getColor(R.color.colorLightGray));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void commentCount(final String s_update_id, final TextView tv_comment_count) {

        final DatabaseReference updateRef = firebaseDatabase
                .getReference()
                .child("Comments")
                .child(s_update_id);

//        TODO: USE GET METHOD ONCE IT IS AVAILABLE
        updateRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                tv_comment_count.setText(String.valueOf(dataSnapshot.getChildrenCount()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civ_user_image;
        ImageButton btn_seen, nav_comment;
        ImageView iv_update_image;
        TextView tv_seen_count, tv_update_content, tv_comment_count, tv_user_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            civ_user_image = itemView.findViewById(R.id.civ_user_image);
            btn_seen = itemView.findViewById(R.id.btn_seen);
            nav_comment = itemView.findViewById(R.id.nav_comment);
            iv_update_image = itemView.findViewById(R.id.iv_update_image);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            tv_update_content = itemView.findViewById(R.id.tv_update_content);
            tv_seen_count = itemView.findViewById(R.id.tv_seen_count);
            tv_comment_count = itemView.findViewById(R.id.tv_comment_count);

        }
    }
}
