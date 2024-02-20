package com.springsecurity.config;

import com.springsecurity.model.User;
import com.springsecurity.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class BeforeAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    @Autowired
    private UserService userService;

    public BeforeAuthenticationFilter() {
        super.setUsernameParameter("email");
        super.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String email = request.getParameter("email");
        User user = userService.getUserByEmail(email);

        System.out.println("Before authentication");

        if(user != null) {
            float spamCore = getGoogleRecaptchaScore();
            if(spamCore < 0.5) {
                if(user.isOTPRequire()) {
                    return super.attemptAuthentication(request, response);
                }

//                generate OTP and send Mail
                try {
                    userService.generateOneTimePassword(user);
                    throw new InsufficientAuthenticationException("OTP");

                } catch (MessagingException | UnsupportedEncodingException e) {
                    throw new AuthenticationServiceException("Error while sending OTP email.");
                }
            }
        }

        return super.attemptAuthentication(request, response);
    }

    private float getGoogleRecaptchaScore() {
        return 0.6f;
    }

    @Autowired
    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }
    @Autowired
    @Override
    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
        super.setAuthenticationSuccessHandler(successHandler);
    }
    @Autowired
    @Override
    public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
        super.setAuthenticationFailureHandler(failureHandler);
    }
}
