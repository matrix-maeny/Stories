package com.matrix_maeny.stories.fragments.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.UserModel;
import com.matrix_maeny.stories.databinding.FragmentLogoutBinding;
import com.matrix_maeny.stories.posts.PostModel;
import com.matrix_maeny.stories.posts.SavedPostsActivity;

import java.util.Objects;


public class LogoutFragment extends BottomSheetDialogFragment {

    private FragmentLogoutBinding binding;
    private LogoutFragmentListener listener;

    public static boolean shouldDelete = false;
    public static PostModel model = null;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLogoutBinding.inflate(inflater, container, false);

        try {
            listener = (LogoutFragmentListener) requireContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Deleting post");
        progressDialog.setMessage("wait...");

        if (!shouldDelete) {
            binding.LgLogoutTv.setText("Log out");
            binding.lgSavedPostsTv.setVisibility(View.VISIBLE);
        } else {
            binding.LgLogoutTv.setText("Delete post");
            binding.lgSavedPostsTv.setVisibility(View.GONE);
        }

        binding.LgLogoutTv.setOnClickListener(v -> {
            if (!shouldDelete) {
                dismiss();
                listener.logout();
            } else {
                deletePost();
            }

        });

        binding.lgSavedPostsTv.setOnClickListener(v -> {
            dismiss();
            requireContext().startActivity(new Intent(requireContext().getApplicationContext(), SavedPostsActivity.class));
        });


        return binding.getRoot();
    }


    private void deletePost() {
        progressDialog.show();
        FirebaseDatabase.getInstance().getReference()
                .child("Posts").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .child(model.getTitle()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        UserModel userModel = snapshot.getValue(UserModel.class);
                                        if (userModel != null) {
                                            FirebaseDatabase.getInstance().getReference().child("Users")
                                                    .child(FirebaseAuth.getInstance().getUid())
                                                    .child("postCount").setValue(userModel.getPostCount() - 1);
                                            Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show();

                                            // deleting at comment and like section

                                            FirebaseDatabase.getInstance().getReference().child("Likes")
                                                    .child(FirebaseAuth.getInstance().getUid())
                                                    .child(model.getUserUid())
                                                    .child(model.getTitle())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if(snapshot.exists()){
                                                                FirebaseDatabase.getInstance().getReference().child("Likes")
                                                                        .child(FirebaseAuth.getInstance().getUid())
                                                                        .child(model.getUserUid())
                                                                        .child(model.getTitle()).removeValue();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                            FirebaseDatabase.getInstance().getReference().child("Comments")
                                                    .child(model.getUserUid())
                                                    .child(model.getTitle())
                                                    .child(FirebaseAuth.getInstance().getUid())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if(snapshot.exists()){
                                                                FirebaseDatabase.getInstance().getReference().child("Comments")
                                                                        .child(model.getUserUid())
                                                                        .child(model.getTitle())
                                                                        .child(FirebaseAuth.getInstance().getUid()).removeValue();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                            try {
                                                FirebaseStorage.getInstance().getReference().child("Posts")
                                                        .child(FirebaseAuth.getInstance().getUid())
                                                        .child(model.getTitle()).delete();
                                                progressDialog.dismiss();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        }
                                        try {
                                            progressDialog.dismiss();
                                            dismiss();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    } else
                        Toast.makeText(requireContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    public interface LogoutFragmentListener {
        void logout();

    }

}