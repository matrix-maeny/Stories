package com.matrix_maeny.stories.registerActivities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextThemeWrapper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.databinding.VerificationDialogBinding;
import com.matrix_maeny.stories.javaclasses.EmailVerification;

public class VerificationDialog extends AppCompatDialogFragment {

    public static boolean isSent = false;
    public static String email = null;

    private VerificationDialogBinding binding;
    private EmailVerification emailVerification;



    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(requireContext(), androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert);
        AlertDialog.Builder builder = new AlertDialog.Builder(wrapper);

        @SuppressLint("InflateParams")
        View root = requireActivity().getLayoutInflater().inflate(R.layout.verification_dialog, null);
        binding = VerificationDialogBinding.bind(root);
        builder.setView(binding.getRoot());
        setCancelable(false);



        if (isSent) {
            binding.vdResultTv.setText(Html.fromHtml("Your email <b>"+email+"</b> is not verified yet"));
            binding.vdVerifyBtn.setText("verify now");
        } else {
            binding.vdResultTv.setText(Html.fromHtml("A link is sent to your email <b>"+email+"</b> (check in spam folder)"));
            binding.vdVerifyBtn.setText("ok");
        }


        binding.vdVerifyBtn.setOnClickListener(v -> {
            this.dismiss();

            if(binding.vdVerifyBtn.getText().equals("ok")){
                requireActivity().finish();
            }

            if (binding.vdVerifyBtn.getText().equals("verify now")) {
                // send email verification
                emailVerification = new EmailVerification(requireContext());
                emailVerification.sendEmailVerification(FirebaseAuth.getInstance(),requireActivity().getSupportFragmentManager());

                binding.vdVerifyBtn.setText("ok");
                binding.vdResultTv.setText(Html.fromHtml("A link is sent to your email <b>"+email+"</b> (check in spam folder)"));

            }
        });


        return builder.create();
    }



}
