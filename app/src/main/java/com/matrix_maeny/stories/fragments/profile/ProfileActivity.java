package com.matrix_maeny.stories.fragments.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.UserModel;
import com.matrix_maeny.stories.databinding.ActivityProfileBinding;
import com.matrix_maeny.stories.fragments.search.SearchAdapter;
import com.matrix_maeny.stories.javaclasses.ExposeDialogs;
import com.matrix_maeny.stories.posts.PostModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    private FirebaseDatabase database;

    private ExposeDialogs dialogs;
    private ArrayList<PostModel> list;
    private SearchAdapter adapter;

    public static  String profileUserId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.prNToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");

        FirebaseApp.initializeApp(ProfileActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        initialize();
    }


    private void initialize() {
        database = FirebaseDatabase.getInstance();

        list = new ArrayList<>();
        adapter = new SearchAdapter(ProfileActivity.this, list);

        binding.prRecyclerView.setLayoutManager(new GridLayoutManager(ProfileActivity.this, 2));
        binding.prRecyclerView.setAdapter(adapter);


        dialogs = new ExposeDialogs(ProfileActivity.this);


        fetchUserData();
    }

    private void fetchUserData() {
        dialogs.showProgressDialog("Fetching details","wait few seconds");
        database.getReference().child("Users").child(Objects.requireNonNull(profileUserId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel model = snapshot.getValue(UserModel.class);
                        if (model != null) {
                            setUserData(model);
                            fetchPosts();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void setUserData(UserModel model) {
        try {
            Picasso.get().load(model.getProfilePicUrl()).placeholder(R.drawable.profile_pic).into(binding.prUserIv);
        } catch (Exception e) {
            e.printStackTrace();
            binding.prUserIv.setImageResource(R.drawable.profile_pic);
        }

        binding.prUsernameTv.setText(model.getUsername());
        if (!model.getAbout().equals("")) {
            binding.prUserAboutTv.setText(model.getAbout());
        } else {
            binding.prUserAboutTv.setText("No about provided");
        }

        long count = model.getPostCount();
        String unit = "";

        if (count / 1000000 > 0) {
            count /= 1000000;
            unit = "M";
        } else if (count / 1000 > 0) {
            count = count / 1000;
            unit = "K";
        }
        binding.prNoOfPostsTv.setText(count + unit);

        count = model.getFollowerCount();
        unit = "";
        if (count / 1000000 > 0) {
            count /= 1000000;
            unit = "M";
        } else if (count / 1000 > 0) {
            count = count / 1000;
            unit = "K";
        }
        binding.prNoOfFollowersTv.setText(count + unit);


        count = model.getFollowingCount();
        unit = "";
        if (count / 1000000 > 0) {
            count /= 1000000;
            unit = "M";
        } else if (count / 1000 > 0) {
            count = count / 1000;
            unit = "K";
        }
        binding.prNoOfFollowingTv.setText(count + unit);

    }

    private void fetchPosts() {
        database.getReference().child("Posts").child(Objects.requireNonNull(profileUserId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        binding.prSwipeRefreshLayout.setRefreshing(false);
                        list.clear();
                        if (snapshot.exists()) {
                            binding.lgEmptyTv.setVisibility(View.INVISIBLE);

                            for (DataSnapshot s : snapshot.getChildren()) {
                                PostModel model = s.getValue(PostModel.class);
                                if (model != null) {
                                    list.add(model);
                                }
                            }

                            refreshAdapter();

                        } else {
                            binding.lgEmptyTv.setVisibility(View.VISIBLE);
                        }
                        try {
                            dialogs.dismissProgressDialog();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshAdapter() {
        new Handler().post(() -> adapter.notifyDataSetChanged());
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseApp.initializeApp(ProfileActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
    }
}