package com.marmara.streetar.login;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by r_rab on 2/25/2018.
 */

public class LoginPresenter {
    LoginView loginView;
    public LoginPresenter(LoginView loginView){
        this.loginView = loginView;
    }
    public void performLogin(String username, String password, FirebaseAuth auth){
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password) ){
            loginView.loginValidations();
        }else if(password.length() < 6){
            loginView.passwordLength();
        }else{
            auth.signInWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        loginView.loginSuccess();
                    } else {
                        loginView.loginError();
                    }
                }
            });
        }
    }

    public void navigateToSignUp(){
        loginView.navigateToSignUpActivity();
    }
    public void navigateToForgotPass(){
        loginView.navigateToForgotPasswordActivity();
    }
}
