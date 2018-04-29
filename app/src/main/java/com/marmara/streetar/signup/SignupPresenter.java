package com.marmara.streetar.signup;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by r_rab on 2/26/2018.
 */

public class SignupPresenter {
    SignupView signupView;

    public SignupPresenter(SignupView signupView){
        this.signupView = signupView;
    }

    public void performSignup(final String email, String username, String password, FirebaseAuth auth, DatabaseReference databaseReference){
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(email)){
            signupView.signupValidations();
        }else //TODO database den kontrol edilecek
            if(password.length() < 6){
            signupView.passwordLength();
        }
        else//
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //  progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {

                            String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users/" + uId);
                            User user = new User(email, uId,"deneme");
                            databaseReference.setValue(user);

                            signupView.signupSuccess();
                        } else {
                            signupView.signupError();
                        }
                    }
                });
    }
}
