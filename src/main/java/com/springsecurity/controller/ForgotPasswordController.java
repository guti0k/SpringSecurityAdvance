package com.springsecurity.controller;

import com.springsecurity.config.Utility;
import com.springsecurity.model.User;
import com.springsecurity.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;


@Controller
public class ForgotPasswordController {
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private UserService userService;

    @GetMapping("/forgot_password")
    public String showForgotPasswordForm(Model model) {

        model.addAttribute("pageTitle", "Forgot Password");

        return "forgot_password_form";
    }

    public void sendMail(String email, String resetPasswordLink) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage);

        mimeMessageHelper.setFrom("nguyenduyquyen0017@gmail.com", "MyShop Support");
        mimeMessageHelper.setTo(email);

        String subject = "Here's the link to reset your password";
        String content = "<p>Hello,</p>"
                + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>"
                + "<p><a href=\"" + resetPasswordLink + "\">Change my password</a></p>"
                + "<br>"
                + "<p>Ignore this email if you do remember your password, "
                + "or you have not made the request.</p>";

        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(content, true);

        javaMailSender.send(mimeMailMessage);

    }

    @PostMapping("/forgot_password")
    public String processForgotPassword(Model model, HttpServletRequest httpServletRequest) {

//        System.out.println("getContextPath: " + request.getContextPath());
//        System.out.println("getRequestURL: " + request.getRequestURL()); đường dẫn url locol....
//        System.out.println("getServletPath: " + request.getServletPath()); đường dẫn path

        String email = httpServletRequest.getParameter("email");
        String token = RandomString.make(30);

        try {
            userService.updateResetPasswordToken(token, email);

//            generate reset password link
            String resetPasswordLink = Utility.getSiteURL(httpServletRequest) + "/reset_password?token=" + token;

//            send mail
            sendMail(email, resetPasswordLink);

            model.addAttribute("message", "We have sent a reset password link to your email. Please check.");
        } catch (MessagingException | UnsupportedEncodingException ex) {
            model.addAttribute("error", "Error while sending email");
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
        }

        return "forgot_password_form";
    }

    @GetMapping("/reset_password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        User user = userService.getUserByToken(token);
        model.addAttribute("token", token);

        if(user == null) {
            model.addAttribute("message", "Invalid Token");
            return "message";
        }
        return "reset_password_form";
    }

    @PostMapping("/reset_password")
    public String processResetPassword(HttpServletRequest httpServletRequest, Model model) {
        String token = httpServletRequest.getParameter("token");
        String newPassword = httpServletRequest.getParameter("password");
        User user = userService.getUserByToken(token);

        if(user == null) {
            model.addAttribute("message", "Invalid Token");
        }
        else {
            userService.updatePassword(user, newPassword);
            model.addAttribute("message", "You have successfully changed your password.");
        }
        return "message";
    }
}
