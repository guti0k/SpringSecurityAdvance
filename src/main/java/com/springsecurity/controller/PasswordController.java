package com.springsecurity.controller;

import com.springsecurity.config.CustomUserDetails;
import com.springsecurity.model.User;
import com.springsecurity.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/change_password")
    public String showChangePasswordForm(Model model) {

        model.addAttribute("pageTitle", "Change Expired Password");
        return "change_password_form";
    }

    @PostMapping("/change_password")
    public String processChangePassword(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        Model model) throws ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = customUserDetails.user;

        String oldPassword = httpServletRequest.getParameter("oldPassword");
        String newPassword = httpServletRequest.getParameter("newPassword");

        if(!passwordEncoder.matches(oldPassword, user.getPassword())) {
            model.addAttribute("pageTitle", "Change Expired Password");
            model.addAttribute("message", "Your old password is incorrect.");
            return "change_password_form";
        }
        if (oldPassword.equals(newPassword)) {
            model.addAttribute("pageTitle", "Change Expired Password");
            model.addAttribute("message", "Your new password must be different than the old one.");
            return "change_password_form";
        }

        userService.changePassword(user, newPassword);
        httpServletRequest.logout();

        model.addAttribute("message", "You have changed your password successfully. "
                + "Please login again.");
        return "login";
    }
}
