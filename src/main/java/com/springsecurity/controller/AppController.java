package com.springsecurity.controller;

import com.springsecurity.config.CustomUserDetails;
import com.springsecurity.model.Role;
import com.springsecurity.model.User;
import com.springsecurity.repository.RoleRepo;
import com.springsecurity.repository.UserRepo;
import com.springsecurity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class AppController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;

    @RequestMapping("")
    public String viewHomePage() {
        return "index";
    }

    @RequestMapping("/login")
    public String showLoginPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            model.addAttribute("message", "");
            return "login";
        }
        return "redirect:/";
    }

    @RequestMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register_form";
    }

    @RequestMapping(value = "/process_register", method = RequestMethod.POST)
    public String processRegister(User user) {

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        userService.registerDefaultUser(user);
        return "register_success";
    }

    @RequestMapping(value = "/users")
    public String getListUser(Model model) {
        List<User> userList = userRepo.findAll();
        model.addAttribute("listUsers", userList);

        return "users";
    }

    @RequestMapping("/users/edit/{id}")
    public String editUser(@PathVariable("id") Integer id, Model model) {

        Optional<User> user = userRepo.findById(id);
        List<Role> roleList = roleRepo.findAll();

        if(user.isPresent()) {
            model.addAttribute("user", user.get());
        }
        model.addAttribute("listRoles", roleList);

        return "user_form";
    }

    @RequestMapping(value = "/users/save", method = RequestMethod.POST)
    public String saveUser(User user) {

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        userRepo.save(user);
        return "redirect:/users";
    }
    @RequestMapping(value = "/admin")
    public String aminPage() {

        return "admin";
    }

}