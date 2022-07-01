package com.matrix_maeny.stories.posts;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.UserModel;
import com.matrix_maeny.stories.databinding.ActivityPostBinding;
import com.matrix_maeny.stories.javaclasses.ExposeDialogs;

import java.time.LocalDate;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private ActivityPostBinding binding;
    private FirebaseAuth auth;
    private String currentUserUid = null;
    private FirebaseDatabase database;
    private FirebaseStorage storage;


    private String title = null, tagLine = null, content = null, references = "";//,username=null;
    private UserModel userModel = null;
    private Uri imageUri = null;
    private boolean isAlreadyExists = false; // to check whether the title is existed or not in the posts

    private ExposeDialogs dialogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.postToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("New Post");

        FirebaseApp.initializeApp(PostActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        initialize();
    }

    private void initialize() {
        auth = FirebaseAuth.getInstance(); // initializing authentication
        currentUserUid = auth.getUid(); // initializing current user id

        database = FirebaseDatabase.getInstance();// initializing firebase database
        storage = FirebaseStorage.getInstance(); // initializing storage

        dialogs = new ExposeDialogs(PostActivity.this);
        dialogs.setAllCancellable(false);

        imageUri = getUriToDrawable(PostActivity.this, R.drawable.bg1);

        getCurrentUserData();

    }



    private void getCurrentUserData() {
        auth = FirebaseAuth.getInstance();
//        firebaseDatabase = FirebaseDatabase.getInstance();

        dialogs.showProgressDialog("Loading...", "Fetching requirements");
        database.getReference().child("Users")
                .child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dialogs.dismissProgressDialog();
                        if (snapshot.exists()) {
                            UserModel model = snapshot.getValue(UserModel.class);
                            if (model != null) {
                                userModel = model;
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialogs.dismissProgressDialog();
                    }
                });
    }


    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();

                if (data != null) {
                    imageUri = data.getData(); // assigning the uri of selected image
                    binding.postUploadedIv.setImageURI(imageUri);
                } else {
                    imageUri = null; // else make it null
                }
                dialogs.dismissProgressDialog(); // dismissing the dialog
            });

    private Uri getUriToDrawable(@NonNull Context context,
                                 @AnyRes int drawableId) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + context.getResources().getResourcePackageName(drawableId)
                + '/' + context.getResources().getResourceTypeName(drawableId)
                + '/' + context.getResources().getResourceEntryName(drawableId));
    }


    public void UploadPostBtn(View view) {

        dialogs.showProgressDialog("Getting Image", "Wait few seconds"); // showing a waiting dialog

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        launcher.launch(Intent.createChooser(intent, "Select a picture"));
    }


    private void postFoodRecipe() {
        isAlreadyExists = false;

        if (checkTitle() && checkTagLine() && checkContent() && setReferences()/* && checkImage()*/) {
            dialogs.showProgressDialog("Uploading Post", "Please wait...");
            database.getReference().child("Users").child(currentUserUid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                for (DataSnapshot s : snapshot.getChildren()) {
                                    if (title.equals(s.getKey())) {
                                        isAlreadyExists = true;
                                        showToast("A post with this title is already exists", 1);
                                        dialogs.dismissProgressDialog();
                                        break;
                                    }
                                }
                            }

                            if (!isAlreadyExists) {
                                uploadPost();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


        }
    }


    private void uploadPost() {
        // a reference for particular folder
        final StorageReference storageReference = storage.getReference().child("Posts")
                .child(currentUserUid)
                .child(title);


        // getting url for image selected
        storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {

            // when it generates url of the image successfully
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                // creating a model of new post
                String date = "";
                if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
                    date = LocalDate.now().toString();
                }
                PostModel newPost = new PostModel(auth.getUid(), userModel.getUsername(), title, tagLine, content, uri.toString(), references, date);


                // setting up value int the realtime database
                database.getReference().child("Posts").child(currentUserUid)
                        .child(title).setValue(newPost).addOnCompleteListener(task -> {
                            // if successfully uploaded
                            dialogs.dismissProgressDialog();// dismissing the dialog

                            if (task.isSuccessful()) {
                                database.getReference().child("Users")
                                        .child(currentUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                UserModel model = snapshot.getValue(UserModel.class);
                                                if (model != null) {
                                                    database.getReference().child("Users")
                                                            .child(currentUserUid).child("postCount").setValue(model.getPostCount() + 1)
                                                            .addOnCompleteListener(task1 -> {
                                                                if (task1.isSuccessful()) {
                                                                    showToast("Post uploaded", 1); // show toast to the user
                                                                    finish(); // finish the new post activity(this activity)
                                                                } else
                                                                    showToast(Objects.requireNonNull(task1.getException()).getMessage(), 1);
                                                                dialogs.dismissProgressDialog();
                                                            }).addOnFailureListener(e -> {
                                                                showToast(e.getMessage(), 1);
                                                                dialogs.dismissProgressDialog();
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                            } else {
                                // else showing the error
                                showToast(Objects.requireNonNull(task.getException()).getMessage(), 1);
                            }
                        }).addOnFailureListener(e -> {
                            showToast(e.getMessage(), 1);
                            dialogs.dismissProgressDialog();
                        });
                //showing the error
            }).addOnFailureListener(e -> {
                showToast(e.getMessage(), 1);
                dialogs.dismissProgressDialog();

            });
            // showing the error
        }).addOnFailureListener(e -> {
            showToast(e.getMessage(), 1);
            dialogs.dismissProgressDialog();

        });

    }


    private boolean checkTitle() {
        try {
            title = binding.postFrTitleEt.getText().toString().trim();


            if (title.contains("#")) {
                Toast.makeText(this, "Title should not contain #", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (title.contains(".")) {
                Toast.makeText(this, "Title should not contain full-stop (.)", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (title.contains("$")) {
                Toast.makeText(this, "Title should not contain $", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (title.contains("[") || title.contains("]")) {
                Toast.makeText(this, "Title should not contain square brackets", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (title.length() >= 80) {
                Toast.makeText(this, "Title length must be less than 100 characters", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!title.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        showToast("Please enter Title", 0);
        return false;
    }

    // to check tag line
    private boolean checkTagLine() {
        try {
            tagLine = binding.postFrTagLineEt.getText().toString().trim();
//            if (tagLine.matches("[#.$]")) {
//                Toast.makeText(this, "Content should not contain Non-Alpha characters", Toast.LENGTH_SHORT).show();
//                return false;
//            }
            if (tagLine.length() >= 100) {
                Toast.makeText(this, "Tagline length must be less than 120 characters", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (!tagLine.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        showToast("Please enter Tag Line", 0);
        return false;
    }

    // to check ingredients

    // to check procedure
    private boolean checkContent() {
        try {
            content = binding.postFrProcedureEt.getText().toString().trim();
//            if (procedure.matches("[#.$]")) {
//                Toast.makeText(this, "Content should not contain Non-Alpha characters", Toast.LENGTH_SHORT).show();
//                return false;
//            }
            if (!content.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        showToast("Please enter Content", 0);
        return false;
    }

    // it is optional so we need not to ask user
    private boolean setReferences() {
        try {
            references = binding.postFrAddInsEt.getText().toString().trim();
//            if (procedure.matches("[#.$]")) {
//                Toast.makeText(this, "Content should not contain Non-Alpha characters", Toast.LENGTH_SHORT).show();
//                return false;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    private void showToast(String msg, int time) {
        if (time == 0) {
            Toast.makeText(PostActivity.this, msg, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(PostActivity.this, msg, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // post
        postFoodRecipe();

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseApp.initializeApp(PostActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
    }
}