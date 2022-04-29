package com.codewithjosh.NewsExpose2k20.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateAdapter extends RecyclerView.Adapter<UpdateAdapter.ViewHolder> {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    String s_user_id;

    FirebaseDatabase firebaseDatabase;

    FirebaseFirestore firebaseFirestore;
    DocumentReference documentRef;

    CollectionReference seenRef, commentRef;
    SharedPreferences sharedPref;

    SharedPreferences.Editor editor;

    public Context context;
    public List<UpdateModel> updateList;

    public UpdateAdapter(Context context, List<UpdateModel> updateList) {
        this.context = context;
        this.updateList = updateList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_update, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final UpdateModel update = updateList.get(position);

//        initViews
        final CircleImageView civ_user_image = holder.civ_user_image;
        final ImageButton btn_seen = holder.btn_seen;
        final ImageButton nav_comment = holder.nav_comment;
        final ImageView iv_update_image = holder.iv_update_image;
        final TextView tv_seen_count = holder.tv_seen_count;
        final TextView tv_update_content = holder.tv_update_content;
        final TextView tv_comment_count = holder.tv_comment_count;
        final TextView tv_user_bio = holder.tv_user_bio;
        final TextView tv_subtitle = holder.tv_subtitle;

//        load
        final String s_update_id = update.getUpdate_id();
        final String s_update_image = update.getUpdate_image();
        final String s_update_content = update.getUpdate_content();
        final Date date_update_timestamp = update.getUpdate_timestamp();
        final String s_update_user_id = update.getUser_id();

        initInstance();
        initSharedPref();
        load();

        loadUser(civ_user_image, tv_user_bio, tv_subtitle, s_update_user_id, date_update_timestamp);

        if (!s_update_content.isEmpty()) {

            tv_update_content.setVisibility(View.VISIBLE);
            tv_update_content.setText(update.getUpdate_content());
        } else tv_update_content.setVisibility(View.GONE);

        Glide.with(context).load(s_update_image).into(iv_update_image);

        seenRef = firebaseFirestore
                .collection("Updates")
                .document(s_update_id)
                .collection("Seen");

        isSeen(btn_seen, tv_seen_count);

        seenCount(tv_seen_count);

        btn_seen.setOnClickListener(v -> {

            final UserModel user = new UserModel();

            documentRef = firebaseFirestore
                    .collection("Updates")
                    .document(s_update_id)
                    .collection("Seen")
                    .document(s_user_id);

            if (btn_seen.getTag().equals("seen")) documentRef.set(user);

            else documentRef.delete();

        });

        commentRef = firebaseFirestore
                .collection("Updates")
                .document(s_update_id)
                .collection("Comments");

        commentCount(tv_comment_count);

        nav_comment.setOnClickListener(v -> {

            editor.putString("s_update_id", s_update_id);
            editor.apply();
            context.startActivity(new Intent(context, CommentActivity.class));
        });

    }

    private void initInstance() {

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private void load() {

        s_user_id = sharedPref.getString("s_user_id", String.valueOf(Context.MODE_PRIVATE));

    }

    private void loadUser(final CircleImageView civ_user_image, final TextView tv_user_name, final TextView tv_subtitle, final String s_update_user_id, final Date date_update_timestamp) {

        firebaseFirestore
                .collection("Users")
                .document(s_update_user_id)
                .addSnapshotListener((value, error) -> {

                    if (value != null)

                        if (value.exists()) {

                            final UserModel user = value.toObject(UserModel.class);

                            if (user != null) {

                                final String s_user_image = user.getUser_image();
                                final String s_user_bio = user.getUser_bio();
                                final String s_user_name = user.getUser_name();
                                final String s_subtitle = getTimeAgo(date_update_timestamp) + " · " + s_user_name;

                                Glide.with(context).load(s_user_image).into(civ_user_image);
                                tv_user_name.setText(s_user_bio);
                                tv_subtitle.setText(s_subtitle);
                            }
                        }
                });

    }

    private String getTimeAgo(final Date date_update_timestamp) {

        final Calendar calendar = Calendar.getInstance();

        long now = calendar.getTime().getTime();
        long time = date_update_timestamp.getTime();

        if (time < 1000000000000L) time *= 1000;
        final long diff = now - time;

        if (diff < MINUTE_MILLIS) return "Just now";

        else if (diff < 60 * MINUTE_MILLIS) return diff / MINUTE_MILLIS + "m";

        else if (diff < 24 * HOUR_MILLIS) return diff / HOUR_MILLIS + "h";

        else if (diff < 48 * HOUR_MILLIS) return "yesterday";

        else return diff / DAY_MILLIS + "d";

    }

    private void isSeen(final ImageButton btn_seen, final TextView tv_seen_count) {

        seenRef
                .document(s_user_id)
                .addSnapshotListener((value, error) -> {

                    if (value != null)

                        if (value.exists()) {

                            btn_seen.setImageResource(R.drawable.ic_seened);
                            btn_seen.setTag("");
                            tv_seen_count.setTextColor(context.getColor(R.color.colorFulvous));
                        }
                        else {
                            btn_seen.setTag("seen");
                            btn_seen.setImageResource(R.drawable.ic_seen);
                            tv_seen_count.setTextColor(context.getColor(R.color.colorWhite_FF));
                        }
                });

    }

    private void seenCount(final TextView tv_seen_count) {

        seenRef
                .addSnapshotListener((value, error) -> {

                    if (value != null) {

                        final String s_seen_count = String.valueOf(value.size());

                        tv_seen_count.setText(s_seen_count);
                    }
                });

    }

    private void commentCount(final TextView tv_comment_count) {

        commentRef
                .addSnapshotListener((value, error) -> {

                    if (value != null) {

                        final String s_comment_count = String.valueOf(value.size());

                        tv_comment_count.setText(s_comment_count);
                    }
                });

    }

    @Override
    public int getItemCount() {
        return updateList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civ_user_image;
        ImageButton btn_seen, nav_comment;
        ImageView iv_update_image;
        TextView tv_seen_count, tv_update_content, tv_comment_count, tv_user_bio, tv_subtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            civ_user_image = itemView.findViewById(R.id.civ_user_image);
            btn_seen = itemView.findViewById(R.id.btn_seen);
            nav_comment = itemView.findViewById(R.id.nav_comment);
            iv_update_image = itemView.findViewById(R.id.iv_update_image);
            tv_user_bio = itemView.findViewById(R.id.tv_user_bio);
            tv_update_content = itemView.findViewById(R.id.tv_update_content);
            tv_seen_count = itemView.findViewById(R.id.tv_seen_count);
            tv_comment_count = itemView.findViewById(R.id.tv_comment_count);
            tv_subtitle = itemView.findViewById(R.id.tv_subtitle);

        }

    }

}
