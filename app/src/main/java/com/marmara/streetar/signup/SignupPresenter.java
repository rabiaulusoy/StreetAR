package com.marmara.streetar.signup;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by r_rab on 2/26/2018.
 */

public class SignupPresenter {
    SignupView signupView;

    public SignupPresenter(SignupView signupView){
        this.signupView = signupView;
    }

    public void performSignup(String email, String username, String password, FirebaseAuth auth){
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(email)){
            signupView.signupValidations();
        }else if(password.length() < 6){
            signupView.passwordLength();
        }
        else{
            //TODO database den kontrol edilecek

            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //  progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        signupView.signupSuccess();
                    } else {
                        signupView.signupError();
                    }
                }
            });
        }
    }
}
