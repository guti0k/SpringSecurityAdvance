package com.springsecurity.config;

import com.springsecurity.model.User;
import com.springsecurity.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationFailure(@NotNull HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        System.out.println("Fail login");
        String email = request.getParameter("email");

        User user = userService.getUserByEmail(email);

//        Khóa tài khoản nếu login failed
        if(user != null) {
            if(user.isEnabled() && user.isAccountNonLocked()) {
                if(user.getFailedAttempt() < UserService.MAX_FAILED_ATTEMPTS - 1) {
                    userService.increaseFailedAttempt(user);
                }
                else {
                    userService.lockUser(user);
                    exception = new LockedException("Your account has been locked due to 3 failed attempt. It will be unlocked after 24 hours.");
                }
            }
            else if(!user.isAccountNonLocked()) {
                if(userService.unlockWhenTimeExprired(user)) {
                    exception = new LockedException("Your account has been unlocked. Please try to login again.");
                }
            }
        }


//        OTP mail
        String failedRedirectURL = "/login?error&email=" + email;
        if(exception.getMessage().contains("OTP")) {
            failedRedirectURL = "/login?otp=true&email=" + email;
        }
        else if (user != null && user.isOTPRequire()){
            failedRedirectURL = "/login?otp=true&email=" + email;
        }


        super.setDefaultFailureUrl(failedRedirectURL);
        super.onAuthenticationFailure(request, response, exception);
    }
}
