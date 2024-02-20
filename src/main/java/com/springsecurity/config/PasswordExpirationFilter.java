package com.springsecurity.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class PasswordExpirationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        System.out.println("\nBefore Login filter: " + httpServletRequest.getRequestURL().toString());

//        lọc các đường dẫn chứa file tĩnh hay /change_password
        String requestUrl = httpServletRequest.getRequestURL().toString();
        if(requestUrl.endsWith(".css") || requestUrl.endsWith(".html") || requestUrl.endsWith(".js")
                || requestUrl.endsWith("/change_password")) {
            chain.doFilter(request, response);
            return;
        }

//        kiểm tra xem ng dùng đã đăng nhập chưa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = null;

        if(authentication != null) {
            principal = authentication.getPrincipal();
        }
//        System.out.println("authentication: " + authentication);
//        System.out.println("principal: " + principal);

        if(principal != null && principal instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) principal;

            if(customUserDetails.user.isPasswordExpired()) {
                String redirectURL = httpServletRequest.getContextPath() + "/change_password";
                httpServletResponse.sendRedirect(redirectURL);
            }
        }

        chain.doFilter(request, response);
    }
}
