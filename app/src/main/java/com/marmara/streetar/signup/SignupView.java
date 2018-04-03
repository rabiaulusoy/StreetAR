package com.marmara.streetar.signup;

/**
 * Created by r_rab on 2/26/2018.
 */

public interface SignupView {
    void signupValidations();
    void signupSuccess();
    void signupError();
    void passwordLength();
}
