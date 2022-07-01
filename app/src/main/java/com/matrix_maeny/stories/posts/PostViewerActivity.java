package com.matrix_maeny.stories.posts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.UserModel;
import com.matrix_maeny.stories.databinding.ActivityPostViewerBinding;
import com.matrix_maeny.stories.databinding.PostModelBinding;
import com.matrix_maeny.stories.fragments.profile.FollowModel;
import com.matrix_maeny.stories.fragments.profile.ProfileActivity;
import com.matrix_maeny.stories.javaclasses.ExposeDialogs;
import com.squareup.picasso.Picasso;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class PostViewerActivity extends AppCompatActivity {

    private ActivityPostViewerBinding binding;


    public static PostModel model = null;
    private FirebaseDatabase firebaseDatabase;
    private String currentUid = "";
    private boolean isFollowing = false;

    private ExposeDialogs exposeDialogs;

    private String profilePicUrl = "";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.pvToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));

        FirebaseApp.initializeApp(PostViewerActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());


        initialize();


        binding.pvUserIv.setOnClickListener(v -> {
            ProfileActivity.profileUserId = model.getUserUid();
            startActivity(new Intent(PostViewerActivity.this, ProfileActivity.class));
        });

        binding.pamFollowTv2.setOnClickListener(v -> {
            if (isFollowing) {
                isFollowing = false;
                unFollowUser();
                binding.pamFollowTv2.setText("Follow");
                binding.pamFollowTv2.setBackgroundResource(R.drawable.follow_bg);
            } else {
                isFollowing = true;
                followUser();
                binding.pamFollowTv2.setText("Following");
                binding.pamFollowTv2.setBackgroundResource(R.drawable.following_bg);
            }
        });
    }

    private void initialize() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        currentUid = FirebaseAuth.getInstance().getUid();

        exposeDialogs = new ExposeDialogs(PostViewerActivity.this);

        if (model != null) {
            if(model.getUserUid().equals(currentUid)){
                binding.pamFollowTv2.setVisibility(View.GONE);
            }
            exposeDialogs.showProgressDialog("Loading", "wait few seconds...");
            setPostContent();
        }
    }


    private void setPostContent() {
        setUserDetails();

        setPostDetails();

        setRecipeContent();


    }

    private void setRecipeContent() {


        binding.pvProcedureTv.setText(model.getContent());

        String headings = "<u><b>" + getString(R.string.any_other_references) + "</b></u>";
        binding.pvAdInsHeadingTv.setText(Html.fromHtml(headings));

        if (model.getReferences().equals("")) {
            binding.addInsLayout2.setVisibility(View.GONE);
            binding.pvLastViewLine.setVisibility(View.INVISIBLE);
        } else {
            binding.pvAddInsTv.setText(model.getReferences());

        }


    }

    @SuppressLint("SetTextI18n")
    private void setPostDetails() {
        Picasso.get().load(model.getImageUrl()).into(binding.pvPostIv);
        binding.pvPostTitleTv.setText(model.getTitle());
        binding.pvPostTagTv.setText(model.getTagLine());
        binding.pvDateTv.setText(Html.fromHtml("shared on <b>" + model.getLocalDate() + "</b>"));

    }

    private void setUserDetails() {
        setProfilePic();

        binding.pvUsernameTv.setText(model.getUsername());
        setFollowings();

    }


    private void setProfilePic() {
        firebaseDatabase.getReference().child("Users").child(Objects.requireNonNull(model.getUserUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel model = snapshot.getValue(UserModel.class);
                        if (model != null) {

                            profilePicUrl = model.getProfilePicUrl();
                            try {

                                Picasso.get().load(model.getProfilePicUrl()).placeholder(R.drawable.profile_pic).into(binding.pvUserIv);

                            } catch (Exception e) {
                                e.printStackTrace();
                                binding.pvUserIv.setImageResource(R.drawable.profile_pic);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void unFollowUser() {
        firebaseDatabase.getReference().child("Followings").child(currentUid).child(model.getUserUid()).removeValue()
                .addOnSuccessListener(unused -> {
                    followingCountForCurrentUser(false);

                    firebaseDatabase.getReference().child("Followers").child(model.getUserUid())
                            .child(currentUid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    followerCountForModelUser(false);
                                }
                            }).addOnFailureListener(e -> Toast.makeText(PostViewerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                }).addOnFailureListener(e -> Toast.makeText(PostViewerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        ;
    }

    private void setFollowings() {
        firebaseDatabase.getReference().child("Followings").child(currentUid).child(model.getUserUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isFollowing = snapshot.exists();
                        if (isFollowing) {
                            binding.pamFollowTv2.setText("Following");
                            binding.pamFollowTv2.setBackgroundResource(R.drawable.following_bg);
                        } else {
                            binding.pamFollowTv2.setText("Follow");
                            binding.pamFollowTv2.setBackgroundResource(R.drawable.follow_bg);
                        }

                        exposeDialogs.dismissProgressDialog();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PostViewerActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void followUser() {

        firebaseDatabase.getReference().child("Users").child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                if (userModel != null) {
                    FollowModel followModel = new FollowModel(userModel.getUsername(), userModel.getProfilePicUrl(), snapshot.getKey());

                    // for other user space
                    firebaseDatabase.getReference().child("Followers").child(model.getUserUid())
                            .child(currentUid)
                            .setValue(followModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    followerCountForModelUser(true);


                                    FollowModel followModel2 = new FollowModel(model.getUsername(), profilePicUrl, model.getUserUid());

                                    // for your space
                                    firebaseDatabase.getReference().child("Followings").child(currentUid)
                                            .child(model.getUserUid()).setValue(followModel2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    followingCountForCurrentUser(true);
                                                }
                                            });

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void followerCountForModelUser(boolean shouldIncrease) {
        firebaseDatabase.getReference().child("Users").child(model.getUserUid()).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);

                        if (userModel != null) {
                            if (shouldIncrease) {
                                userModel.setFollowerCount(userModel.getFollowerCount() + 1);
                            } else
                                userModel.setFollowerCount(userModel.getFollowerCount() - 1);


                            firebaseDatabase.getReference().child("Users").child(model.getUserUid())
                                    .setValue(userModel);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PostViewerActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void followingCountForCurrentUser(boolean shouldIncrease) {
        firebaseDatabase.getReference().child("Users").child(currentUid).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);

                        if (userModel != null) {
                            if (shouldIncrease) {
                                userModel.setFollowingCount(userModel.getFollowingCount() + 1);
                            } else
                                userModel.setFollowingCount(userModel.getFollowingCount() - 1);

                            firebaseDatabase.getReference().child("Users").child(currentUid)
                                    .setValue(userModel);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseApp.initializeApp(PostViewerActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
    }
}