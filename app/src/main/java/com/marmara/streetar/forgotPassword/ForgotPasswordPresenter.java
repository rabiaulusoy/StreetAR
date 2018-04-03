package com.marmara.streetar.forgotPassword;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by EDA on 12.03.2018.
 */

public class ForgotPasswordPresenter {
    ForgotPasswordView forgotPasswordView;

    public ForgotPasswordPresenter(ForgotPasswordView forgotPasswordView){
        this.forgotPasswordView = forgotPasswordView;
    }

    public void performForgotPassword(String email, FirebaseAuth auth){
        if(TextUtils.isEmpty(email)){
            forgotPasswordView.forgotPasswordValidations();
        }
        else{
            //TODO database den kontrol edilecek

            auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        forgotPasswordView.forgotPasswordSuccess();
                    } else {
                        forgotPasswordView.forgotPasswordError();
                    }
                }
            });
        }
    }
}
