package com.springsecurity.config;

import com.springsecurity.model.User;
import com.springsecurity.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

//        customUserDetails.getAuthorities().stream().forEach(role -> System.out.println(role.getAuthority()));
//        System.out.println(request.getContextPath());
//        System.out.println(customUserDetails.user.getId());
//        System.out.println(customUserDetails.hasRole("Admin"));

        System.out.println("Success login");

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = customUserDetails.user;

        System.out.println("user: " + user.getEmail());

        if(user.getFailedAttempt() > 0) {
            userService.resetFailedAttempt(user.getEmail());
        }

        if(user.isOTPRequire()) {
            userService.clearOTP(user);
        }

        String redirectUrl = request.getContextPath();

        if(customUserDetails.hasRole("User")) {
            redirectUrl += "/";
        }
        else if (customUserDetails.hasRole("Admin")) {
            redirectUrl += "/admin";
        }

        super.setDefaultTargetUrl(redirectUrl);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
