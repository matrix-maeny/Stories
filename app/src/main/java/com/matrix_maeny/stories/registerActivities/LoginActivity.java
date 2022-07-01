package com.matrix_maeny.stories.registerActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.stories.MainActivity;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.UserModel;
import com.matrix_maeny.stories.databinding.ActivityLoginBinding;
import com.matrix_maeny.stories.javaclasses.ExposeDialogs;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    private ExposeDialogs dialogs;

    private String email = null, password = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // to create a translucent status of status bar

        FirebaseApp.initializeApp(LoginActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        initialize();
    }




    private void initialize() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null && user.isEmailVerified()) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }

        dialogs = new ExposeDialogs(LoginActivity.this);


        binding.lgSignUpTv.setOnClickListener(lgSignUpTvListener);
        binding.lgLoginBtn.setOnClickListener(lgLoginBtnListener);


    }

    View.OnClickListener lgSignUpTvListener = v -> {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        finish();
    };

    View.OnClickListener lgLoginBtnListener = v -> login();


    private void login() {
        if (checkEmail() && checkPassword()) {
            dialogs.showProgressDialog("Logging in...", "Please wait few seconds");
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        if (user.isEmailVerified()) {
                            setupUserDetails();
                        } else {
                            dialogs.dismissProgressDialog();
                            ExposeDialogs.showVerificationDialog(getSupportFragmentManager(), "verification dialog", true, user.getEmail());
                        }
                    }

                } else {
                    dialogs.dismissProgressDialog();
                    Toast.makeText(LoginActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }

            }).addOnFailureListener(e -> {

                dialogs.dismissProgressDialog();
                Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            });
        }

    }

    private void setupUserDetails() {
        firebaseDatabase.getReference().child("Users").child(Objects.requireNonNull(firebaseAuth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel model = snapshot.getValue(UserModel.class);

                            if (model != null && model.getEmail().equals("")) {
                                model.setEmail(email);
                                model.setPassword(password);

                                firebaseDatabase.getReference().child("Users").child(firebaseAuth.getUid()).setValue(model)
                                        .addOnCompleteListener(task -> {

                                            dialogs.dismissProgressDialog();

                                            if (task.isSuccessful()) {
                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                finish();
                                            } else
                                                Toast.makeText(LoginActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                                        }).addOnFailureListener(e -> {
                                            dialogs.dismissProgressDialog();
                                            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                        });
                            }else{
                                dialogs.dismissProgressDialog();

                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialogs.dismissProgressDialog();
                        Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private boolean checkEmail() {
        try {
            email = Objects.requireNonNull(binding.lgEmailEt.getText()).toString();
            if (!email.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter Email", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean checkPassword() {
        try {
            password = Objects.requireNonNull(binding.lgPasswordEt.getText()).toString();
            if (!password.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter Password", Toast.LENGTH_SHORT).show();
        return false;
    }

}