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
import com.codewithjosh.NewsExpose2k20.BuildConfig;
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

    public Context context;
    public List<UpdateModel> updateList;

    public UpdateAdapter(Context context, List<UpdateModel> updateList) {
        this.context = context;
        this.updateList = updateList;
    }

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_update, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final UpdateModel update = updateList.get(position);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        final String s_user_id = firebaseAuth.getCurrentUser().getUid();

        Glide.with(context).load(update.getUpdate_image()).into(holder.iv_update_image);

        if (update.getUpdate_content().isEmpty()) holder.tv_update_content.setVisibility(View.GONE);
        else {
            holder.tv_update_content.setVisibility(View.VISIBLE);
            holder.tv_update_content.setText(update.getUpdate_content());
        }

        if (update.getUser_id().isEmpty()) {
            holder.tv_user_name.setVisibility(View.VISIBLE);
            holder.tv_user_name.setText(context.getResources().getString(R.string.def_user_name));
        }
        else {
            holder.tv_user_name.setVisibility(View.VISIBLE);
            holder.tv_user_name.setText(update.getUser_id());
        }

        isSeen(update.getUpdate_id(), s_user_id, holder.btn_seen);
        seenCount(holder.tv_seen_count, s_user_id, update.getUpdate_id());
        commentCount(update.getUpdate_id(), holder.tv_comment_count);

        holder.btn_seen.setOnClickListener(v -> {

            final DatabaseReference seenRef = firebaseDatabase
                    .getReference()
                    .child("Seen")
                    .child(update.getUpdate_id())
                    .child(s_user_id);

            if (holder.btn_seen.getTag().equals("seen")) seenRef.setValue(true);
            else seenRef.removeValue();

        });

        holder.nav_comment.setOnClickListener(v -> {

            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("s_update_id", update.getUpdate_id());
            intent.putExtra("s_user_id", s_user_id);
            context.startActivity(intent);
        });

        getSource(holder.civ_user_image, holder.tv_user_name, update.getUser_id());
    }

    @Override
    public int getItemCount() {
        return updateList.size();
    }

    private void getSource(final CircleImageView civ_user_image, final TextView tv_user_name, final String s_user_id) {

        firebaseDatabase
                .getReference("Users")
                .child(s_user_id)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final UserModel user = dataSnapshot.getValue(UserModel.class);

                        Glide.with(context).load(user.getUser_image()).into(civ_user_image);
                        tv_user_name.setText(user.getUser_name());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void isSeen(final String s_update_id, final String s_user_id, final ImageButton btn_seen) {

        firebaseDatabase
                .getReference("Seen")
                .child(s_update_id)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(s_user_id).exists()) {

                            btn_seen.setImageResource(R.drawable.ic_seened);
                            btn_seen.setTag("seened");
                        }
                        else {

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

        firebaseDatabase
                .getReference("Seen")
                .child(s_update_id)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        tv_seen_count.setText(String.valueOf(dataSnapshot.getChildrenCount()));

                        if (dataSnapshot.child(s_user_id).exists()) tv_seen_count.setTextColor(context.getResources().getColor(R.color.colorKUCrimson));
                        else tv_seen_count.setTextColor(context.getResources().getColor(R.color.colorLightGray));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void commentCount(final String s_update_id, final TextView tv_comment_count) {

        final int i_version_code = BuildConfig.VERSION_CODE;

        firebaseDatabase
                .getReference("Comments")
                .child(s_update_id)
                .orderByChild("user_version_code")
                .equalTo(i_version_code)
                .addValueEventListener(new ValueEventListener() {

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
