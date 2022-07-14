package com.codewithjosh.NewsExpose2k20.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithjosh.NewsExpose2k20.R;
import com.codewithjosh.NewsExpose2k20.adapters.UpdateAdapter;
import com.codewithjosh.NewsExpose2k20.models.UpdateModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    ConstraintLayout isLoading;
    RecyclerView recyclerUpdates;
    TextView tvStatus;
    FirebaseFirestore firebaseFirestore;
    Context context;
    private UpdateAdapter updateAdapter;
    private List<UpdateModel> updates;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        initInstances();
        loadUpdates();

        return view;

    }

    private void initViews(final View view) {

        isLoading = view.findViewById(R.id.is_loading);
        recyclerUpdates = view.findViewById(R.id.recycler_updates);
        tvStatus = view.findViewById(R.id.tv_status);

        initRecyclerView();

        updates = new ArrayList<>();
        updateAdapter = new UpdateAdapter(getContext(), updates);
        recyclerUpdates.setAdapter(updateAdapter);

    }

    private void initRecyclerView() {

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerUpdates.setLayoutManager(linearLayoutManager);
        recyclerUpdates.setHasFixedSize(true);

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void loadUpdates() {

        isLoading.setVisibility(View.VISIBLE);

        firebaseFirestore
                .collection("Updates")
                .orderBy("update_timestamp")
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        if (validate(value)) onLoadUpdates(value);

                        else isLoading.setVisibility(View.GONE);

                    }

                });

    }

    private void onLoadUpdates(final QuerySnapshot value) {

        isLoading.setVisibility(View.GONE);
        tvStatus.setText("");

        updates.clear();
        for (QueryDocumentSnapshot snapshot : value) {

            final UpdateModel update = snapshot.toObject(UpdateModel.class);
            updates.add(update);

        }
        updateAdapter.notifyDataSetChanged();

    }

    private boolean validate(final QuerySnapshot value) {

        if (!isConnected()) tvStatus.setText(R.string.text_status_disconnected);

        else if (value.isEmpty()) tvStatus.setText(R.string.text_status_empty);

        else return true;

        return false;

    }

    private boolean isConnected() {

        if (getContext() != null) context = getContext();

        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

}