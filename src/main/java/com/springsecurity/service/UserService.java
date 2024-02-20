package com.springsecurity.service;

import com.springsecurity.model.Role;
import com.springsecurity.model.User;
import com.springsecurity.repository.RoleRepo;
import com.springsecurity.repository.UserRepo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Date;

@Service
@Transactional
public class UserService {

    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final int LOCK_TIME_DURATION = 10 * 60 * 1000;

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    public UserRepo userRepo;
    @Autowired
    public RoleRepo roleRepo;
    public void registerDefaultUser(User user) {
        Role role = roleRepo.findByName("User");
        user.addRole(role);
        userRepo.save(user);
    }
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public void updateResetPasswordToken(String token, String email) throws Exception {
        User user = userRepo.findByEmail(email);

        if(user != null) {
            user.setResetPasswordToken(token);
            userRepo.save(user);
        }
        else {
            throw new Exception("Could not found any customer with email: " + email);
        }
    }

    public User getUserByToken(String token) {
        return userRepo.findByResetPasswordToken(token);
    }

    public void updatePassword(User user, String newPassword) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        newPassword = bCryptPasswordEncoder.encode(newPassword);

        user.setPassword(newPassword);
        user.setResetPasswordToken(null);
        userRepo.save(user);
    }

    public void increaseFailedAttempt(User user) {
        int failedAttempt = user.getFailedAttempt() + 1;
        userRepo.updateFailedAttempts(failedAttempt, user.getEmail());
    }

    public void lockUser(User user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());

        userRepo.save(user);
    }

    public boolean unlockWhenTimeExprired(User user) {
        long currentTime = System.currentTimeMillis();

        if(user.getLockTime().getTime() + LOCK_TIME_DURATION < currentTime) {
            user.setAccountNonLocked(true);
            user.setFailedAttempt(0);
            user.setLockTime(null);

            userRepo.save(user);
            return true;
        }
        return false;
    }

    public void resetFailedAttempt(String email) {

        userRepo.updateFailedAttempts(0, email);
    }

    public void generateOneTimePassword(User user) throws MessagingException, UnsupportedEncodingException {
        String OTP = RandomString.make(8);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        String encodeOTP = bCryptPasswordEncoder.encode(OTP);

        user.setOneTimePassword(encodeOTP);
        user.setOtpRequestedTime(new Date());
        userRepo.save(user);

        sendOTPMail(user, OTP);
    }

    private void sendOTPMail(User user, String OTP) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        helper.setFrom("nguyenduyquyen0017@gmail.com", "MyShop Support");
        helper.setTo(user.getEmail());

        String subject = "Here's your One Time Password (OTP) - Expire in 5 minutes!;";
        String content = "<p>Hello " + user.getFirstName() + "</p>"
                + "<p>For security reason, you're required to use the following "
                + "One Time Password to login:</p>"
                + "<p><b>" + OTP + "</b></p>"
                + "<br>"
                + "<p>Note: this OTP is set to expire in 5 minutes.</p>";
        helper.setSubject(subject);
        helper.setText(content, true);

        javaMailSender.send(mimeMessage);
    }

    public void clearOTP(User user) {
        user.setOneTimePassword(null);
        user.setOtpRequestedTime(null);

        userRepo.save(user);
    }

    public void changePassword(User user, String newPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangeTime(new Date());

        userRepo.save(user);
    }
}
