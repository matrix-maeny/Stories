package com.matrix_maeny.stories.fragments.profile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.databinding.ActivityProfilePicViewerBinding;
import com.matrix_maeny.stories.javaclasses.ExposeDialogs;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfilePicViewerActivity extends AppCompatActivity {

    private ActivityProfilePicViewerBinding binding;

    private Uri imageUri = null;
    public static String profilePicUrl = "";

    private ExposeDialogs dialogs;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfilePicViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        setSupportActionBar(binding.ppvToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile Pic");


        FirebaseApp.initializeApp(ProfilePicViewerActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        initialize();
    }

    private void initialize() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();


        dialogs = new ExposeDialogs(ProfilePicViewerActivity.this);

        try {
            Picasso.get().load(profilePicUrl).placeholder(R.drawable.profile_pic).into(binding.ppvUserProfilePicIv);
        } catch (Exception e) {
            e.printStackTrace();
            chooseProfilePic();
        }
    }


    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();

                if (data != null) {
                    imageUri = data.getData(); // assigning the uri of selected image
                    binding.ppvUserProfilePicIv.setImageURI(imageUri);
//                    saveProfilePic();
                } else {
                    imageUri = null; // else make it null
                }
                dialogs.dismissProgressDialog();
            });

    public void chooseProfilePic() {

        dialogs.showProgressDialog("Getting Image", "Wait few seconds"); // showing a waiting dialog

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        launcher.launch(Intent.createChooser(intent, "Select a picture"));
    }


    private void saveProfilePic() {
        dialogs.showProgressDialog("Saving", "wait few seconds");


        final StorageReference storageReference = storage.getReference().child("Users").child(Objects.requireNonNull(auth.getUid()));

        storageReference.putFile(imageUri).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (task.isSuccessful()) {

                        database.getReference().child("Users")
                                .child(Objects.requireNonNull(auth.getUid())).child("profilePicUrl").setValue(uri.toString())
                                .addOnCompleteListener(task1 -> {

                                    dialogs.dismissProgressDialog();

                                    if(task1.isSuccessful()){
                                        Toast.makeText(ProfilePicViewerActivity.this, "Profile image saved", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }else
                                        Toast.makeText(ProfilePicViewerActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                                }).addOnFailureListener(e -> {
                                    dialogs.dismissProgressDialog();
                                    Toast.makeText(ProfilePicViewerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                });

                    } else {
                        Toast.makeText(ProfilePicViewerActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        dialogs.dismissProgressDialog();
                    }
                }).addOnFailureListener(e -> {
                    dialogs.dismissProgressDialog();


                    Toast.makeText(ProfilePicViewerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

            } else
                Toast.makeText(ProfilePicViewerActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

            dialogs.dismissProgressDialog();

        }).addOnFailureListener(e -> {
            Toast.makeText(ProfilePicViewerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            dialogs.dismissProgressDialog();


        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_pic_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // choose profile pic
        switch (item.getItemId()) {
            case R.id.ppv_edit_pic:
                chooseProfilePic();
                break;
            case R.id.ppv_save:
                saveProfilePic();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseApp.initializeApp(ProfilePicViewerActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
    }
}