package com.springsecurity.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoder {

    public static void main(String[] args) {

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.out.println(bCryptPasswordEncoder.encode("111111"));
        System.out.println(bCryptPasswordEncoder.matches("123456", "$2a$10$IC9P6cmJ/XG9HTh2FvD4ieVt9ob.VL3rH/Y6JfUo/dTC2jVwX0A7u"));

    }

}

//$2a$10$vqcWyOUoREALQyobyy8X1uuCCp7tmNs/tv2EjsvQBGqkdZRV.38vO
//$10$97sGMpw2B3N2fDSKGtxZvO8vTgbjGdc0nttIxGWzznqVAppB.yP42